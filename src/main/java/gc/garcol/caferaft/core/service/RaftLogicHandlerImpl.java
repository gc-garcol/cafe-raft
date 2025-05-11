package gc.garcol.caferaft.core.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.yaml.snakeyaml.util.Tuple;

import gc.garcol.caferaft.application.network.cluster.ClusterRpcNetworkOutbound;
import gc.garcol.caferaft.core.async.ExecutorEventPublisher;
import gc.garcol.caferaft.core.client.CommandResponse;
import gc.garcol.caferaft.core.client.CommandSerdes;
import gc.garcol.caferaft.core.client.CommonErrorResponse;
import gc.garcol.caferaft.core.constant.ClusterProperty;
import static gc.garcol.caferaft.core.constant.LogConstant.INITIAL_POSITION;
import gc.garcol.caferaft.core.log.LogEntry;
import gc.garcol.caferaft.core.log.LogEntryRequest;
import gc.garcol.caferaft.core.log.LogManager;
import gc.garcol.caferaft.core.log.Position;
import gc.garcol.caferaft.core.repository.ClusterStateRepository;
import gc.garcol.caferaft.core.rpc.AppendEntryRequest;
import gc.garcol.caferaft.core.state.CandidateVolatileState;
import gc.garcol.caferaft.core.state.RaftState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author thaivc
 * @since 2025
 */
@Slf4j
@RequiredArgsConstructor
public class RaftLogicHandlerImpl implements RaftLogicHandler {
    private final RaftState raftState;
    private final LogManager logManager;
    private final StateMachine stateMachine;
    private final ClusterProperty clusterProperty;
    private final LinkedList<ClientReplier> repliers;
    private final ExecutorEventPublisher replyPublisher;
    private final ClusterStateRepository clusterStateRepository;
    private final BroadcastService broadcastService;
    private final ClusterRpcNetworkOutbound clusterRpcNetworkOutbound;
    private final TaskExecutor commonExecutorPool;
    private final CommandSerdes commandSerdes;

    @Override
    public void apply() {
        switch (raftState.getRole()) {
            case FOLLOWER -> followerLogic();
            case CANDIDATE -> candidateLogic();
            case LEADER -> leaderLogic();
        }
    }

    private void candidateLogic() {
        var currentTime = System.currentTimeMillis();
        if (currentTime >= raftState.getElectionTimeout()) {
            log.debug("Election timeout, request for election");

            var newElectionTimeout = System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(
                clusterProperty.getElectionTimeoutMs()[0],
                clusterProperty.getElectionTimeoutMs()[1]
            );
            raftState.setElectionTimeout(newElectionTimeout);

            this.prepareForVote();
            this.broadcastService.broadcastVote();
        }
    }

    private void leaderLogic() {
        if (raftState.getHeartbeatTimeout() < System.currentTimeMillis()) {
            this.broadcastService.broadcastHeartbeat();
            raftState.setHeartbeatTimeout(System.currentTimeMillis() + clusterProperty.getHeartbeatIntervalMs());
        } else {
            this.broadcastService.broadcast(nodeId -> {
                var lastPosition = logManager.lastPosition();

                Position nextAppendPosition = raftState.getLeaderVolatileState().getNextAppendPositions().get(nodeId);

                List<LogEntryRequest> logEntries = new ArrayList<>();

                int batchSize = clusterProperty.getAppendLogBatchSize();
                Position currentAppendPosition = nextAppendPosition;

                // [Docs]: If last log index ≥ nextIndex for a follower: send
                // AppendEntries RPC with log entries starting at nextIndex
                while (logEntries.size() < batchSize && currentAppendPosition != null && currentAppendPosition.compareTo(lastPosition) <= 0) {
                    if (!INITIAL_POSITION.equals(currentAppendPosition)) {
                        LogEntry logEntry = logManager.getLog(currentAppendPosition.term(), currentAppendPosition.index());
                        if (logEntry == null) {
                            break;
                        }
                        logEntries.add(
                            new LogEntryRequest(
                                logEntry.getPosition(),
                                commandSerdes.type(logEntry.getCommand()),
                                commandSerdes.toBytes(logEntry.getCommand())
                            )
                        );
                    }
                    currentAppendPosition = logManager.nextPosition(currentAppendPosition);
                }

                if (!logEntries.isEmpty()) {
                    Position previousPosition = logManager.previousPosition(logEntries.getFirst().getPosition());

                    AppendEntryRequest appendEntryRequest = new AppendEntryRequest();
                    appendEntryRequest.setSender(raftState.getPersistentState().getNodeId());
                    appendEntryRequest.setTerm(raftState.getPersistentState().getCurrentTerm());
                    appendEntryRequest.setPreviousPosition(previousPosition);
                    appendEntryRequest.setLeaderCommit(raftState.getVolatileState().getCommitPosition());
                    appendEntryRequest.setEntries(logEntries);

                    commonExecutorPool.execute(() -> clusterRpcNetworkOutbound.appendEntryRequest(nodeId, appendEntryRequest));
                }
            });
        }

        applyCommitedLog();
    }

    private void followerLogic() {
        var currentTime = System.currentTimeMillis();
        if (currentTime >= raftState.getHeartbeatTimeout()) {
            log.info("Election timeout, convert to CANDIDATE");

            raftState.toCandidate();
            raftState.setCandidateVolatileState(new CandidateVolatileState());
            var newElectionTimeout = System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(
                clusterProperty.getElectionTimeoutMs()[0],
                clusterProperty.getElectionTimeoutMs()[1]
            );
            raftState.setElectionTimeout(newElectionTimeout);
            raftState.setHeartbeatTimeout(0);
            return;
        }

        int batchSize = clusterProperty.getCommitedLogBatchSize();
        for (int i = 0; i < batchSize; i++) {
            this.applyStateMachine();
        }
    }

    Tuple<Position, CommandResponse> applyStateMachine() {
        // [Docs]: If commitIndex > lastApplied: increment lastApplied, apply
        // log[lastApplied] to state machine
        if (raftState.getVolatileState().getLastApplied().compareTo(raftState.getVolatileState().getCommitPosition()) < 0) {
            Position nextPosition = logManager.nextPosition(raftState.getVolatileState().getLastApplied());
            if (nextPosition == null) {
                return null;
            }
            LogEntry logEntry = logManager.getLog(nextPosition.term(), nextPosition.index());
            var response = stateMachine.accept(logEntry.getCommand());
            raftState.getVolatileState().setLastApplied(nextPosition);
            return new Tuple<>(nextPosition, response);
        }
        return null;
    }

    private void prepareForVote() {
        long currentTerm = raftState.getPersistentState().getCurrentTerm();
        raftState.getPersistentState().setCurrentTerm(currentTerm + 1);
        raftState.getPersistentState().setVotedFor(raftState.getPersistentState().getNodeId()); // Vote for self
        clusterStateRepository.save(raftState.getPersistentState());

        raftState.getCandidateVolatileState().getVotedQuorum().clear();

        // Vote for self
        raftState.getCandidateVolatileState().getVotedQuorum().put(raftState.getPersistentState().getNodeId(), true);
    }

    private void applyCommitedLog() {
        int batchSize = clusterProperty.getCommitedLogBatchSize();

        for (int i = 0; i < batchSize; i++) {
            Tuple<Position, CommandResponse> applyLogResult = this.applyStateMachine();
            if (applyLogResult == null) {
                break;
            }

            Position appliedPosition = applyLogResult._1();
            CommandResponse response = applyLogResult._2();

            handleExpiredRepliers(appliedPosition);
            handleCurrentPositionResponse(appliedPosition, response);
        }
    }

    private void handleExpiredRepliers(Position currentPosition) {
        while (!repliers.isEmpty() && repliers.getFirst().position().compareTo(currentPosition) < 0) {
            var replier = repliers.removeFirst();
            replyPublisher.publish(() -> replier.replier().complete(
                new CommonErrorResponse(
                    HttpStatus.REQUEST_TIMEOUT.value(),
                    "Request timeout, old-request is not processed"
                )
            ));
        }
    }

    private void handleCurrentPositionResponse(Position currentPosition, CommandResponse response) {
        if (!repliers.isEmpty() && repliers.getFirst().position().compareTo(currentPosition) == 0) {
            var replier = repliers.removeFirst();
            replyPublisher.publish(() -> replier.replier().complete(response));
        }
    }
}

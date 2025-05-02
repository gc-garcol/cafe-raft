package gc.garcol.caferaft.core.service;

import gc.garcol.caferaft.application.network.cluster.ClusterRpcNetworkOutbound;
import gc.garcol.caferaft.core.async.ExecutorEventPublisher;
import gc.garcol.caferaft.core.client.CommandResponse;
import gc.garcol.caferaft.core.client.CommonErrorResponse;
import gc.garcol.caferaft.core.constant.ClusterProperty;
import gc.garcol.caferaft.core.log.LogEntry;
import gc.garcol.caferaft.core.log.LogManager;
import gc.garcol.caferaft.core.log.Position;
import gc.garcol.caferaft.core.repository.ClusterStateRepository;
import gc.garcol.caferaft.core.rpc.VoteRequest;
import gc.garcol.caferaft.core.state.CandidateVolatileState;
import gc.garcol.caferaft.core.state.NodeId;
import gc.garcol.caferaft.core.state.RaftState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.yaml.snakeyaml.util.Tuple;

import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

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
    private final ClusterRpcNetworkOutbound clusterRpcNetworkOutbound;
    private final TaskExecutor commonExecutorPool;

    @Override
    public void apply() {
        switch (raftState.getRole()) {
            case CANDIDATE -> candidateLogic();
            case LEADER -> leaderLogic();
            case FOLLOWER -> followerLogic();
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
            this.broadcastVote();
        }
    }

    private void leaderLogic() {

        // TODO appendEntry in other thread

        applyCommitedLog();
    }

    private void followerLogic() {
        var currentTime = System.currentTimeMillis();
        if (currentTime >= raftState.getHeartbeatTimeout()) {
            log.info("Election timeout, convert to CANDIDATE");

            raftState.toCandidate();
            raftState.setCandidateVolatileState(new CandidateVolatileState());
            raftState.setElectionTimeout(0);
            raftState.setHeartbeatTimeout(0);
            return;
        }

        this.applyStateMachine();
    }

    Tuple<Position, CommandResponse> applyStateMachine() {
        // If commitIndex > lastApplied: increment lastApplied, apply
        // log[lastApplied] to state machine
        if (raftState.getVolatileState().getLastApplied().compareTo(raftState.getVolatileState().getCommitPosition()) < 0) {
            Position nextPosition = logManager.nextPosition(raftState.getVolatileState().getLastApplied());
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

    private void broadcastVote() {
        Position lastPosition = logManager.lastPosition();
        long currentTerm = raftState.getPersistentState().getCurrentTerm();

        VoteRequest voteRequest = VoteRequest.builder()
            .term(currentTerm)
            .candidateId(raftState.getPersistentState().getNodeId())
            .sender(raftState.getPersistentState().getNodeId())
            .lastPosition(lastPosition)
            .build();

        for (int nodeId = 0; nodeId < clusterProperty.getNodes().size(); nodeId++) {
            if (nodeId == raftState.getPersistentState().getNodeId().id()) {
                continue;
            }

            final NodeId targetNode = new NodeId(nodeId);
            commonExecutorPool.execute(() -> clusterRpcNetworkOutbound.voteRequest(targetNode, voteRequest));
        }
    }

    private void applyCommitedLog() {
        int batchSize = 1_000;

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

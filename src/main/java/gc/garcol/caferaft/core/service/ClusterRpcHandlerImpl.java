package gc.garcol.caferaft.core.service;

import gc.garcol.caferaft.application.network.cluster.ClusterRpcNetworkOutbound;
import gc.garcol.caferaft.core.client.CommandSerdes;
import gc.garcol.caferaft.core.constant.ClusterProperty;
import gc.garcol.caferaft.core.log.LogEntry;
import gc.garcol.caferaft.core.log.LogEntryRequest;
import gc.garcol.caferaft.core.log.LogManager;
import gc.garcol.caferaft.core.log.Position;
import gc.garcol.caferaft.core.repository.ClusterStateRepository;
import gc.garcol.caferaft.core.rpc.*;
import gc.garcol.caferaft.core.state.NodeId;
import gc.garcol.caferaft.core.state.PersistentState;
import gc.garcol.caferaft.core.state.RaftRole;
import gc.garcol.caferaft.core.state.RaftState;
import gc.garcol.caferaft.core.util.Uncheck;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;

import java.util.List;

import static gc.garcol.caferaft.core.constant.LogConstant.INITIAL_POSITION;

/**
 * @author thaivc
 * @since 2025
 */
@Slf4j
@RequiredArgsConstructor
public class ClusterRpcHandlerImpl implements ClusterRpcHandler {

    private final RaftState raftState;
    private final LogManager logManager;
    private final ClusterRpcNetworkOutbound clusterRpcNetworkOutbound;
    private final ClusterStateRepository clusterStateRepository;
    private final TaskExecutor commonExecutorPool;
    private final ClusterProperty clusterProperty;
    private final BroadcastService broadcastService;
    private final CommandSerdes commandSerdes;

    @Override
    public void handleClusterRpc(ClusterRpc clusterRpc) {

        preCheckRpc(clusterRpc);

        switch (clusterRpc) {
            case AppendEntryRequest request -> this.handleAppendEntryRequest(request);
            case AppendEntryResponse response -> this.handleAppendEntryResponse(response);
            case VoteRequest request -> this.handleVoteRequest(request);
            case VoteResponse response -> this.handleVoteResponse(response);
            default -> log.error("Received unknown cluster rpc: {}", clusterRpc);
        }
    }

    private void preCheckRpc(ClusterRpc clusterRpc) {
        // [Docs]: If RPC request or response contains term T > currentTerm:
        // set currentTerm = T, convert to follower (§5.1)
        if (clusterRpc.getTerm() > raftState.getPersistentState().getCurrentTerm()) {
            log.info("Detect higher term, convert to FOLLOWER");
            raftState.toFollower();
            raftState.setHeartbeatTimeout(System.currentTimeMillis() + clusterProperty.getHeartbeatTimeoutMs());
            PersistentState persistentState = raftState.getPersistentState();
            persistentState.setVotedFor(null);
            persistentState.setCurrentTerm(clusterRpc.getTerm());
            clusterStateRepository.save(persistentState);
        }
    }

    private void handleAppendEntryRequest(AppendEntryRequest request) {
        log.debug("Received AppendEntryRequest {}", request);

        if (RaftRole.CANDIDATE.equals(raftState.getRole())) {
            log.info("Received append-log, convert to FOLLOWER");
            raftState.toFollower();
        }

        AppendEntryResponse appendEntryResponse = new AppendEntryResponse();
        appendEntryResponse.setSender(raftState.getPersistentState().getNodeId());
        appendEntryResponse.setTerm(raftState.getPersistentState().getCurrentTerm());

        // [Docs]: Reply false if term < currentTerm (§5.1)
        if (request.getTerm() < raftState.getPersistentState().getCurrentTerm()) {
            appendEntryResponse.setSuccess(false);
            appendEntryResponse.setConflictPosition(logManager.lastPosition());
            commonExecutorPool.execute(() -> clusterRpcNetworkOutbound.appendEntryResponse(request.getSender(), appendEntryResponse));
            raftState.setHeartbeatTimeout(System.currentTimeMillis() + clusterProperty.getHeartbeatTimeoutMs());
            return;
        }

        // [Docs]: Reply false if log doesn't contain an entry at prevLogIndex
        // whose term matches prevLogTerm (§5.3)
        if (!INITIAL_POSITION.equals(request.getPreviousPosition())) {
            LogEntry previousLog = logManager.getLog(request.getPreviousPosition().term(), request.getPreviousPosition().index());
            if (previousLog == null) {
                appendEntryResponse.setSuccess(false);
                appendEntryResponse.setConflictPosition(logManager.lastPosition());
                commonExecutorPool.execute(() -> clusterRpcNetworkOutbound.appendEntryResponse(request.getSender(), appendEntryResponse));
                raftState.setHeartbeatTimeout(System.currentTimeMillis() + clusterProperty.getHeartbeatTimeoutMs());
                return;
            }
        }

        // [Docs]: true if follower contained entry matching
        // prevLogIndex and prevLogTerm
        appendEntryResponse.setSuccess(true);

        // [Docs]: If an existing entry conflicts with a new one (same index
        // but different terms), delete the existing entry and all that
        // follow it (§5.3)
        {
            if (request.isHeartbeat()) {

                // [1] Truncate in case heartbeat
                // LEADER:    x x x
                // FOLLOWER:  x x x x x
                //                  * * : logs to be truncated

                Position nextPosition = logManager.nextPosition(request.getPreviousPosition());
                if (nextPosition != null) {
                    Uncheck.runSafe(() -> logManager.truncateLogs(nextPosition.term(), nextPosition.index()));
                }

            } else {

                // [1] Truncate in case having logs
                //              - -      - - -  : request-append-logs
                // LEADER:    x x x      y y y
                // FOLLOWER:  x x x x x
                //                  * * : logs to be truncated

                Position existedPosition = logManager.nextPosition(request.getPreviousPosition());
                for (LogEntryRequest logEntry : request.getEntries()) {
                    if (existedPosition == null) {
                        // [Docs]:  Append any new entries not already in the log
                        logManager.appendLog(logEntry.getPosition().term(), commandSerdes.fromBytes(logEntry.getType(), logEntry.getCommand()));
                        continue;
                    }

                    // if occur the conflict log, then truncate that log and all logs following it
                    // this occurs at-most-once due to the nextPosition is to be set to null
                    if (!Position.equals(existedPosition, logEntry.getPosition())) {
                        logManager.truncateLogs(existedPosition.term(), existedPosition.index());

                        // after truncating the logs, there are no conflict logs to check
                        existedPosition = null;

                        // [Docs]:  Append any new entries not already in the log
                        logManager.appendLog(logEntry.getPosition().term(), commandSerdes.fromBytes(logEntry.getType(), logEntry.getCommand()));

                    } else {
                        // else the append-log matches the existing log, then do no-thing
                        // continue with new log
                        existedPosition = logManager.nextPosition(existedPosition);
                    }
                }
            }
        }

        // [Docs]: If leaderCommit > commitIndex, set commitIndex =
        // min(leaderCommit, index of last new entry)
        if (request.getLeaderCommit().compareTo(raftState.getVolatileState().getCommitPosition()) > 0) {
            raftState.getVolatileState().setCommitPosition(request.getLeaderCommit());
        }

        Position matchedPosition = request.isHeartbeat() ? logManager.lastPosition() : request.getEntries().getLast().getPosition();
        appendEntryResponse.setMatchedPosition(matchedPosition);
        commonExecutorPool.execute(() -> clusterRpcNetworkOutbound.appendEntryResponse(request.getSender(), appendEntryResponse));
        raftState.setHeartbeatTimeout(System.currentTimeMillis() + clusterProperty.getHeartbeatTimeoutMs());
    }

    private void handleAppendEntryResponse(AppendEntryResponse response) {
        log.debug("Received AppendEntryResponse {}", response);
        if (!RaftRole.LEADER.equals(raftState.getRole())) {
            return;
        }

        // [Docs]: If AppendEntries fails because of log inconsistency:
        // decrement nextIndex and retry (§5.3)
        if (!response.isSuccess()) {
            var nextAppendPosition = raftState.getLeaderVolatileState().getNextAppendPositions().get(response.getSender());

            var downGradePosition = logManager.previousPosition(nextAppendPosition);
            if (downGradePosition == null) {
                var lastPosition = logManager.lastPosition();
                if (nextAppendPosition.compareTo(lastPosition) > 0) {
                    downGradePosition = lastPosition;
                } else {
                    downGradePosition = INITIAL_POSITION.copy();
                }
            }

            var conflictPosition = response.getConflictPosition();
            if (conflictPosition.compareTo(downGradePosition) < 0) {
                downGradePosition = conflictPosition;
            }

            if (raftState.getLeaderVolatileState().getMatchPositions().get(response.getSender()).compareTo(downGradePosition) <= 0) {
                raftState.getLeaderVolatileState().getNextAppendPositions().put(response.getSender(), downGradePosition);
            }
            return;
        }

        // [Docs]: If successful: update nextIndex and matchIndex for
        // follower (§5.3)
        if (raftState.getLeaderVolatileState().getMatchPositions().get(response.getSender()).compareTo(response.getMatchedPosition()) < 0) {
            var newNextAppendPosition = logManager.nextPosition(response.getMatchedPosition());
            if (newNextAppendPosition == null) {
                var lastPosition = logManager.lastPosition();
                newNextAppendPosition = new Position(lastPosition.term(), lastPosition.index() + 1);
            }

            raftState.getLeaderVolatileState().getMatchPositions().put(response.getSender(), response.getMatchedPosition());
            raftState.getLeaderVolatileState().getNextAppendPositions().put(response.getSender(), newNextAppendPosition);
        }

        // [Docs]: • If there exists an N such that N > commitIndex, a majority
        // of matchIndex[i] ≥ N, and log[N].term == currentTerm:
        // set commitIndex = N (§5.3, §5.4).
        List<Position> sortedMatchedPositions = raftState.getLeaderVolatileState()
            .getMatchPositions()
            .values()
            .stream()
            .sorted()
            .toList();

        Position quorumPosition = sortedMatchedPositions.get(sortedMatchedPositions.size() / 2);
        Position commitedPosition = raftState.getVolatileState().getCommitPosition();
        if (quorumPosition.compareTo(commitedPosition) > 0) {
            raftState.getVolatileState().setCommitPosition(quorumPosition);
        }
    }

    private void handleVoteRequest(VoteRequest request) {
        log.debug("Received VoteRequest {}", request);

        PersistentState persistentState = raftState.getPersistentState();

        // Reply false if term < currentTerm (§5.1)
        if (request.getTerm() < persistentState.getCurrentTerm()) {
            VoteResponse voteResponse = new VoteResponse();
            voteResponse.setSender(persistentState.getNodeId());
            voteResponse.setTerm(persistentState.getCurrentTerm());
            voteResponse.setVoteGranted(false);
            commonExecutorPool.execute(() -> clusterRpcNetworkOutbound.voteResponse(request.getSender(), voteResponse));
            return;
        }

        // [Docs]: If votedFor is null or candidateId, and candidate’s log is at
        // least as up-to-date as receiver’s log, grant vote (§5.2, §5.4)
        boolean isValidVoteFor = persistentState.getVotedFor() == null || persistentState.getVotedFor().id() == request.getCandidateId().id();
        boolean isValidLog = logManager.lastPosition().compareTo(request.getLastPosition()) <= 0;

        boolean shouldVote = isValidVoteFor && isValidLog;
        if (shouldVote) {
            persistentState.setVotedFor(request.getCandidateId());
            clusterStateRepository.save(persistentState);
        }

        VoteResponse voteResponse = new VoteResponse();
        voteResponse.setSender(persistentState.getNodeId());
        voteResponse.setTerm(persistentState.getCurrentTerm());
        voteResponse.setVoteGranted(shouldVote);
        commonExecutorPool.execute(() -> clusterRpcNetworkOutbound.voteResponse(request.getSender(), voteResponse));
    }

    private void handleVoteResponse(VoteResponse response) {
        log.debug("Received VoteResponse {}", response);

        if (!RaftRole.CANDIDATE.equals(raftState.getRole())) {
            return;
        }

        var quorum = raftState.getCandidateVolatileState().getVotedQuorum();
        quorum.put(response.getSender(), response.isVoteGranted());

        var totalApproved = quorum.values().stream().filter(Boolean::booleanValue).count();

        // [Docs]: If votes received from the majority of servers: become leader
        if (!RaftRole.LEADER.equals(raftState.getRole()) && totalApproved > raftState.getPersistentState().getTotalNodes() / 2) {
            log.info("Received votes from majority of nodes, become LEADER");
            raftState.toLeader();
            var lastPosition = logManager.lastPosition();

            // [Docs]: for each server, index of the next log entry to send to that server (initialized to leader last log index + 1)
            var nextPosition = new Position(lastPosition.term(), lastPosition.index() + 1);
            for (int nodeId = 0; nodeId < clusterProperty.getNodes().size(); nodeId++) {
                if (nodeId == raftState.getPersistentState().getNodeId().id()) {
                    continue;
                }
                var node = new NodeId(nodeId);
                raftState.getLeaderVolatileState().getMatchPositions().put(node, INITIAL_POSITION.copy());
                raftState.getLeaderVolatileState().getNextAppendPositions().put(node, nextPosition.copy());
            }

            this.broadcastService.broadcastHeartbeat();
            raftState.setHeartbeatTimeout(System.currentTimeMillis() + clusterProperty.getHeartbeatIntervalMs());
        }
    }
}

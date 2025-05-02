package gc.garcol.caferaft.core.service;

import gc.garcol.caferaft.application.network.cluster.ClusterRpcNetworkOutbound;
import gc.garcol.caferaft.core.constant.ClusterProperty;
import gc.garcol.caferaft.core.log.LogManager;
import gc.garcol.caferaft.core.repository.ClusterStateRepository;
import gc.garcol.caferaft.core.rpc.*;
import gc.garcol.caferaft.core.state.PersistentState;
import gc.garcol.caferaft.core.state.RaftRole;
import gc.garcol.caferaft.core.state.RaftState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;

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

    @Override
    public void handleClusterRpc(ClusterRpc clusterRpc) {

        preCheckRpc(clusterRpc);

        switch (clusterRpc) {
            case AppendEntryRequest request -> this.handleAppendEntryRequest(request);
            case VoteRequest request -> this.handleVoteRequest(request);
            case AppendEntryResponse response -> this.handleAppendEntryResponse(response);
            case VoteResponse response -> this.handleVoteResponse(response);
            default -> log.error("Received unknown cluster rpc: {}", clusterRpc);
        }
    }

    private void preCheckRpc(ClusterRpc clusterRpc) {
        // If RPC request or response contains term T > currentTerm:
        // set currentTerm = T, convert to follower (§5.1)
        if (clusterRpc.getTerm() > raftState.getPersistentState().getCurrentTerm()) {
            raftState.toFollower();
            raftState.setHeartbeatTimeout(System.currentTimeMillis() + clusterProperty.getHeartbeatTimeoutMs());
            PersistentState persistentState = raftState.getPersistentState();
            persistentState.setVotedFor(null);
            persistentState.setCurrentTerm(clusterRpc.getTerm());
            clusterStateRepository.save(persistentState);
        }
    }

    private void handleAppendEntryRequest(AppendEntryRequest request) {
        log.info("Received AppendEntryRequest {}", request);
    }

    private void handleVoteRequest(VoteRequest request) {
        log.info("Received VoteRequest {}", request);

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

        // If votedFor is null or candidateId, and candidate’s log is at
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

    private void handleAppendEntryResponse(AppendEntryResponse response) {
        log.info("Received AppendEntryResponse {}", response);
    }

    private void handleVoteResponse(VoteResponse response) {
        log.info("Received VoteResponse {}", response);

        if (!RaftRole.CANDIDATE.equals(raftState.getRole())) {
            return;
        }

        var quorum = raftState.getCandidateVolatileState().getVotedQuorum();
        quorum.put(response.getSender(), response.isVoteGranted());

        var totalApproved = quorum.values().stream().filter(Boolean::booleanValue).count();

        // If votes received from majority of servers: become leader
        if (totalApproved > raftState.getPersistentState().getTotalNodes() / 2) {
            log.info("Received votes from majority of nodes, become LEADER");
            raftState.toLeader();
        }
    }
}

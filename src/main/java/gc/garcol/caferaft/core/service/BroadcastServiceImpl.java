package gc.garcol.caferaft.core.service;

import gc.garcol.caferaft.application.network.cluster.ClusterRpcNetworkOutbound;
import gc.garcol.caferaft.core.constant.ClusterProperty;
import gc.garcol.caferaft.core.log.LogManager;
import gc.garcol.caferaft.core.log.Position;
import gc.garcol.caferaft.core.rpc.AppendEntryRequest;
import gc.garcol.caferaft.core.rpc.ClusterRpc;
import gc.garcol.caferaft.core.rpc.VoteRequest;
import gc.garcol.caferaft.core.state.NodeId;
import gc.garcol.caferaft.core.state.RaftState;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author thaivc
 * @since 2025
 */
@RequiredArgsConstructor
public class BroadcastServiceImpl implements BroadcastService {
    private final ClusterProperty clusterProperty;
    private final Executor commonExecutorPool;
    private final RaftState raftState;
    private final LogManager logManager;
    private final ClusterRpcNetworkOutbound clusterRpcNetworkOutbound;

    @Override
    public <T extends ClusterRpc> void broadcast(T rpc, BiConsumer<NodeId, T> consumer) {
        for (int nodeId = 0; nodeId < clusterProperty.getNodes().size(); nodeId++) {
            if (nodeId == raftState.getPersistentState().getNodeId().id()) {
                continue;
            }

            final NodeId targetNode = new NodeId(nodeId);
            commonExecutorPool.execute(() -> consumer.accept(targetNode, rpc));
        }
    }

    @Override
    public void broadcast(Consumer<NodeId> consumer) {
        for (int nodeId = 0; nodeId < clusterProperty.getNodes().size(); nodeId++) {
            if (nodeId == raftState.getPersistentState().getNodeId().id()) {
                continue;
            }

            consumer.accept(new NodeId(nodeId));
        }
    }

    @Override
    public void broadcastVote() {
        Position lastPosition = logManager.lastPosition();
        long currentTerm = raftState.getPersistentState().getCurrentTerm();

        VoteRequest voteRequest = VoteRequest.builder()
            .term(currentTerm)
            .candidateId(raftState.getPersistentState().getNodeId())
            .sender(raftState.getPersistentState().getNodeId())
            .lastPosition(lastPosition)
            .build();

        this.broadcast(voteRequest, clusterRpcNetworkOutbound::voteRequest);
    }

    @Override
    public void broadcastHeartbeat() {
        AppendEntryRequest appendEntryRequest = new AppendEntryRequest();
        appendEntryRequest.setSender(raftState.getPersistentState().getNodeId());
        appendEntryRequest.setTerm(raftState.getPersistentState().getCurrentTerm());
        appendEntryRequest.setPreviousPosition(logManager.lastPosition());
        appendEntryRequest.setLeaderCommit(raftState.getVolatileState().getCommitPosition());
        this.broadcast(appendEntryRequest, clusterRpcNetworkOutbound::appendEntryRequest);
    }
}

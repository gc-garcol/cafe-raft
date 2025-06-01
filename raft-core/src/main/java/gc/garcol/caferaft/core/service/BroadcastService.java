package gc.garcol.caferaft.core.service;

import gc.garcol.caferaft.core.rpc.ClusterRpc;
import gc.garcol.caferaft.core.state.NodeId;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author thaivc
 * @since 2025
 */
public interface BroadcastService {
    <T extends ClusterRpc> void broadcast(T rpc, BiConsumer<NodeId, T> consumer);

    void broadcast(Consumer<NodeId> consumer);

    void broadcastVote();

    void broadcastHeartbeat();
}

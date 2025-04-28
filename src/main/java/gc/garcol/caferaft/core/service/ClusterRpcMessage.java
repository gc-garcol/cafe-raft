package gc.garcol.caferaft.core.service;

import gc.garcol.caferaft.core.rpc.ClusterRpc;

/**
 * @author thaivc
 * @since 2025
 */
public interface ClusterRpcMessage<T extends ClusterRpc> extends Message<T> {
}

package gc.garcol.caferaft.core.rpc;

import gc.garcol.caferaft.core.state.NodeId;

/**
 * @author thaivc
 * @since 2025
 */
public interface RpcNetworkOutbound {

    void appendEntryRequest(NodeId nodeId, AppendEntryRequest request);

    void appendEntryResponse(NodeId nodeId, AppendEntryResponse response);

    void voteRequest(NodeId nodeId, VoteRequest request);

    void voteResponse(NodeId nodeId, VoteResponse response);

}

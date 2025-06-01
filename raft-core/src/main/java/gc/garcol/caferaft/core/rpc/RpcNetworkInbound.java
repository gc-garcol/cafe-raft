package gc.garcol.caferaft.core.rpc;

/**
 * @author thaivc
 * @since 2025
 */
public interface RpcNetworkInbound {

    void appendEntryRequest(AppendEntryRequest request);

    void appendEntryResponse(AppendEntryResponse response);

    void voteRequest(VoteRequest request);

    void voteResponse(VoteResponse response);

}

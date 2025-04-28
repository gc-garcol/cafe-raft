package gc.garcol.caferaft.core.service;

import gc.garcol.caferaft.core.rpc.AppendEntryRequest;
import gc.garcol.caferaft.core.rpc.AppendEntryResponse;
import gc.garcol.caferaft.core.rpc.ClusterRpc;
import gc.garcol.caferaft.core.rpc.VoteRequest;
import gc.garcol.caferaft.core.rpc.VoteResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * @author thaivc
 * @since 2025
 */
@Slf4j
public class ClusterRpcHandlerImpl implements ClusterRpcHandler {
    @Override
    public void handleClusterRpc(ClusterRpc clusterRpc) {
        switch (clusterRpc) {
            case AppendEntryRequest request -> this.handleAppendEntryRequest(request);
            case VoteRequest request -> this.handleVoteRequest(request);
            case AppendEntryResponse response -> this.handleAppendEntryResponse(response);
            case VoteResponse response -> this.handleVoteResponse(response);
            default -> log.error("Received unknown cluster rpc: {}", clusterRpc);
        }
    }

    private void handleAppendEntryRequest(AppendEntryRequest request) {
        log.info("Received append entry request from node {}", request.getSender());
    }

    private void handleVoteRequest(VoteRequest request) {
        log.info("Received vote request from node {}", request.getSender());
    }

    private void handleAppendEntryResponse(AppendEntryResponse response) {
        log.info("Received append entry response from node {}", response.getSender());
    }

    private void handleVoteResponse(VoteResponse response) {
        log.info("Received vote response from node {}", response.getSender());
    }
}

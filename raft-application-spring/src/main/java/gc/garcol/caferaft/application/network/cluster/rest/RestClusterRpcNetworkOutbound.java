package gc.garcol.caferaft.application.network.cluster.rest;

import gc.garcol.caferaft.core.constant.ClusterProperty;
import gc.garcol.caferaft.core.rpc.*;
import gc.garcol.caferaft.core.state.NodeId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * @author thaivc
 * @since 2025
 */
@Slf4j
@Component
@Profile("rpc-rest")
@RequiredArgsConstructor
public class RestClusterRpcNetworkOutbound implements RpcNetworkOutbound {

    private final ClusterProperty clusterProperties;
    private final WebClient       webClient;

    @Override
    public void appendEntryRequest(NodeId nodeId, AppendEntryRequest request) {
        sendRequest(nodeId, request, "/rpc/append-entry-request");
    }

    @Override
    public void appendEntryResponse(NodeId nodeId, AppendEntryResponse response) {
        sendRequest(nodeId, response, "/rpc/append-entry-response");
    }

    @Override
    public void voteRequest(NodeId nodeId, VoteRequest request) {
        sendRequest(nodeId, request, "/rpc/vote-request");
    }

    @Override
    public void voteResponse(NodeId nodeId, VoteResponse response) {
        sendRequest(nodeId, response, "/rpc/vote-response");
    }

    private <T> void sendRequest(NodeId nodeId, T payload, String endpoint) {
        String url = getNodeBaseUrl(nodeId) + endpoint;
        webClient.post().uri(url).contentType(MediaType.APPLICATION_JSON).bodyValue(payload).retrieve()
                .toBodilessEntity().onErrorResume(e -> {
                    log.debug("Error on sending request to nodeId: {}, payload: {}, error: {}", nodeId, payload,
                            e.getMessage());
                    return Mono.empty();
                }).subscribe();
    }

    private String getNodeBaseUrl(NodeId nodeId) {
        return clusterProperties.getNodes().get(nodeId.id());
    }
}

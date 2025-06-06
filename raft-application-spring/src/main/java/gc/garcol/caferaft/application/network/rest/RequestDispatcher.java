package gc.garcol.caferaft.application.network.rest;

import gc.garcol.caferaft.application.payload.BaseClientMessage;
import gc.garcol.caferaft.core.client.ClientRequest;
import gc.garcol.caferaft.core.client.ClientResponse;
import gc.garcol.caferaft.core.client.CommonErrorResponse;
import gc.garcol.caferaft.core.constant.ClusterProperty;
import gc.garcol.caferaft.core.service.RaftMessageCoordinator;
import gc.garcol.caferaft.core.state.RaftRole;
import gc.garcol.caferaft.core.state.RaftState;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author thaivc
 * @since 2025
 */
@Service
@RequiredArgsConstructor
public class RequestDispatcher {

    private final RaftMessageCoordinator raftMessageCoordinator;
    private final RaftState              raftState;
    private final ClusterProperty        clusterProperty;
    private final WebClient              webClient;

    @Value("${client-request-timeout-ms}")
    private long clientRequestTimeout;

    public <T extends ClientRequest> CompletableFuture<?> dispatch(ServerWebExchange exchange, T request) {
        if (!RaftRole.LEADER.equals(raftState.getRole()) && raftState.getLeaderId() != null) {
            return forwardRequest(exchange, request);
        }

        UUID correlationId = UUID.randomUUID();
        CompletableFuture<ClientResponse> responseFuture = new CompletableFuture<>();
        BaseClientMessage<T> message = new BaseClientMessage<>(correlationId, responseFuture, request);

        if (!raftMessageCoordinator.publish(message)) {
            return responseFuture.completeAsync(() -> new CommonErrorResponse(503, "Queue is full"));
        }

        return responseFuture.completeOnTimeout(new CommonErrorResponse(408, "Timeout"), clientRequestTimeout,
                TimeUnit.MILLISECONDS);
    }

    private <T extends ClientRequest> CompletableFuture<?> forwardRequest(ServerWebExchange exchange, T body) {
        String url = clusterProperty.getNodes().get(raftState.leaderId.id()) + exchange.getRequest().getURI().getPath();

        return webClient.method(exchange.getRequest().getMethod()).uri(url)
                .headers(headers -> headers.addAll(exchange.getRequest().getHeaders()))
                .contentType(MediaType.APPLICATION_JSON).bodyValue(body).retrieve().bodyToMono(Object.class).toFuture();
    }
}

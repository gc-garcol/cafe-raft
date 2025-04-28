package gc.garcol.caferaft.application.network.rest;

import gc.garcol.caferaft.application.payload.BaseClientMessage;
import gc.garcol.caferaft.core.client.ClientRequest;
import gc.garcol.caferaft.core.client.ClientResponse;
import gc.garcol.caferaft.core.client.CommonErrorResponse;
import gc.garcol.caferaft.core.service.RaftMessageCoordinator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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

    @Value("${client-request-timeout-ms}")
    private long clientRequestTimeout;

    public <T extends ClientRequest> CompletableFuture<ClientResponse> dispatch(T command) {
        UUID correlationId = UUID.randomUUID();
        CompletableFuture<ClientResponse> responseFuture = new CompletableFuture<>();
        BaseClientMessage<T> message = new BaseClientMessage<>(correlationId, responseFuture, command);

        if (!raftMessageCoordinator.publish(message)) {
            return responseFuture.completeAsync(
                () -> new CommonErrorResponse(HttpStatus.SERVICE_UNAVAILABLE.value(), "Queue is full")
            );
        }

        return responseFuture
            .completeOnTimeout(
                new CommonErrorResponse(HttpStatus.REQUEST_TIMEOUT.value(), "Timeout"),
                clientRequestTimeout,
                TimeUnit.MILLISECONDS
            );
    }
}

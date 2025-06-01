package gc.garcol.caferaft.application.network.rest;

import gc.garcol.caferaft.application.payload.BaseClientMessage;
import gc.garcol.caferaft.core.client.ClientRequest;
import gc.garcol.caferaft.core.client.ClientResponse;
import gc.garcol.caferaft.core.client.CommonErrorResponse;
import gc.garcol.caferaft.core.constant.ClusterProperty;
import gc.garcol.caferaft.core.service.RaftMessageCoordinator;
import gc.garcol.caferaft.core.state.RaftRole;
import gc.garcol.caferaft.core.state.RaftState;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
    private final RestTemplate           restTemplate = new RestTemplate();

    @Value("${client-request-timeout-ms}")
    private long clientRequestTimeout;

    public <T extends ClientRequest> CompletableFuture<?> dispatch(HttpServletRequest httpRequest, T request) {

        if (!RaftRole.LEADER.equals(raftState.getRole()) && raftState.getLeaderId() != null) {
            return forwardRequest(httpRequest, request);
        }

        UUID correlationId = UUID.randomUUID();
        CompletableFuture<ClientResponse> responseFuture = new CompletableFuture<>();
        BaseClientMessage<T> message = new BaseClientMessage<>(correlationId, responseFuture, request);

        if (!raftMessageCoordinator.publish(message)) {
            return responseFuture.completeAsync(
                    () -> new CommonErrorResponse(HttpStatus.SERVICE_UNAVAILABLE.value(), "Queue is full"));
        }

        return responseFuture.completeOnTimeout(new CommonErrorResponse(HttpStatus.REQUEST_TIMEOUT.value(), "Timeout"),
                clientRequestTimeout, TimeUnit.MILLISECONDS);
    }

    private <T extends ClientRequest> CompletableFuture<?> forwardRequest(HttpServletRequest httpRequest, T body) {
        String url = clusterProperty.getNodes().get(raftState.leaderId.id()) + httpRequest.getRequestURI();

        HttpHeaders headers = new HttpHeaders();
        // todo forward header values

        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<?> requestEntity = HttpMethod.valueOf(httpRequest.getMethod()).equals(HttpMethod.GET) ?
                new HttpEntity<>(headers) :
                new HttpEntity<>(body, headers);

        ResponseEntity<?> response = restTemplate.exchange(url, HttpMethod.valueOf(httpRequest.getMethod()),
                requestEntity, new ParameterizedTypeReference<>() {
                });

        return CompletableFuture.completedFuture(response.getBody());
    }
}

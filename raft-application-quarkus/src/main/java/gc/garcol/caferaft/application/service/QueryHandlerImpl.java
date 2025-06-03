package gc.garcol.caferaft.application.service;

import gc.garcol.caferaft.core.async.ExecutorEventPublisher;
import gc.garcol.caferaft.core.client.ClientResponse;
import gc.garcol.caferaft.core.client.CommonErrorResponse;
import gc.garcol.caferaft.core.client.Query;
import gc.garcol.caferaft.core.service.QueryHandler;
import gc.garcol.caferaft.core.service.StateMachine;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * @author thaivc
 * @since 2025
 */
@Slf4j
@ApplicationScoped
public class QueryHandlerImpl implements QueryHandler {

    @Inject
    private StateMachine           stateMachine;
    @Inject
    private ExecutorEventPublisher replyPublisher;

    @Override
    public void handleRequest(Query query, CompletableFuture<ClientResponse> responseReplier) {
        try {
            log.debug("Received query: {}", query);
            var response = stateMachine.apply(query);
            replyPublisher.publish(() -> responseReplier.complete(response));
        } catch (IllegalArgumentException e) {
            replyPublisher.publish(() -> responseReplier.complete(new CommonErrorResponse(400, e.getMessage())));
        } catch (Exception e) {
            replyPublisher.publish(() -> responseReplier.complete(new CommonErrorResponse(500, e.getMessage())));
        }
    }
} 
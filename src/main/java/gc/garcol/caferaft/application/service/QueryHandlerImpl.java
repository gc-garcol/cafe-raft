package gc.garcol.caferaft.application.service;

import gc.garcol.caferaft.core.async.ExecutorEventPublisher;
import gc.garcol.caferaft.core.client.ClientResponse;
import gc.garcol.caferaft.core.client.CommonErrorResponse;
import gc.garcol.caferaft.core.client.Query;
import gc.garcol.caferaft.core.service.QueryHandler;
import gc.garcol.caferaft.core.service.StateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * @author thaivc
 * @since 2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QueryHandlerImpl implements QueryHandler {

    private final StateMachine stateMachine;
    private final ExecutorEventPublisher replyPublisher;

    @Override
    public void handleRequest(Query query, CompletableFuture<ClientResponse> responseReplier) {
        try {
            log.debug("Received query: {}", query);
            var response = stateMachine.apply(query);
            replyPublisher.publish(() -> responseReplier.complete(response));
        } catch (IllegalArgumentException e) {
            replyPublisher.publish(() -> responseReplier.complete(new CommonErrorResponse(
                400,
                e.getMessage()
            )));
        } catch (Exception e) {
            replyPublisher.publish(() -> responseReplier.complete(new CommonErrorResponse(
                500,
                e.getMessage()
            )));
        }
    }
}

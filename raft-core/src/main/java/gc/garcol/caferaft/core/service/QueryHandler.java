package gc.garcol.caferaft.core.service;

import gc.garcol.caferaft.core.client.ClientResponse;
import gc.garcol.caferaft.core.client.Query;

import java.util.concurrent.CompletableFuture;

/**
 * @author thaivc
 * @since 2025
 */
public interface QueryHandler {

    void handleRequest(Query query, CompletableFuture<ClientResponse> responseReplier);

}

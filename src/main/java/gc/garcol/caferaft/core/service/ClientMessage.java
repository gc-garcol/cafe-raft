package gc.garcol.caferaft.core.service;

import gc.garcol.caferaft.core.client.ClientRequest;
import gc.garcol.caferaft.core.client.ClientResponse;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author thaivc
 * @since 2025
 */
public interface ClientMessage<T extends ClientRequest> extends Message<T> {
    UUID correlationId();
    CompletableFuture<ClientResponse> replier();
}

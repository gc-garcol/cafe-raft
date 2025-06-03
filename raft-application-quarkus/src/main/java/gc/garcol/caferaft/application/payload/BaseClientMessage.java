package gc.garcol.caferaft.application.payload;

import gc.garcol.caferaft.core.client.ClientRequest;
import gc.garcol.caferaft.core.client.ClientResponse;
import gc.garcol.caferaft.core.service.ClientMessage;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author thaivc
 * @since 2025
 */
public record BaseClientMessage<T extends ClientRequest>(UUID correlationId, CompletableFuture<ClientResponse> replier,
                                                         T payload) implements ClientMessage<T> {
} 
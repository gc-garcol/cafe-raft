package gc.garcol.caferaft.core.service;

import gc.garcol.caferaft.core.client.ClientResponse;
import gc.garcol.caferaft.core.log.Position;

import java.util.concurrent.CompletableFuture;

/**
 * @author thaivc
 * @since 2025
 */
public record ClientReplier(
    Position position,
    CompletableFuture<ClientResponse> replier
) implements Comparable<ClientReplier> {
    @Override
    public int compareTo(ClientReplier other) {
        return this.position.compareTo(other.position);
    }
}

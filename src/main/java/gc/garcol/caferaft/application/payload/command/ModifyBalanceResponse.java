package gc.garcol.caferaft.application.payload.command;

import java.util.UUID;

/**
 * @author thaivc
 * @since 2025
 */
public record ModifyBalanceResponse(
    UUID correlationId,
    int status,
    String message
) {
}

package gc.garcol.caferaft.core.client;

/**
 * @author thaivc
 * @since 2025
 */
public record CommonErrorResponse(int code, String message) implements ClientResponse {
}

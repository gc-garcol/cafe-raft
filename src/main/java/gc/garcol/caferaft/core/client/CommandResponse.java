package gc.garcol.caferaft.core.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author thaivc
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandResponse implements ClientResponse {
    protected int code;
    protected String message;
}

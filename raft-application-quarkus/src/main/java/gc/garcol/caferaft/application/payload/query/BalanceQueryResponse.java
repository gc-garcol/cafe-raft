package gc.garcol.caferaft.application.payload.query;

import gc.garcol.caferaft.core.client.ClientResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

/**
 * @author thaivc
 * @since 2025
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceQueryResponse implements ClientResponse {
    private long id;

    @Builder.Default
    private BigInteger amount = BigInteger.ZERO;

    @Builder.Default
    private boolean active = false;
}

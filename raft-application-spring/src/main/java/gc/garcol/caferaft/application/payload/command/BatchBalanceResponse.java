package gc.garcol.caferaft.application.payload.command;

import gc.garcol.caferaft.core.client.CommandResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author thaivc
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BatchBalanceResponse extends CommandResponse {
    List<ModifyBalanceResponse> result;
}

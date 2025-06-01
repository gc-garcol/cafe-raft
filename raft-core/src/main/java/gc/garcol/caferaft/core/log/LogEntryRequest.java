package gc.garcol.caferaft.core.log;

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
public class LogEntryRequest {

    private Position position;
    private int      type;
    private byte[]   command;
}

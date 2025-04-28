package gc.garcol.caferaft.core.log;

import gc.garcol.caferaft.core.client.Command;
import lombok.Data;

/**
 * @author thaivc
 * @since 2025
 */
@Data
public class LogEntry {
    private Position position;
    private Command command;
}

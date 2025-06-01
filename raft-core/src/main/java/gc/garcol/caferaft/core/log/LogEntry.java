package gc.garcol.caferaft.core.log;

import gc.garcol.caferaft.core.client.Command;
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
public class LogEntry {
    private Position position;
    private Command  command;
}

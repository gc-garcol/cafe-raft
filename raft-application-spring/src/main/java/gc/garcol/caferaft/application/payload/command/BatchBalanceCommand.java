package gc.garcol.caferaft.application.payload.command;

import gc.garcol.caferaft.core.client.Command;
import lombok.Data;

import java.util.List;

/**
 * @author thaivc
 * @since 2025
 */
@Data
public class BatchBalanceCommand implements Command {
    private List<ModifyBalanceCommand> commands;
}

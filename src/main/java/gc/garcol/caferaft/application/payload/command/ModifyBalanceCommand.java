package gc.garcol.caferaft.application.payload.command;

import gc.garcol.caferaft.core.client.Command;
import lombok.Data;

import java.util.UUID;

/**
 * @author thaivc
 * @since 2025
 */
@Data
public class ModifyBalanceCommand implements Command {
    private UUID correlationId;
    private CreateBalanceCommand createBalanceCommand;
    private DepositCommand depositCommand;
    private TransferCommand transferCommand;
    private WithdrawCommand withdrawCommand;
}

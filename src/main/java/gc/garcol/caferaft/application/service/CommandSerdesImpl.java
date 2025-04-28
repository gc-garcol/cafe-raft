package gc.garcol.caferaft.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gc.garcol.caferaft.application.payload.command.CreateBalanceCommand;
import gc.garcol.caferaft.application.payload.command.DepositCommand;
import gc.garcol.caferaft.application.payload.command.TransferCommand;
import gc.garcol.caferaft.application.payload.command.WithdrawCommand;
import gc.garcol.caferaft.core.client.Command;
import gc.garcol.caferaft.core.client.CommandSerdes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author thaivc
 * @since 2025
 */
@Service
@RequiredArgsConstructor
public class CommandSerdesImpl implements CommandSerdes {

    private final ObjectMapper objectMapper;

    @Override
    public byte[] toBytes(Command command) {
        try {
            return objectMapper.writeValueAsBytes(command);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Command fromBytes(int type, byte[] bytes) {
        try {
            Class<?> clazz = switch (type) {
                case 0 -> CreateBalanceCommand.class;
                case 1 -> DepositCommand.class;
                case 2 -> WithdrawCommand.class;
                case 3 -> TransferCommand.class;
                default -> throw new IllegalArgumentException("Unknown command type: " + type);
            };
            return (Command) objectMapper.readValue(bytes, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int type(Command command) {
        return switch (command) {
            case CreateBalanceCommand createBalanceCommand -> 0;
            case DepositCommand depositCommand -> 1;
            case WithdrawCommand withdrawCommand -> 2;
            case TransferCommand transferCommand -> 3;
            default ->
                throw new IllegalArgumentException("Unknown command type: " + command.getClass().getSimpleName());
        };
    }
}

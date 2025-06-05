package gc.garcol.caferaft.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gc.garcol.caferaft.application.payload.command.*;
import gc.garcol.caferaft.core.client.Command;
import gc.garcol.caferaft.core.client.CommandSerdes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author thaivc
 * @since 2025
 */
@Service
@RequiredArgsConstructor
public class CommandSerdesImpl implements CommandSerdes {

    private static final Map<Integer, Class<? extends Command>> COMMAND_TYPES = Map.of(0, CreateBalanceCommand.class, 1,
            DepositCommand.class, 2, WithdrawCommand.class, 3, TransferCommand.class, 4, BatchBalanceCommand.class);
    private final        ObjectMapper                           objectMapper;

    @Override
    public byte[] toBytes(Command command) {
        if (command instanceof Marshallable marshallable) {
            return marshallable.toBytes();
        }
        try {
            return objectMapper.writeValueAsBytes(command);
        } catch (Exception e) {
            throw new CommandSerializationException("Failed to serialize command", e);
        }
    }

    @Override
    public Command fromBytes(int type, byte[] bytes) {
        if (type == 0) {
            return CreateBalanceCommand.fromBytes(bytes);
        }
        if (type == 1) {
            return DepositCommand.fromBytes(bytes);
        }
        if (type == 2) {
            return WithdrawCommand.fromBytes(bytes);
        }
        if (type == 3) {
            return TransferCommand.fromBytes(bytes);
        }

        try {
            Class<? extends Command> commandClass = COMMAND_TYPES.get(type);
            if (commandClass == null) {
                throw new IllegalArgumentException("Unknown command type: " + type);
            }
            return objectMapper.readValue(bytes, commandClass);
        } catch (Exception e) {
            throw new CommandSerializationException("Failed to deserialize command of type " + type, e);
        }
    }

    @Override
    public int type(Command command) {
        return COMMAND_TYPES.entrySet().stream().filter(entry -> entry.getValue().isInstance(command))
                .map(Map.Entry::getKey).findFirst().orElseThrow(() -> new IllegalArgumentException(
                        "Unknown command type: " + command.getClass().getSimpleName()));
    }

    private static class CommandSerializationException extends RuntimeException {
        public CommandSerializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

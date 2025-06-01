package gc.garcol.caferaft.application.payload.command;

import gc.garcol.caferaft.core.client.Command;

import java.math.BigInteger;

/**
 * @author thaivc
 * @since 2025
 */
public record TransferCommand(long fromId, long toId, BigInteger amount) implements Command {
}
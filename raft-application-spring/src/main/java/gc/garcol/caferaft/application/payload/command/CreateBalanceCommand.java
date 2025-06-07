package gc.garcol.caferaft.application.payload.command;

import gc.garcol.caferaft.core.client.Command;

import java.nio.ByteBuffer;

/**
 * @author thaivc
 * @since 2025
 */
public record CreateBalanceCommand(long id) implements Command, Marshallable {

    static final ByteBuffer writeBuffer = ByteBuffer.allocate(Long.BYTES);

    public static CreateBalanceCommand fromBytes(byte[] bytes) {
        ByteBuffer readBuffer = ByteBuffer.wrap(bytes);
        return new CreateBalanceCommand(readBuffer.getLong());
    }

    @Override
    public byte[] toBytes() {
        writeBuffer.clear();
        writeBuffer.putLong(id);
        writeBuffer.flip();
        byte[] result = new byte[writeBuffer.limit()];
        writeBuffer.get(result);
        return result;
    }
}

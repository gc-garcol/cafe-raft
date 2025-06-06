package gc.garcol.caferaft.application.payload.command;

import gc.garcol.caferaft.core.client.Command;

import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * @author thaivc
 * @since 2025
 */
public record TransferCommand(long fromId, long toId, BigInteger amount) implements Command, Marshallable {
    static final ByteBuffer writeBuffer = ByteBuffer.allocate(Long.BYTES + Long.BYTES + Long.BYTES * 4);

    public static TransferCommand fromBytes(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long fromId = byteBuffer.getLong();
        long toId = byteBuffer.getLong();
        byte[] amountBytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(amountBytes);
        BigInteger amount = new BigInteger(amountBytes);
        return new TransferCommand(fromId, toId, amount);
    }

    @Override
    public byte[] toBytes() {
        writeBuffer.clear();
        writeBuffer.putLong(fromId);
        writeBuffer.putLong(toId);
        writeBuffer.put(amount.toByteArray());
        writeBuffer.flip();
        byte[] bytes = new byte[writeBuffer.limit()];
        writeBuffer.get(bytes);
        return bytes;
    }
}

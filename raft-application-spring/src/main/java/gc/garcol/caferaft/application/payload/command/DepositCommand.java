package gc.garcol.caferaft.application.payload.command;

import gc.garcol.caferaft.core.client.Command;

import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * @author thaivc
 * @since 2025
 */
public record DepositCommand(long id, BigInteger amount) implements Command, Marshallable {
    static final ByteBuffer writeBuffer = ByteBuffer.allocate(Long.BYTES + Long.BYTES * 4);

    public static DepositCommand fromBytes(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long id = byteBuffer.getLong();
        byte[] amountBytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(amountBytes);
        BigInteger amount = new BigInteger(amountBytes);
        return new DepositCommand(id, amount);
    }

    @Override
    public byte[] toBytes() {
        writeBuffer.clear();
        writeBuffer.putLong(id);
        writeBuffer.put(amount.toByteArray());
        writeBuffer.flip();
        byte[] bytes = new byte[writeBuffer.limit()];
        writeBuffer.get(bytes);
        return bytes;
    }
}

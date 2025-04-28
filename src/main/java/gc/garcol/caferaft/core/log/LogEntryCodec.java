package gc.garcol.caferaft.core.log;

/**
 * @author thaivc
 * @since 2025
 */
public interface LogEntryCodec {
    LogEntry fromBytes(byte[] bytes);

    byte[] toBytes(LogEntry logEntry);
}

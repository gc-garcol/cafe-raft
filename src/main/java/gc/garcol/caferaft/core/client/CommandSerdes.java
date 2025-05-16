package gc.garcol.caferaft.core.client;

/**
 * @author thaivc
 * @since 2025
 */
public interface CommandSerdes {

    byte[] toBytes(Command command);

    Command fromBytes(int type, byte[] bytes);

    int type(Command command);

}

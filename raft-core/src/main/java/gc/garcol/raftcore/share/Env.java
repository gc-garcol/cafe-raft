package gc.garcol.raftcore.share;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * @author thaivc
 * @since 2024
 */
public class Env
{
    private static final Dotenv dotenv = Dotenv.load();

    public static final String DATA_DIR = dotenv.get("DATA_DIR");
    public static final String LOG_DIR = dotenv.get("LOG_DIR");
    public static final String METADATA_FILE = dotenv.get("METADATA_FILE");
}

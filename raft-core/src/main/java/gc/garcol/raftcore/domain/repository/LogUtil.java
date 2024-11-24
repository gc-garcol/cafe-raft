package gc.garcol.raftcore.domain.repository;

import gc.garcol.exchangecore.common.Env;
import gc.garcol.exchangecore.common.InternalException;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author thaivc
 * @since 2024
 */
public class LogUtil
{
    private static final int LONG_LENGTH = String.valueOf(Long.MAX_VALUE).length();
    private static final String LOG_FORMAT = "%0" + LONG_LENGTH + "d" + ".dat";
    private static final String INDEX_FORMAT = "%0" + LONG_LENGTH + "d" + ".index.dat";

    public static String logName(long segment)
    {
        return String.format(LOG_FORMAT, segment);
    }

    public static String logPath(long segment)
    {
        return Env.LOG_DIR + "/" + logName(segment);
    }

    public static String indexName(long segment)
    {
        return String.format(INDEX_FORMAT, segment);
    }

    public static String indexPath(long segment)
    {
        return Env.LOG_DIR + "/" + indexName(segment);
    }

    public static void createDirectoryNX(String pathStr)
    {
        Path path = Path.of(pathStr);
        if (!Files.exists(path))
        {
            try
            {
                Files.createDirectories(path);
            }
            catch (Exception e)
            {
                throw new InternalException(e);
            }
        }
    }

}

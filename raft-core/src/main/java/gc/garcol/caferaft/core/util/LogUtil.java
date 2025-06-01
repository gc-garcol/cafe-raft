package gc.garcol.caferaft.core.util;

import java.io.File;

/**
 * The `LogUtil` class provides utility methods for log file operations.
 * It includes methods to generate log and index file names based on a segment number,
 * and to create directories if they do not exist.
 *
 * <p> This class is thread-safe and ensures that the log directory is created if it does not exist.
 *
 * @author thaivc
 * @since 2024
 */
public class LogUtil {

    private static final int    LONG_LENGTH  = String.valueOf(Long.MAX_VALUE).length();
    private static final String LOG_FORMAT   = "%0" + LONG_LENGTH + "d" + ".data.dat";
    private static final String INDEX_FORMAT = "%0" + LONG_LENGTH + "d" + ".index.dat";

    private LogUtil() {
    }

    /**
     * Generates the log file name for the specified term.
     *
     * @param term the term number
     * @return the log file name
     */
    public static String segmentName(long term) {
        return String.format(LOG_FORMAT, term);
    }

    /**
     * Generates the index file name for the specified term.
     *
     * @param term the term number
     * @return the index file name
     */
    public static String indexName(long term) {
        return String.format(INDEX_FORMAT, term);
    }

    /**
     * Extracts the segment number from the specified file name.
     *
     * @param segmentName the file name
     * @return the segment number
     */
    public static long term(String segmentName) {
        return Long.parseLong(segmentName.substring(0, segmentName.indexOf('.')));
    }

    /**
     * Creates a directory if it does not exist.
     *
     * @param pathStr the path of the directory
     */
    public static void createDirectoryNX(String pathStr) {
        File dir = new File(pathStr);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

}
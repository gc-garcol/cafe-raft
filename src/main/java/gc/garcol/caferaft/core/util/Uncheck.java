package gc.garcol.caferaft.core.util;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

/**
 * @author thaivc
 * @since 2025
 */
@Slf4j
@NoArgsConstructor
public class Uncheck {

    public static void runSafe(UncheckRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            log.error("Run error", e);
        }
    }

    public static <T> T callSafe(Callable<T> callable, T defaultValue) {
        try {
            return callable.call();
        } catch (Exception e) {
            log.error("Run error", e);
            return defaultValue;
        }
    }

    public interface UncheckRunnable {
        void run() throws Exception;
    }
}

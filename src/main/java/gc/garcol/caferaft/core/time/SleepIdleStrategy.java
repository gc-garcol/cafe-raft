package gc.garcol.caferaft.core.time;

import lombok.SneakyThrows;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * @author thaivc
 * @since 2025
 */
public class SleepIdleStrategy implements IdleStrategy {
    private final Duration duration;

    public SleepIdleStrategy(long nanoSeconds) {
        this.duration = Duration.of(nanoSeconds, ChronoUnit.NANOS);
    }

    @SneakyThrows
    @Override
    public void idle() {
        Thread.sleep(duration);
    }
}

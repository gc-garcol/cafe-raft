package gc.garcol.caferaft.application.config.async;

import gc.garcol.caferaft.core.async.ExecutorEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author thaivc
 * @since 2025
 */
@Getter
@RequiredArgsConstructor
public class ExecutorEventWrapper {
    private final Object        source;
    private final ExecutorEvent executorEvent;
} 
package gc.garcol.caferaft.application.config.async;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * @author thaivc
 * @since 2025
 */
@ApplicationScoped
public class ExecutorEventConsumer {

    public void consume(ExecutorEventWrapper eventWrapper) {
        eventWrapper.getExecutorEvent().run();
    }
} 
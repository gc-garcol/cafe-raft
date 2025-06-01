package gc.garcol.caferaft.application.config.async;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author thaivc
 * @since 2025
 */
@Component
public class ExecutorEventConsumer {

    @Async("executor-event-pool")
    @EventListener
    public void consume(ExecutorEventWrapper eventWrapper) {
        eventWrapper.executorEvent.run();
    }

}

package gc.garcol.caferaft.application.config.async;

import gc.garcol.caferaft.core.async.ExecutorEvent;
import gc.garcol.caferaft.core.async.ExecutorEventPublisher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import lombok.RequiredArgsConstructor;

/**
 * @author thaivc
 * @since 2025
 */
@ApplicationScoped
@RequiredArgsConstructor
public class ExecutorEventPublisherImpl implements ExecutorEventPublisher {
    private final Event<ExecutorEventWrapper> publisher;

    @Override
    public void publish(ExecutorEvent executorEvent) {
        publisher.fire(new ExecutorEventWrapper(this, executorEvent));
    }
} 
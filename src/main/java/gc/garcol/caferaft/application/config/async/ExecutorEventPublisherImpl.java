package gc.garcol.caferaft.application.config.async;

import gc.garcol.caferaft.core.async.ExecutorEvent;
import gc.garcol.caferaft.core.async.ExecutorEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * @author thaivc
 * @since 2025
 */
@Component
@RequiredArgsConstructor
public class ExecutorEventPublisherImpl implements ExecutorEventPublisher {
    private final ApplicationEventPublisher publisher;

    @Override
    public void publish(ExecutorEvent executorEvent) {
        publisher.publishEvent(new ExecutorEventWrapper(this, executorEvent));
    }
}

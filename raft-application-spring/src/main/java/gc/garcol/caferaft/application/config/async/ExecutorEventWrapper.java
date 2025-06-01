package gc.garcol.caferaft.application.config.async;

import gc.garcol.caferaft.core.async.ExecutorEvent;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author thaivc
 * @since 2025
 */
@Getter
public class ExecutorEventWrapper extends ApplicationEvent {

    ExecutorEvent executorEvent;

    public ExecutorEventWrapper(Object source, ExecutorEvent executorEvent) {
        super(source);
        this.executorEvent = executorEvent;
    }
}

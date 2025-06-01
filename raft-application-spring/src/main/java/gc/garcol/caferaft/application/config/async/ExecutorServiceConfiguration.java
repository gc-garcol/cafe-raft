package gc.garcol.caferaft.application.config.async;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

/**
 * @author thaivc
 * @since 2025
 */
@Configuration
public class ExecutorServiceConfiguration {
    @Bean(name = "executor-event-pool")
    public TaskExecutor executorEventPool() {
        return Thread::startVirtualThread;
    }

    @Bean(name = "common-executor-pool")
    public TaskExecutor commonExecutorPool() {
        return Thread::startVirtualThread;
    }
}

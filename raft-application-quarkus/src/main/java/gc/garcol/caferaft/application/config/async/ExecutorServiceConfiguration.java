package gc.garcol.caferaft.application.config.async;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;

import java.util.concurrent.Executor;

/**
 * @author thaivc
 * @since 2025
 */
@ApplicationScoped
public class ExecutorServiceConfiguration {
    @Produces
    @ApplicationScoped
    @Named("executor-event-pool")
    public Executor executorEventPool() {
        return Thread::startVirtualThread;
    }

    @Produces
    @ApplicationScoped
    @Named("common-executor-pool")
    public Executor commonExecutorPool() {
        return Thread::startVirtualThread;
    }
} 
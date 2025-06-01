package gc.garcol.caferaft.application;

import gc.garcol.caferaft.core.service.ClusterBootstrap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author thaivc
 * @since 2025
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Bootstrap {

    private final ClusterBootstrap clusterBootstrap;

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        clusterBootstrap.start();
    }

    @EventListener(ContextClosedEvent.class)
    public void stop() {
        clusterBootstrap.stop();
    }
}

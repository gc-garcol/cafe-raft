package gc.garcol.caferaft.application;

import gc.garcol.caferaft.core.service.ClusterBootstrap;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author thaivc
 * @since 2025
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class Bootstrap {

    private final ClusterBootstrap clusterBootstrap;

    void onStart(@Observes StartupEvent ev) {
        clusterBootstrap.start();
    }

    void onStop(@Observes ShutdownEvent ev) {
        clusterBootstrap.stop();
    }
} 
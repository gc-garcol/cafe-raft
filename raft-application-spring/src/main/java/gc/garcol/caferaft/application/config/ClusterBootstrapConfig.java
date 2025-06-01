package gc.garcol.caferaft.application.config;

import gc.garcol.caferaft.core.constant.ClusterProperty;
import gc.garcol.caferaft.core.log.LogManager;
import gc.garcol.caferaft.core.repository.ClusterStateRepository;
import gc.garcol.caferaft.core.service.ClusterBootstrap;
import gc.garcol.caferaft.core.state.RaftState;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author thaivc
 * @since 2025
 */
@RequiredArgsConstructor
@Configuration
public class ClusterBootstrapConfig {
    private final Runnable               clusterWorker;
    private final RaftState              raftState;
    private final ClusterStateRepository clusterStateRepository;
    private final AtomicBoolean          appRunning;
    private final ClusterProperty        clusterProperty;
    private final LogManager             logManager;

    @Bean
    public ClusterBootstrap clusterBootstrap() {
        return new ClusterBootstrap(clusterWorker, raftState, clusterStateRepository, appRunning, clusterProperty,
                logManager);
    }
}

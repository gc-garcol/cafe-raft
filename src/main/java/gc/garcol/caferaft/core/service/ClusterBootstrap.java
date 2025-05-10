package gc.garcol.caferaft.core.service;

import gc.garcol.caferaft.core.constant.ClusterProperty;
import gc.garcol.caferaft.core.log.LogManager;
import gc.garcol.caferaft.core.repository.ClusterStateRepository;
import gc.garcol.caferaft.core.state.PersistentState;
import gc.garcol.caferaft.core.state.RaftState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author thaivc
 * @since 2025
 */
@Slf4j
@RequiredArgsConstructor
public class ClusterBootstrap {
    private final Runnable clusterWorker;
    private final RaftState raftState;
    private final ClusterStateRepository clusterStateRepository;
    private final AtomicBoolean appRunning;
    private final ClusterProperty clusterProperty;
    private final LogManager logManager;

    public void start() {
        log.info("Starting Node-{}", clusterProperty.getNodeId());

        this.logManager.loadSegments();
        this.loadPersistentState();
        new Thread(clusterWorker).start();

        log.info("Node-{} started", clusterProperty.getNodeId());
    }

    public void stop() {
        log.info("Node-{} stopping", clusterProperty.getNodeId());
        appRunning.set(false);
    }

    private void loadPersistentState() {
        PersistentState persistentState = clusterStateRepository.load();
        raftState.setPersistentState(persistentState);
    }
}

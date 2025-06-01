package gc.garcol.caferaft.core.repository;

import gc.garcol.caferaft.core.state.PersistentState;

/**
 * @author thaivc
 * @since 2025
 */
public interface ClusterStateRepository {
    PersistentState load();

    void save(PersistentState persistentState);
}

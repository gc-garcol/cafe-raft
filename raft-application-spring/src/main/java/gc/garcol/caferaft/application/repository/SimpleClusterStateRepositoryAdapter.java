package gc.garcol.caferaft.application.repository;

import gc.garcol.caferaft.core.constant.ClusterProperty;
import gc.garcol.caferaft.core.repository.SimpleClusterStateRepository;
import org.springframework.stereotype.Repository;

/**
 * @author thaivc
 * @since 2025
 */
@Repository
public class SimpleClusterStateRepositoryAdapter extends SimpleClusterStateRepository {
    public SimpleClusterStateRepositoryAdapter(ClusterProperty clusterProperty) {
        super(clusterProperty);
    }
}

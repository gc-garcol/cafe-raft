package gc.garcol.caferaft.application.repository;

import gc.garcol.caferaft.core.constant.ClusterProperty;
import gc.garcol.caferaft.core.repository.SimpleClusterStateRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * @author thaivc
 * @since 2025
 */
@ApplicationScoped
public class SimpleClusterStateRepositoryAdapter extends SimpleClusterStateRepository {
    public SimpleClusterStateRepositoryAdapter(ClusterProperty clusterProperty) {
        super(clusterProperty);
    }
} 
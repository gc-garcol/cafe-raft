package gc.garcol.caferaft.application.repository;

import gc.garcol.caferaft.core.client.CommandSerdes;
import gc.garcol.caferaft.core.constant.ClusterProperty;
import gc.garcol.caferaft.core.repository.SimpleLogRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * @author thaivc
 * @since 2025
 */
@ApplicationScoped
public class SimpleLogRepositoryAdapter extends SimpleLogRepository {
    public SimpleLogRepositoryAdapter(ClusterProperty clusterProperty, CommandSerdes commandSerdes) {
        super(clusterProperty, commandSerdes);
    }
} 
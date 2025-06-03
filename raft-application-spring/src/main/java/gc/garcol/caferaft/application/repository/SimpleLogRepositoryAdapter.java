package gc.garcol.caferaft.application.repository;

import gc.garcol.caferaft.core.client.CommandSerdes;
import gc.garcol.caferaft.core.constant.ClusterProperty;
import gc.garcol.caferaft.core.repository.SimpleLogRepository;
import org.springframework.stereotype.Repository;

/**
 * @author thaivc
 * @since 2025
 */
@Repository
public class SimpleLogRepositoryAdapter extends SimpleLogRepository {
    public SimpleLogRepositoryAdapter(ClusterProperty clusterProperty, CommandSerdes commandSerdes) {
        super(clusterProperty, commandSerdes);
    }
}

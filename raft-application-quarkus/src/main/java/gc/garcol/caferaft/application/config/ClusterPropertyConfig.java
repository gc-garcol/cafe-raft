package gc.garcol.caferaft.application.config;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;

/**
 * @author thaivc
 * @since 2025
 */
@Getter
@Setter
@ApplicationScoped
public class ClusterPropertyConfig {

    @ConfigProperty(name = "cluster.properties.queue-size")
    int queueSize;
    
    @ConfigProperty(name = "cluster.properties.idle-strategy")
    String idleStrategy;

    @ConfigProperty(name = "cluster.properties.nodeId")
    int nodeId;

    @ConfigProperty(name = "cluster.properties.nodes")
    List<String> nodes;

    @ConfigProperty(name = "cluster.properties.base-disk")
    String baseDisk;

    @ConfigProperty(name = "cluster.properties.electionTimeoutMs")
    int[] electionTimeoutMs;

    @ConfigProperty(name = "cluster.properties.heartbeatTimeoutMs")
    int heartbeatTimeoutMs;

    @ConfigProperty(name = "cluster.properties.heartbeatIntervalMs")
    int heartbeatIntervalMs;

    @ConfigProperty(name = "cluster.properties.nextSendLogTimeoutMs")
    int nextSendLogTimeoutMs;

    @ConfigProperty(name = "cluster.properties.messageBatchSize")
    int messageBatchSize;

    @ConfigProperty(name = "cluster.properties.commitedLogBatchSize")
    int commitedLogBatchSize;

    @ConfigProperty(name = "cluster.properties.appendLogBatchSize")
    int appendLogBatchSize;
} 
package gc.garcol.caferaft.application.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author thaivc
 * @since 2025
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cluster.properties")
public class ClusterPropertyConfig {

    private int    queueSize;
    private String idleStrategy;

    private int          nodeId;
    private List<String> nodes;
    private String       baseDisk;

    private int[] electionTimeoutMs;
    private int   heartbeatTimeoutMs;
    private int   heartbeatIntervalMs;
    private int   nextSendLogTimeoutMs;

    private int messageBatchSize;
    private int commitedLogBatchSize;
    private int appendLogBatchSize;
}

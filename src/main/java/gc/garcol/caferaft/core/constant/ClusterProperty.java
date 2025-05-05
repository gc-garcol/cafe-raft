package gc.garcol.caferaft.core.constant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author thaivc
 * @since 2025
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClusterProperty {
    private int queueSize;
    private String idleStrategy;

    private int nodeId;
    private List<String> nodes;
    private String baseDisk;

    private int[] electionTimeoutMs;
    private int heartbeatTimeoutMs;
    private int heartbeatIntervalMs;
    private int commitedLogBatchSize;
    private int appendLogBatchSize;
}

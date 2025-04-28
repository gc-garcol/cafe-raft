package gc.garcol.caferaft.core.rpc;

import gc.garcol.caferaft.core.state.NodeId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author thaivc
 * @since 2025
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteResponse implements ClusterRpc {

    private NodeId sender;

    private long term;

    private boolean voteGranted;

}

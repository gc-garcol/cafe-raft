package gc.garcol.caferaft.core.rpc;

import gc.garcol.caferaft.core.log.Position;
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
public class AppendEntryResponse implements ClusterRpc {

    private NodeId sender;

    private long term;

    private boolean success;

    // if success, then use matchedPosition in order to update leader-volatile-state
    private Position matchedPosition;
}

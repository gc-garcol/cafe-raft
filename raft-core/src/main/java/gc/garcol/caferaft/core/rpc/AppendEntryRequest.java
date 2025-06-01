package gc.garcol.caferaft.core.rpc;

import gc.garcol.caferaft.core.log.LogEntryRequest;
import gc.garcol.caferaft.core.log.Position;
import gc.garcol.caferaft.core.state.NodeId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an AppendEntries RPC request in the Raft consensus algorithm.
 * This request is sent by the leader to replicate log entries and to provide a form of heartbeat.
 * <p>
 * The AppendEntries RPC is used to: <br>
 * 1. Replicate log entries to followers <br>
 * 2. Ensure consistency of logs across the cluster <br>
 * 3. Provide a heartbeat mechanism to maintain leadership <br>
 *
 * @author thaivc
 * @since 2025
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppendEntryRequest implements ClusterRpc {

    private NodeId sender;

    /**
     * Leader's current term number.
     * Used by followers to update their term if they are behind.
     */
    private long term;

    /**
     * Position of the log entry immediately preceding the new entries.
     * Used to ensure log consistency between leader and follower.
     */
    private Position previousPosition;

    /**
     * [Docs]: log entries to store (empty for heartbeat;
     * may send more than one for efficiency)
     */
    @Builder.Default
    private List<LogEntryRequest> entries = new ArrayList<>();

    /**
     * Leader's commit position.
     * Used to inform followers which entries have been committed and can be applied to their state machines.
     */
    private Position leaderCommit;

    public boolean isHeartbeat() {
        return entries.isEmpty();
    }
}

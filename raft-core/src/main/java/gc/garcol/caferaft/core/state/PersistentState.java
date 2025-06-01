package gc.garcol.caferaft.core.state;

import lombok.Data;

/**
 * Represents the persistent state of a Raft node that is stored on stable storage
 * (e.g., disk) to survive server crashes and restarts.
 * <p>
 * This state must be updated on stable storage before responding to RPCs.
 */
@Data
public class PersistentState {
    /**
     * Unique identifier for this node in the cluster
     */
    NodeId nodeId;

    int totalNodes;

    /**
     * latest term server has seen (initialized to 0
     * on first boot, increases monotonically)
     */
    long currentTerm = 0;

    /**
     * CandidateId that received vote in current term (or null if none)
     */
    NodeId votedFor;
}

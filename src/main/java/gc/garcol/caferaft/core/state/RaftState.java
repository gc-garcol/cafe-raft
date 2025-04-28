package gc.garcol.caferaft.core.state;

import lombok.Data;

/**
 * Represents the complete state of a Raft node, containing all necessary information
 * for the Raft consensus algorithm to function properly.
 * <p>
 * The state is divided into three main components: <br>
 * 1. Persistent state - survives server restarts <br>
 * 2. Volatile state - maintained in memory <br>
 * 3. Leader volatile state - maintained only by the leader in memory <br>
 *
 * @author thaivc
 * @since 2025
 */
@Data
public class RaftState {

    RaftRole role = RaftRole.FOLLOWER;

    long heartbeatTimeout;
    long electionTimeout;

    /**
     * The persistent state component that survives server restarts
     */
    PersistentState persistentState = new PersistentState();

    /**
     * The volatile state component maintained in memory
     */
    VolatileState volatileState = new VolatileState();

    /**
     * The leader-specific volatile state, only maintained by the leader
     */
    LeaderVolatileState leaderVolatileState = null;
}


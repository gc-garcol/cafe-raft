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

    public volatile RaftRole role = RaftRole.FOLLOWER; // since expose health
    public volatile NodeId   leaderId; // since expose health

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
     * The leader-specific volatile state, only maintained by the leader in memory
     */
    LeaderVolatileState leaderVolatileState = null;

    /**
     * The candidate-specific volatile state, only maintained by the candidate in memory
     */
    CandidateVolatileState candidateVolatileState = null;

    public void toFollower() {
        role = RaftRole.FOLLOWER;
        leaderVolatileState = null;
        candidateVolatileState = null;
        leaderId = null;
    }

    public void toLeader() {
        role = RaftRole.LEADER;
        leaderVolatileState = new LeaderVolatileState();
        candidateVolatileState = null;
        leaderId = this.getPersistentState().nodeId;
    }

    public void toCandidate() {
        role = RaftRole.CANDIDATE;
        candidateVolatileState = new CandidateVolatileState();
        leaderVolatileState = null;
        leaderId = null;
    }
}


package gc.garcol.caferaft.core.state;

import gc.garcol.caferaft.core.log.Position;
import lombok.Data;

import java.util.Map;

/**
 * Represents the volatile state that is maintained only by the leader.
 * This state is reinitialized after election.
 */
@Data
public class LeaderVolatileState {

    /**
     * For each server, index of the next log entry to send to that server.
     * Initialized to leader's last log index + 1.
     * <p>
     * This is used to track the next log entry that should be sent to each follower
     * during log replication.
     */
    Map<NodeId, Position> nextPositions;

    /**
     * For each server, index of highest log entry known to be replicated on server.
     * <p>
     * This is used to track the highest log entry that has been successfully
     * replicated to each follower.
     */
    Map<NodeId, Position> matchPositions;
}

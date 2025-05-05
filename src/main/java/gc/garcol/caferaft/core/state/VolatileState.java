package gc.garcol.caferaft.core.state;

import gc.garcol.caferaft.core.log.Position;
import lombok.Data;

import static gc.garcol.caferaft.core.constant.LogConstant.INITIAL_POSITION;

/**
 * Represents the volatile state of a Raft node that is maintained in memory
 * and is lost if the server crashes or restarts.
 * <p>
 * This state is updated in memory and does not need to be persisted.
 */
@Data
public class VolatileState {
    /**
     * Index of highest log entry known to be committed
     */
    Position commitPosition = INITIAL_POSITION.copy();

    /**
     * Index of highest log entry applied to state machine
     */
    Position lastApplied = INITIAL_POSITION.copy();
}

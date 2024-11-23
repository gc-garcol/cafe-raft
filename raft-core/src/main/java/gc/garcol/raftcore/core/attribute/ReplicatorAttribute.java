package gc.garcol.raftcore.core.attribute;

import gc.garcol.raftcore.core.EntryPosition;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * Volatile state on leaders
 * <p>
 * (Reinitialized after election)
 *
 * @author thaivc
 * @since 2025
 */
@Getter
@Setter
@Accessors(fluent = true, chain = true)
public class ReplicatorAttribute implements Attribute
{

    /**
     * for each server, index of the next log entry
     * to send to that server (initialized to leader
     * last log <em><b>"index + 1"</b></em>)
     */
    private Map<Integer, EntryPosition> nextPositions;

    /**
     * for each server, index of highest log entry
     * known to be replicated on server
     * (initialized to <em><b>"0"</b></em>, increases monotonically)
     */
    private Map<Integer, EntryPosition> matchedPositions;

}

package gc.garcol.raftcore.core.attribute;

import gc.garcol.raftcore.core.EntryPosition;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author thaivc
 * @since 2025
 */
@Getter
@Setter
@Accessors(fluent = true, chain = true)
public class StateMachineAttribute implements Attribute
{
    /**
     * index of highest log entry known to be
     * committed (initialized to 0, increases
     * monotonically)
     */
    private EntryPosition commitPosition;

    /**
     * index of highest log entry applied to state
     * machine (initialized to 0, increases
     * monotonically)
     */
    private EntryPosition lastAppliedPosition;
}

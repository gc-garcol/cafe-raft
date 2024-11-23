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
public class LogAttribute implements Attribute
{
    /**
     * latest term server has seen (initialized to <em><b>"0"</b></em>
     * on first boot, increases monotonically)
     */
    private long currentTerm;

    /**
     * latest appended position
     */
    private EntryPosition latestPosition;
}

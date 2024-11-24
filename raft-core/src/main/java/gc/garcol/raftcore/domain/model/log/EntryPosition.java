package gc.garcol.raftcore.domain.model.log;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author thaivc
 * @since 2024
 */
@Getter
@Setter
@Accessors(fluent = true)
public class EntryPosition
{
    private long term;
    private long index;
}

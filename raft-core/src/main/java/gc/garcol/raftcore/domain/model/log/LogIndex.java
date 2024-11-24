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
@Accessors(fluent = true, chain = true)
public class LogIndex
{

    public static final int SIZE = Long.BYTES + Integer.BYTES;

    long index;
    int entryLength;
}

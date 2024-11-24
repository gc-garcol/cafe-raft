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
public class LogMetadata
{
    public static final int SIZE = Long.BYTES;
    long currentTerm = 1;
}

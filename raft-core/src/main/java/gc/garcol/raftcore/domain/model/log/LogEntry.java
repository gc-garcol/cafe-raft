package gc.garcol.raftcore.domain.model.log;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author thaivc
 * @since 2024
 */
@Getter
@Setter
@NoArgsConstructor
public class LogEntry
{
    private long term;
    private long index;

    private byte[] data;
}

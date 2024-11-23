package gc.garcol.raftcore.core;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.nio.ByteBuffer;

/**
 * Represents a log entry in the Raft consensus algorithm.
 * <p>
 * Each log entry consists of a position and the associated data.
 *
 * @author thaivc
 * @since 2024
 */
@Getter
@Setter
@NoArgsConstructor
public class LogEntry
{
    /**
     * position of the entry in the log
     */
    EntryPosition position;

    /**
     * data of the entry
     */
    private ByteBuffer data;
}

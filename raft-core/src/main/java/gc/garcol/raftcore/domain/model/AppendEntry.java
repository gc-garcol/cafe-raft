package gc.garcol.raftcore.domain.model;

import gc.garcol.raftcore.domain.model.log.LogEntry;
import gc.garcol.raftcore.domain.model.log.LogIndex;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author thaivc
 * @since 2024
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(fluent = true, chain = true)
public class AppendEntry implements RpcRequest
{
    /**
     * leader’s term
     */
    private long term;

    /**
     * so follower can redirect clients
     */
    private int leaderId;

    /**
     * Including:
     * <ul>
     *   <li>Index of log entry immediately preceding new ones.</li>
     *   <li>Term of prevLogIndex entry</li>
     * </ul>
     * <p>
     * Examples:
     * <ul>
     *   <li>If entries are from { term = 2, index = 2 } to { term = 3, index = 5 }, then prevLogIndex = { term = 2, index = 1 }</li>
     *   <li>If entries are from { term = 1, index = 0 } to { term = 3, index = 5 }, then prevLogIndex = null</li>
     * </ul>
     * </p>
     */
    private LogIndex prevLogIndex;

    /**
     * log entries to store (empty for heartbeat;
     * may send more than one for efficiency)
     */
    private List<LogEntry> entries;

    /**
     * leader’s commitIndex
     */
    private LogIndex leaderCommitIndex;
}

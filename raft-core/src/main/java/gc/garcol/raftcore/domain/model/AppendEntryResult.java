package gc.garcol.raftcore.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author thaivc
 * @since 2024
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(fluent = true, chain = true)
public class AppendEntryResult implements RpcRequest
{
    /**
     * currentTerm, for leader to update itself
     */
    private long term;

    /**
     * true if follower contained entry matching
     * prevLogIndex and prevLogTerm
     */
    private boolean success;
}

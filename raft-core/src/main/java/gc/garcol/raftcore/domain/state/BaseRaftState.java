package gc.garcol.raftcore.domain.state;

import gc.garcol.raftcore.domain.model.log.LogIndex;
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
public class BaseRaftState
{
    /**
     * latest term server has seen (initialized to 0
     * on first boot, increases monotonically)
     * <p>
     * Including: current-term and current-index
     */
    protected LogIndex currentIndex;

    /**
     * candidateId that received vote in current
     * term (or null if none)
     */
    protected Integer votedFor;

    /**
     * index of highest log entry known to be
     * committed (initialized to 0, increases
     * monotonically)
     */
    protected LogIndex commitIndex;

    /**
     * index of highest log entry applied to state
     * machine (initialized to 0, increases
     * monotonically)
     */
    protected LogIndex lastApplied;
}

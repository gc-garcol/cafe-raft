package gc.garcol.raftcore.core.attribute;

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
public class ElectionAttribute implements Attribute
{
    /**
     * candidateId that received vote in <b>"current term"</b> (or null if none)
     */
    private Integer voteFor;

    /**
     * current leader id
     */
    private Integer currentLeader;
}

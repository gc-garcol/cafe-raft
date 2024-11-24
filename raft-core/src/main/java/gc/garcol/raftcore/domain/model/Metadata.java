package gc.garcol.raftcore.domain.model;

import gc.garcol.raftcore.domain.model.log.LogIndex;
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
@Accessors(fluent = true)
public class Metadata
{

    private LogIndex currentIndex;

}

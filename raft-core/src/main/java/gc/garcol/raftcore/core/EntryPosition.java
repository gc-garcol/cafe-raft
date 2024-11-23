package gc.garcol.raftcore.core;

import lombok.*;

/**
 * @author thaivc
 * @since 2024
 */
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class EntryPosition
{
    private long term;
    private long index;
}

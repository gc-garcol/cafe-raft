package gc.garcol.caferaft.core.state;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author thaivc
 * @since 2025
 */
@Data
public class CandidateVolatileState {
    private Map<NodeId, Boolean> votedQuorum = new HashMap<>();
}

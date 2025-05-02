package gc.garcol.caferaft.core.state;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

/**
 * @author thaivc
 * @since 2025
 */
@Data
public class CandidateVolatileState {
    private Map<NodeId, Boolean> votedQuorum = new HashMap<>();
}

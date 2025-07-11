package gc.garcol.caferaft.application.config;

import gc.garcol.caferaft.core.log.LogManager;
import gc.garcol.caferaft.core.log.Segment;
import gc.garcol.caferaft.core.state.NodeId;
import gc.garcol.caferaft.core.state.RaftState;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * @author thaivc
 * @since 2025
 */
@Component
@RequiredArgsConstructor
public class ActuatorInfoConfig implements InfoContributor {

    private final RaftState  raftState;
    private final LogManager logManager;

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> raftNode = Map.of("nodeId", raftState.getPersistentState().getNodeId().id(), "state",
                raftState.role, "leaderId", Optional.ofNullable(raftState.leaderId).map(NodeId::id).orElse(-1),

                // not sync immediately
                "commitedPosition", raftState.getVolatileState().getCommitPosition(), "lastApplied",
                raftState.getVolatileState().getLastApplied(), "totalLogs",
                logManager.segments.stream().map(Segment::getSize).reduce(Long::sum));
        builder.withDetails(raftNode);
    }
}

package gc.garcol.caferaft.application.view;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import gc.garcol.caferaft.core.constant.ClusterProperty;
import gc.garcol.caferaft.core.log.LogManager;
import gc.garcol.caferaft.core.log.Segment;
import gc.garcol.caferaft.core.state.NodeId;
import gc.garcol.caferaft.core.state.RaftState;
import lombok.RequiredArgsConstructor;

/**
 * @author thaivc
 * @since 2025
 */
@Controller
@RequiredArgsConstructor
public class AppView {
    private final RaftState raftState;
    private final LogManager logManager;
    private final ClusterProperty clusterProperty;

    @RequestMapping("/")
    public String cluster(Model model) {
        var nodes = java.util.stream.IntStream.range(0, clusterProperty.getNodes().size())
            .mapToObj(i -> new NodeInfo(i, clusterProperty.getNodes().get(i)))
            .map(Object::toString)
            .collect(Collectors.toList());

        model.addAttribute("nodeId", raftState.getPersistentState().getNodeId().id());
        model.addAttribute("state", raftState.role);
        model.addAttribute("term", raftState.getPersistentState().getCurrentTerm());
        model.addAttribute("totalLogs", logManager.segments.stream().map(Segment::getSize).reduce(Long::sum).orElse(0L));
        model.addAttribute("leaderId", Optional.ofNullable(raftState.leaderId).map(NodeId::id).orElse(null));
        model.addAttribute("lastPosition", logManager.lastPosition());
        model.addAttribute("commitedPosition", raftState.getVolatileState().getCommitPosition());
        model.addAttribute("lastApplied", raftState.getVolatileState().getLastApplied());
        model.addAttribute("nodes", nodes);
        return "index";
    }
}

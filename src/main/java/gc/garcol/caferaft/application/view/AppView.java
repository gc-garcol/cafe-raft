package gc.garcol.caferaft.application.view;

import gc.garcol.caferaft.core.state.NodeId;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import gc.garcol.caferaft.core.log.LogManager;
import gc.garcol.caferaft.core.log.Segment;
import gc.garcol.caferaft.core.state.RaftState;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * @author thaivc
 * @since 2025
 */
@Controller
@RequiredArgsConstructor
public class AppView {
    private final RaftState raftState;
    private final LogManager logManager;

    @RequestMapping("/")
    public String cluster(Model model) {
        model.addAttribute("nodeId", raftState.getPersistentState().getNodeId().id());
        model.addAttribute("state", raftState.role);
        model.addAttribute("totalLogs", logManager.segments.stream().map(Segment::getSize).reduce(Long::sum).orElse(0L));
        model.addAttribute("leaderId", Optional.ofNullable(raftState.leaderId).map(NodeId::id).orElse(null));
        model.addAttribute("lastPosition", logManager.lastPosition());
        model.addAttribute("commitedPosition", raftState.getVolatileState().getCommitPosition());
        model.addAttribute("lastApplied", raftState.getVolatileState().getLastApplied());
        return "index";
    }
}

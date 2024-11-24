package gc.garcol.raftcore.domain;

import gc.garcol.raftcore.domain.state.RaftState;
import lombok.RequiredArgsConstructor;

/**
 * @author thaivc
 * @since 2024
 */
@RequiredArgsConstructor
public class RaftServer
{

    private final RaftState raftState;

}

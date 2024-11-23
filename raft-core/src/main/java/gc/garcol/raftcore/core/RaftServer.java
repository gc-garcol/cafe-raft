package gc.garcol.raftcore.core;

import gc.garcol.raftcore.core.state.RaftState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author thaivc
 * @since 2024
 */
@Slf4j
@RequiredArgsConstructor
public class RaftServer
{
    private RaftState raftState;
}

package gc.garcol.raftcore.domain.repository;

import gc.garcol.raftcore.domain.RaftServer;

/**
 * @author thaivc
 * @since 2024
 */
public interface SnapshotRepository
{
    void saveSnapshot(RaftServer raftServer);

    void loadSnapshotInto(RaftServer raftServer);
}

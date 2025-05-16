package gc.garcol.caferaft.core.service;

/**
 * @author thaivc
 * @since 2025
 */
public interface RaftLogicHandler {
    /**
     * Executes role-specific Raft protocol logic based on the current node's role.
     *
     * <p>For each role, the following actions are performed:
     * <ul>
     *     <li><b>Leader:</b> Sends AppendEntries RPCs to all followers to maintain leadership
     *     and replicate new log entries.</li>
     *
     *     <li><b>Candidate:</b> Initiates a new election by sending RequestVote RPCs to all
     *     other nodes when the election timeout occurs.</li>
     *
     *     <li><b>Follower:</b> Converts to candidate if heartbeat timeout occurs.</li>
     *
     *     <li><b>Leader/Follower:</b> Applies committed log entries to the state machine.
     *     This is done by checking the committed position and executing any pending commands
     *     that have been committed by the cluster.</li>
     * </ul>
     *
     * <p>This method should be called periodically to ensure proper functioning of the
     * Raft consensus protocol. The exact timing of these calls is managed by the
     * implementation's scheduling mechanism.
     */
    void apply();
}

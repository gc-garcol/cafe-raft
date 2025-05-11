package gc.garcol.caferaft.core.service;

import gc.garcol.caferaft.core.client.Command;
import gc.garcol.caferaft.core.client.CommonErrorResponse;
import gc.garcol.caferaft.core.client.Query;
import gc.garcol.caferaft.core.constant.ClusterProperty;
import gc.garcol.caferaft.core.state.RaftRole;
import gc.garcol.caferaft.core.state.RaftState;
import gc.garcol.caferaft.core.time.IdleStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author thaivc
 * @since 2025
 */
@Slf4j
@RequiredArgsConstructor
public class RaftMessageCoordinator {

    private final IdleStrategy consumerIdle;
    private final ClusterProperty clusterProperty;
    private final AtomicBoolean running;
    private final QueryHandler queryHandler;
    private final CommandJournaler commandJournaler;
    private final ClusterRpcHandler clusterRpcHandler;
    private final RaftLogicHandler raftLogicHandler;
    private final RaftState raftState;
    private final LinkedList<ClientReplier> repliers;

    private ConcurrentLinkedDeque<Message<?>> messageQueue;

    public boolean publish(Message<?> message) {
        return messageQueue.add(message);
    }

    public Runnable build(int queueSize) {
        this.messageQueue = new ConcurrentLinkedDeque<>();
        return this::doWork;
    }

    private void doWork() {
        while (running.get()) {
            try {
                consumerIdle.idle();
                int batchSize = clusterProperty.getMessageBatchSize();
                Message<?> message;
                while (batchSize-- > 0 && (message = messageQueue.poll()) != null) {
                    this.handleMessage(message);
                }

                this.raftLogicHandler.apply();
            } catch (Exception e) {
                log.error("Error processing message", e);
            }
        }
    }

    private void handleMessage(Message<?> message) {
        switch (message) {
            case ClientMessage<?> clientMessage when clientMessage.payload() instanceof Query query -> {
                if (isNotLeader()) {
                    rejectNonLeaderRequest(clientMessage);
                    return;
                }
                queryHandler.handleRequest(query, clientMessage.replier());
            }
            case ClientMessage<?> clientMessage when clientMessage.payload() instanceof Command command -> {
                if (isNotLeader()) {
                    rejectNonLeaderRequest(clientMessage);
                    return;
                }
                // store future-replier, when the command is applied into the state machine, then reply to the client
                var logEntry = commandJournaler.acceptCommand(raftState.getPersistentState().getCurrentTerm(), command);
                repliers.add(new ClientReplier(
                    logEntry.getPosition(),
                    clientMessage.replier()
                ));
            }
            case ClusterRpcMessage<?> clusterRpcMessage ->
                clusterRpcHandler.handleClusterRpc(clusterRpcMessage.payload());
            default -> log.error("Unknown message type: {}", message.getClass().getName());
        }
    }

    private boolean isNotLeader() {
        return !RaftRole.LEADER.equals(raftState.getRole());
    }

    private void rejectNonLeaderRequest(ClientMessage<?> clientMessage) {
        clientMessage.replier().complete(
            new CommonErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "Node is not LEADER"
            )
        );
    }
}

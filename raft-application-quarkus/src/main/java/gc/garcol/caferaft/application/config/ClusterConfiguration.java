package gc.garcol.caferaft.application.config;

import gc.garcol.caferaft.core.async.ExecutorEventPublisher;
import gc.garcol.caferaft.core.client.CommandSerdes;
import gc.garcol.caferaft.core.constant.ClusterProperty;
import gc.garcol.caferaft.core.log.LogManager;
import gc.garcol.caferaft.core.repository.ClusterStateRepository;
import gc.garcol.caferaft.core.repository.LogRepository;
import gc.garcol.caferaft.core.rpc.RpcNetworkOutbound;
import gc.garcol.caferaft.core.service.*;
import gc.garcol.caferaft.core.state.RaftState;
import gc.garcol.caferaft.core.time.IdleStrategy;
import gc.garcol.caferaft.core.time.SleepIdleStrategy;
import gc.garcol.caferaft.core.time.YieldIdleStrategy;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author thaivc
 * @since 2025
 */
@ApplicationScoped
@RequiredArgsConstructor
public class ClusterConfiguration {

    private final ClusterPropertyConfig clusterProperties;
    private final Vertx                 vertx;

    @Produces
    @ApplicationScoped
    public WebClient webClient() {
        return WebClient.create(vertx);
    }

    @Produces
    @ApplicationScoped
    public AtomicBoolean appRunning() {
        return new AtomicBoolean(true);
    }

    @Produces
    @ApplicationScoped
    public ClusterProperty clusterProperty() {
        var clusterProperty = new ClusterProperty();
        clusterProperty.setQueueSize(clusterProperties.getQueueSize());
        clusterProperty.setIdleStrategy(clusterProperties.getIdleStrategy());
        clusterProperty.setNodeId(clusterProperties.getNodeId());
        clusterProperty.setNodes(clusterProperties.getNodes());
        clusterProperty.setBaseDisk(clusterProperties.getBaseDisk());
        clusterProperty.setElectionTimeoutMs(clusterProperties.getElectionTimeoutMs());
        clusterProperty.setHeartbeatTimeoutMs(clusterProperties.getHeartbeatTimeoutMs());
        clusterProperty.setHeartbeatIntervalMs(clusterProperties.getHeartbeatIntervalMs());
        clusterProperty.setNextSendLogTimeoutMs(clusterProperties.getNextSendLogTimeoutMs());
        clusterProperty.setMessageBatchSize(clusterProperties.getMessageBatchSize());
        clusterProperty.setCommitedLogBatchSize(clusterProperties.getCommitedLogBatchSize());
        clusterProperty.setAppendLogBatchSize(clusterProperties.getAppendLogBatchSize());
        return clusterProperty;
    }

    @Produces
    @ApplicationScoped
    public IdleStrategy idleStrategy() {
        var config = clusterProperties.getIdleStrategy().split("\\|");
        if (config[0].contains(SleepIdleStrategy.class.getSimpleName())) {
            return new SleepIdleStrategy(Long.parseLong(config[1]));
        }
        return new YieldIdleStrategy();
    }

    @Produces
    @ApplicationScoped
    public LogManager logManager(LogRepository logRepository) {
        return new LogManager(logRepository);
    }

    @Produces
    @ApplicationScoped
    public CommandJournaler commandJournaler(LogManager logManager) {
        return new CommandJournalerImpl(logManager);
    }

    @Produces
    @ApplicationScoped
    public RaftState raftState() {
        return new RaftState();
    }

    @Produces
    @ApplicationScoped
    public LinkedList<ClientReplier> repliers() {
        return new LinkedList<>();
    }

    @Produces
    @ApplicationScoped
    public BroadcastService broadcastService(ClusterProperty clusterProperty,
                                             @Named("common-executor-pool") Executor commonExecutorPool,
                                             RaftState raftState, LogManager logManager,
                                             RpcNetworkOutbound rpcNetworkOutbound) {
        return new BroadcastServiceImpl(clusterProperty, commonExecutorPool, raftState, logManager, rpcNetworkOutbound);
    }

    @Produces
    @ApplicationScoped
    public ClusterRpcHandler clusterRpcHandler(RaftState raftState, LogManager logManager,
                                               ClusterStateRepository clusterStateRepository,
                                               RpcNetworkOutbound rpcNetworkOutbound,
                                               @Named("common-executor-pool") Executor commonExecutorPool,
                                               ClusterProperty clusterProperty, BroadcastService broadcastService,
                                               CommandSerdes commandSerdes) {
        return new ClusterRpcHandlerImpl(raftState, logManager, rpcNetworkOutbound, clusterStateRepository,
                commonExecutorPool, clusterProperty, broadcastService, commandSerdes);
    }

    @Produces
    @ApplicationScoped
    public RaftLogicHandler raftLogicHandler(RaftState raftState, LogManager logManager, StateMachine stateMachine,
                                             ClusterProperty clusterProperty, LinkedList<ClientReplier> repliers,
                                             ExecutorEventPublisher replyPublisher,
                                             ClusterStateRepository clusterStateRepository,
                                             BroadcastService broadcastService, RpcNetworkOutbound rpcNetworkOutbound,
                                             @Named("common-executor-pool") Executor commonExecutorPool,
                                             CommandSerdes commandSerdes) {
        return new RaftLogicHandlerImpl(raftState, logManager, stateMachine, clusterProperty, repliers, replyPublisher,
                clusterStateRepository, broadcastService, rpcNetworkOutbound, commonExecutorPool, commandSerdes);
    }

    @Produces
    @ApplicationScoped
    public RaftMessageCoordinator raftMessageCoordinator(IdleStrategy idleStrategy, ClusterProperty clusterProperty,
                                                         AtomicBoolean appRunning, QueryHandler queryHandler,
                                                         CommandJournaler commandJournaler,
                                                         ClusterRpcHandler clusterRpcHandler,
                                                         RaftLogicHandler raftLogicHandler, RaftState raftState,
                                                         LinkedList<ClientReplier> repliers) {
        return new RaftMessageCoordinator(idleStrategy, clusterProperty, appRunning, queryHandler, commandJournaler,
                clusterRpcHandler, raftLogicHandler, raftState, repliers);
    }

    @Produces
    @ApplicationScoped
    public Runnable clusterWorker(RaftMessageCoordinator raftMessageCoordinator) {
        return raftMessageCoordinator.build(clusterProperties.getQueueSize());
    }
} 
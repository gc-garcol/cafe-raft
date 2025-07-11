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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author thaivc
 * @since 2025
 */
@Configuration
@RequiredArgsConstructor
public class ClusterConfiguration {

    private final ClusterPropertyConfig clusterProperties;

    @Bean
    public AtomicBoolean appRunning() {
        return new AtomicBoolean(true);
    }

    @Bean
    public ClusterProperty clusterProperty() {
        var clusterProperty = new ClusterProperty();
        BeanUtils.copyProperties(clusterProperties, clusterProperty);
        return clusterProperty;
    }

    @Bean
    public IdleStrategy idleStrategy() {
        var config = clusterProperties.getIdleStrategy().split("\\|");
        if (config[0].contains(SleepIdleStrategy.class.getSimpleName())) {
            return new SleepIdleStrategy(Long.parseLong(config[1]));
        }
        return new YieldIdleStrategy();
    }

    @Bean
    public LogManager logManager(final LogRepository logRepository) {
        return new LogManager(logRepository);
    }

    @Bean
    public CommandJournaler commandJournaler(final LogManager logManager) {
        return new CommandJournalerImpl(logManager);
    }

    @Bean
    public RaftState raftState() {
        return new RaftState();
    }

    @Bean
    public LinkedList<ClientReplier> repliers() {
        return new LinkedList<>();
    }

    @Bean
    public BroadcastService broadcastService(final ClusterProperty clusterProperty,
                                             final @Qualifier("common-executor-pool") TaskExecutor commonExecutorPool,
                                             final RaftState raftState, final LogManager logManager,
                                             final RpcNetworkOutbound rpcNetworkOutbound) {
        return new BroadcastServiceImpl(clusterProperty, commonExecutorPool, raftState, logManager, rpcNetworkOutbound);
    }

    @Bean
    public ClusterRpcHandler clusterRpcHandler(final RaftState raftState, final LogManager logManager,
                                               final ClusterStateRepository clusterStateRepository,
                                               final RpcNetworkOutbound rpcNetworkOutbound,
                                               final @Qualifier("common-executor-pool") TaskExecutor commonExecutorPool,
                                               final ClusterProperty clusterProperty,
                                               final BroadcastService broadcastService,
                                               final CommandSerdes commandSerdes) {
        return new ClusterRpcHandlerImpl(raftState, logManager, rpcNetworkOutbound, clusterStateRepository,
                commonExecutorPool, clusterProperty, broadcastService, commandSerdes);
    }

    @Bean
    public RaftLogicHandler raftLogicHandler(final RaftState raftState, final LogManager logManager,
                                             final StateMachine stateMachine, final ClusterProperty clusterProperty,
                                             final LinkedList<ClientReplier> repliers,
                                             final ExecutorEventPublisher replyPublisher,
                                             final ClusterStateRepository clusterStateRepository,
                                             final BroadcastService broadcastService,
                                             final RpcNetworkOutbound rpcNetworkOutbound,
                                             final @Qualifier("common-executor-pool") TaskExecutor commonExecutorPool,
                                             final CommandSerdes commandSerdes) {
        return new RaftLogicHandlerImpl(raftState, logManager, stateMachine, clusterProperty, repliers, replyPublisher,
                clusterStateRepository, broadcastService, rpcNetworkOutbound, commonExecutorPool, commandSerdes);
    }

    @Bean
    public RaftMessageCoordinator raftMessageCoordinator(final IdleStrategy idleStrategy,
                                                         final ClusterProperty clusterProperty,
                                                         final AtomicBoolean appRunning,
                                                         final QueryHandler queryHandler,
                                                         final CommandJournaler commandJournaler,
                                                         final ClusterRpcHandler clusterRpcHandler,
                                                         final RaftLogicHandler raftLogicHandler,
                                                         final RaftState raftState,
                                                         final LinkedList<ClientReplier> repliers) {
        return new RaftMessageCoordinator(idleStrategy, clusterProperty, appRunning, queryHandler, commandJournaler,
                clusterRpcHandler, raftLogicHandler, raftState, repliers);
    }

    @Bean
    public Runnable clusterWorker(final RaftMessageCoordinator raftMessageCoordinator) {
        return raftMessageCoordinator.build(clusterProperties.getQueueSize());
    }
}

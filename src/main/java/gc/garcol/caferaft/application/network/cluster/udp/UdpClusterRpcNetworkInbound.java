package gc.garcol.caferaft.application.network.cluster.udp;

import gc.garcol.caferaft.core.rpc.*;
import gc.garcol.caferaft.core.service.ClusterRpcMessage;
import gc.garcol.caferaft.core.service.RaftMessageCoordinator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * @author thaivc
 * @since 2025
 */
@Profile("rpc-udp")
@Component
@RequiredArgsConstructor
public class UdpClusterRpcNetworkInbound implements RpcNetworkInbound {

    private final RpcMessageSerdes rpcMessageSerdes;
    private final RaftMessageCoordinator raftMessageCoordinator;

    @ServiceActivator(inputChannel = UdpChannelConstant.INBOUND_CHANNEL)
    public void handleMessage(Message<byte[]> message) {
        ClusterRpc clusterRpc = rpcMessageSerdes.fromBytes(message.getPayload());
        publishMessage(clusterRpc);
    }

    private <T extends ClusterRpc> void publishMessage(T payload) {
        var message = new ClusterRpcMessage<T>() {
            @Override
            public T payload() {
                return payload;
            }
        };
        raftMessageCoordinator.publish(message);
    }

    @Override
    public void appendEntryRequest(AppendEntryRequest request) {
        throw new IllegalStateException("Never appear!!!");
    }

    @Override
    public void appendEntryResponse(AppendEntryResponse response) {
        throw new IllegalStateException("Never appear!!!");
    }

    @Override
    public void voteRequest(VoteRequest request) {
        throw new IllegalStateException("Never appear!!!");
    }

    @Override
    public void voteResponse(VoteResponse response) {
        throw new IllegalStateException("Never appear!!!");
    }
}

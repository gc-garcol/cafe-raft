package gc.garcol.caferaft.application.network.cluster.udp;

import gc.garcol.caferaft.core.rpc.*;
import gc.garcol.caferaft.core.state.NodeId;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.ip.udp.UnicastSendingMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author thaivc
 * @since 2025
 */
@Profile("rpc-udp")
@Component
@RequiredArgsConstructor
public class UdpClusterRpcNetworkOutbound implements RpcNetworkOutbound {

    private final Map<Integer, UnicastSendingMessageHandler> udpSendingAdapter;
    private final RpcMessageSerdes rpcMessageSerdes;

    @Override
    public void appendEntryRequest(NodeId nodeId, AppendEntryRequest request) {
        sendMessage(nodeId, UdpChannelConstant.RPC_TYPE_APPEND_ENTRY_REQUEST, request);
    }

    @Override
    public void appendEntryResponse(NodeId nodeId, AppendEntryResponse response) {
        sendMessage(nodeId, UdpChannelConstant.RPC_TYPE_APPEND_ENTRY_RESPONSE, response);
    }

    @Override
    public void voteRequest(NodeId nodeId, VoteRequest request) {
        sendMessage(nodeId, UdpChannelConstant.RPC_TYPE_VOTE_REQUEST, request);
    }

    @Override
    public void voteResponse(NodeId nodeId, VoteResponse response) {
        sendMessage(nodeId, UdpChannelConstant.RPC_TYPE_VOTE_RESPONSE, response);
    }

    private void sendMessage(NodeId nodeId, int rpcType, ClusterRpc clusterRpc) {
        byte[] data = rpcMessageSerdes.toBytes(clusterRpc);
        Message<byte[]> message = new GenericMessage<>(
            data,
            Map.of(UdpChannelConstant.HEADER_TYPE, rpcType)
        );
        UnicastSendingMessageHandler sender = udpSendingAdapter.get(nodeId.id());
        sender.handleMessage(message);
    }
}

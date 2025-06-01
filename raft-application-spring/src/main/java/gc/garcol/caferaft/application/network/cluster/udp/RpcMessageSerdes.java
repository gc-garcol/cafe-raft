package gc.garcol.caferaft.application.network.cluster.udp;

import com.fasterxml.jackson.databind.ObjectMapper;
import gc.garcol.caferaft.core.rpc.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * @author thaivc
 * @since 2025
 */
@Profile("rpc-udp")
@Component
@RequiredArgsConstructor
public class RpcMessageSerdes {

    private static final Map<Integer, Class<? extends ClusterRpc>> RPC_CLUSTER_TYPES = Map.of(
            UdpChannelConstant.RPC_TYPE_APPEND_ENTRY_REQUEST, AppendEntryRequest.class,
            UdpChannelConstant.RPC_TYPE_APPEND_ENTRY_RESPONSE, AppendEntryResponse.class,
            UdpChannelConstant.RPC_TYPE_VOTE_REQUEST, VoteRequest.class, UdpChannelConstant.RPC_TYPE_VOTE_RESPONSE,
            VoteResponse.class);
    private final        ObjectMapper                              objectMapper;

    private static int getHeaderType(ClusterRpc clusterRpc) {
        int headerType = -1;
        if (clusterRpc instanceof AppendEntryRequest) {
            headerType = UdpChannelConstant.RPC_TYPE_APPEND_ENTRY_REQUEST;
        } else if (clusterRpc instanceof AppendEntryResponse) {
            headerType = UdpChannelConstant.RPC_TYPE_APPEND_ENTRY_RESPONSE;
        } else if (clusterRpc instanceof VoteRequest) {
            headerType = UdpChannelConstant.RPC_TYPE_VOTE_REQUEST;
        } else if (clusterRpc instanceof VoteResponse) {
            headerType = UdpChannelConstant.RPC_TYPE_VOTE_RESPONSE;
        }
        return headerType;
    }

    @SneakyThrows
    public byte[] toBytes(ClusterRpc clusterRpc) {
        byte[] payload = objectMapper.writeValueAsBytes(clusterRpc);

        int headerType = getHeaderType(clusterRpc);

        if (headerType == -1) {
            throw new IllegalArgumentException("Invalid cluster RPC type");
        }

        byte[] header = ByteBuffer.allocate(4).putInt(headerType).array();
        return ByteBuffer.allocate(header.length + payload.length).put(header).put(payload).array();
    }

    @SneakyThrows
    public ClusterRpc fromBytes(int type, byte[] bytes) {
        Class<? extends ClusterRpc> rpcClass = RPC_CLUSTER_TYPES.get(type);
        return objectMapper.readValue(bytes, rpcClass);
    }

    public ClusterRpc fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int headerType = buffer.getInt();
        byte[] payload = new byte[bytes.length - 4];
        buffer.get(payload);
        return fromBytes(headerType, payload);
    }

}

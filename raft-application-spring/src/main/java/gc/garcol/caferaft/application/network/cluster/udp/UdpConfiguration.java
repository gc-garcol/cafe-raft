package gc.garcol.caferaft.application.network.cluster.udp;

import gc.garcol.caferaft.core.constant.ClusterProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.ip.udp.UnicastReceivingChannelAdapter;
import org.springframework.integration.ip.udp.UnicastSendingMessageHandler;
import org.springframework.messaging.MessageChannel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author thaivc
 * @since 2025
 */
@Profile("rpc-udp")
@Configuration
@RequiredArgsConstructor
public class UdpConfiguration {

    private final ClusterProperty clusterProperty;
    @Value("${udp.nodes.hosts}")
    private       List<String>    hosts;
    @Value("${udp.nodes.ports}")
    private       List<Integer>   ports;

    @Bean
    public MessageChannel inboundChannel() {
        return new DirectChannel();
    }

    @Bean
    public Map<Integer, UnicastSendingMessageHandler> udpSendingAdapter() {
        Map<Integer, UnicastSendingMessageHandler> otherNodes = new HashMap<>();
        for (int nodeId = 0; nodeId < hosts.size(); nodeId++) {
            if (nodeId == clusterProperty.getNodeId()) {
                continue;
            }

            otherNodes.put(nodeId, new UnicastSendingMessageHandler(hosts.get(nodeId), ports.get(nodeId)));
        }
        return otherNodes;
    }

    @Bean
    public UnicastReceivingChannelAdapter udpReceiverAdapter(final MessageChannel inboundChannel) {
        UnicastReceivingChannelAdapter receiver = new UnicastReceivingChannelAdapter(
                ports.get(clusterProperty.getNodeId()));
        receiver.setOutputChannel(inboundChannel);
        receiver.setOutputChannelName(UdpChannelConstant.INBOUND_CHANNEL);
        return receiver;
    }
}

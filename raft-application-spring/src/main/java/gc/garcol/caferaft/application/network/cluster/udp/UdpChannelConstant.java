package gc.garcol.caferaft.application.network.cluster.udp;

import lombok.NoArgsConstructor;

/**
 * @author thaivc
 * @since 2025
 */
@NoArgsConstructor
public class UdpChannelConstant {

    public static final String INBOUND_CHANNEL = "udp-inbound-channel";

    public static final String HEADER_TYPE = "header-type";

    public static final int RPC_TYPE_APPEND_ENTRY_REQUEST  = 1;
    public static final int RPC_TYPE_APPEND_ENTRY_RESPONSE = 2;
    public static final int RPC_TYPE_VOTE_REQUEST          = 3;
    public static final int RPC_TYPE_VOTE_RESPONSE         = 4;
}

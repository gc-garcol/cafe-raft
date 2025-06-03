package gc.garcol.caferaft.application.network.cluster.rest;

import gc.garcol.caferaft.core.rpc.*;
import gc.garcol.caferaft.core.service.ClusterRpcMessage;
import gc.garcol.caferaft.core.service.RaftMessageCoordinator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author thaivc
 * @since 2025
 */
@RestController
@Profile("rpc-rest")
@RequiredArgsConstructor
public class RestClusterRpcNetworkInbound implements RpcNetworkInbound {

    private final RaftMessageCoordinator raftMessageCoordinator;

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/rpc/append-entry-request")
    public io.smallrye.mutiny.Uni appendEntryRequest(@RequestBody AppendEntryRequest request) {
        publishMessage(request);
        return null;
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/rpc/append-entry-response")
    public void appendEntryResponse(@RequestBody AppendEntryResponse response) {
        publishMessage(response);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/rpc/vote-request")
    public void voteRequest(@RequestBody VoteRequest request) {
        publishMessage(request);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/rpc/vote-response")
    public void voteResponse(@RequestBody VoteResponse response) {
        publishMessage(response);
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
}

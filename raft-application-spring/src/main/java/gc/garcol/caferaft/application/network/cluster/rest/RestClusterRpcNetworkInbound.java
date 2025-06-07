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
import reactor.core.publisher.Mono;

/**
 * @author thaivc
 * @since 2025
 */
@RestController
@Profile("rpc-rest")
@RequiredArgsConstructor
public class RestClusterRpcNetworkInbound {

    private final RaftMessageCoordinator raftMessageCoordinator;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/rpc/append-entry-request")
    public Mono<Void> appendEntryRequest(@RequestBody Mono<AppendEntryRequest> appendEntryRequest) {
        return appendEntryRequest.doOnNext(this::publishMessage).then();
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/rpc/append-entry-response")
    public Mono<Void> appendEntryResponse(@RequestBody Mono<AppendEntryResponse> appendEntryResponse) {
        return appendEntryResponse.doOnNext(this::publishMessage).then();
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/rpc/vote-request")
    public Mono<Void> voteRequest(@RequestBody Mono<VoteRequest> voteRequest) {
        return voteRequest.doOnNext(this::publishMessage).then();
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/rpc/vote-response")
    public Mono<Void> voteResponse(@RequestBody Mono<VoteResponse> voteResponse) {
        return voteResponse.doOnNext(this::publishMessage).then();
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

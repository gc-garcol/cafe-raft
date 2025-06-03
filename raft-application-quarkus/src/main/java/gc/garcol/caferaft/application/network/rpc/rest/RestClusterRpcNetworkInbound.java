package gc.garcol.caferaft.application.network.rpc.rest;

import gc.garcol.caferaft.core.rpc.*;
import gc.garcol.caferaft.core.service.ClusterRpcMessage;
import gc.garcol.caferaft.core.service.RaftMessageCoordinator;
import io.quarkus.vertx.web.Body;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

import static io.quarkus.vertx.web.ReactiveRoutes.APPLICATION_JSON;

/**
 * @author thaivc
 * @since 2025
 */
@RouteBase(produces = APPLICATION_JSON, consumes = APPLICATION_JSON)
@RequiredArgsConstructor
public class RestClusterRpcNetworkInbound {

    private final RaftMessageCoordinator raftMessageCoordinator;

    @Route(path = "/rpc/append-entry-request", methods = { Route.HttpMethod.POST })
    public Uni<Void> appendEntryRequest(@Body AppendEntryRequest request) {
        publishMessage(request);
        return Uni.createFrom().voidItem();
    }

    @Route(path = "/rpc/append-entry-response", methods = { Route.HttpMethod.POST })
    public Uni<Void> appendEntryResponse(@Body AppendEntryResponse response) {
        publishMessage(response);
        return Uni.createFrom().voidItem();
    }

    @Route(path = "/rpc/vote-request", methods = { Route.HttpMethod.POST })
    public Uni<Void> voteRequest(@Body VoteRequest request) {
        publishMessage(request);
        return Uni.createFrom().voidItem();
    }

    @Route(path = "/rpc/vote-response", methods = { Route.HttpMethod.POST })
    public Uni<Void> voteResponse(@Body VoteResponse response) {
        publishMessage(response);
        return Uni.createFrom().voidItem();
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

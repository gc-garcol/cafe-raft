package gc.garcol.caferaft.application.network.rest;

import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static io.quarkus.vertx.web.ReactiveRoutes.APPLICATION_JSON;

/**
 * @author thaivc
 * @since 2025
 */
@Slf4j
@RouteBase(produces = APPLICATION_JSON)
@RequiredArgsConstructor
public class CommandController {

    @Route(path = "/balance", methods = { Route.HttpMethod.GET })
    Uni<String> hello() {
        return Uni.createFrom().item("hello");
    }

}

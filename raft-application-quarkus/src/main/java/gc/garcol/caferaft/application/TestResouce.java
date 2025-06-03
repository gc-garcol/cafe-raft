package gc.garcol.caferaft.application;

import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

import static io.quarkus.vertx.web.ReactiveRoutes.APPLICATION_JSON;

/**
 * @author thaivc
 * @since 2025
 */
@Slf4j
@RouteBase(path = "test", produces = APPLICATION_JSON)
public class TestResouce {

    @Route(path = "/hello", methods = { Route.HttpMethod.GET })
    Uni<String> hello() {
        return Uni.createFrom().item("hello");
    }
}

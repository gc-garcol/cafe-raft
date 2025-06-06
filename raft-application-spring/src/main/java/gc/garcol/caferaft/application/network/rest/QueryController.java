package gc.garcol.caferaft.application.network.rest;

import gc.garcol.caferaft.application.payload.query.BalanceQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author thaivc
 * @since 2025
 */
@RestController
@RequiredArgsConstructor
public class QueryController {

    private final RequestDispatcher requestDispatcher;

    @GetMapping("/balance/{id}")
    Mono<?> getBalance(ServerWebExchange request, @PathVariable Long id) {
        return Mono.fromFuture(requestDispatcher.dispatch(request, new BalanceQuery(id)));
    }
}

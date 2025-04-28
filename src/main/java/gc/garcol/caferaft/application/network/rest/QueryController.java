package gc.garcol.caferaft.application.network.rest;

import gc.garcol.caferaft.application.payload.query.BalanceQuery;
import gc.garcol.caferaft.core.client.ClientResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

/**
 * @author thaivc
 * @since 2025
 */
@RestController
@RequiredArgsConstructor
public class QueryController {

    private final RequestDispatcher requestDispatcher;

    @GetMapping("/balance/{id}")
    CompletableFuture<ClientResponse> getBalance(@PathVariable Long id) {
        return requestDispatcher.dispatch(new BalanceQuery(id));
    }
}

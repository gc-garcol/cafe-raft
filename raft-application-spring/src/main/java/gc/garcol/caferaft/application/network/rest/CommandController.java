package gc.garcol.caferaft.application.network.rest;

import gc.garcol.caferaft.application.payload.command.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author thaivc
 * @since 2025
 */
@RestController
@RequiredArgsConstructor
public class CommandController {

    private final RequestDispatcher requestDispatcher;

    @PostMapping("/balance")
    Mono<?> createBalance(ServerWebExchange request, @RequestBody CreateBalanceCommand command) {
        return Mono.fromFuture(requestDispatcher.dispatch(request, command));
    }

    @PostMapping("/balance/deposit")
    Mono<?> deposit(ServerWebExchange request, @RequestBody DepositCommand command) {
        return Mono.fromFuture(requestDispatcher.dispatch(request, command));
    }

    @PostMapping("/balance/withdraw")
    Mono<?> withdraw(ServerWebExchange request, @RequestBody WithdrawCommand command) {
        return Mono.fromFuture(requestDispatcher.dispatch(request, command));
    }

    @PostMapping("/balance/transfer")
    Mono<?> transfer(ServerWebExchange request, @RequestBody TransferCommand command) {
        return Mono.fromFuture(requestDispatcher.dispatch(request, command));
    }

    @PostMapping("/balance/batch")
    Mono<?> batch(ServerWebExchange request, @RequestBody BatchBalanceCommand command) {
        return Mono.fromFuture(requestDispatcher.dispatch(request, command));
    }
}

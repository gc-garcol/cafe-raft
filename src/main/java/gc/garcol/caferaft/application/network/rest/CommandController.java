package gc.garcol.caferaft.application.network.rest;

import gc.garcol.caferaft.application.payload.command.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

/**
 * @author thaivc
 * @since 2025
 */
@RestController
@RequiredArgsConstructor
public class CommandController {

    private final RequestDispatcher requestDispatcher;

    @PostMapping("/balance")
    CompletableFuture<?> createBalance(
        HttpServletRequest request,
        @RequestBody CreateBalanceCommand command
    ) {
        return requestDispatcher.dispatch(request, command);
    }

    @PostMapping("/balance/deposit")
    CompletableFuture<?> deposit(
        HttpServletRequest request,
        @RequestBody DepositCommand command
    ) {
        return requestDispatcher.dispatch(request, command);
    }

    @PostMapping("/balance/withdraw")
    CompletableFuture<?> withdraw(
        HttpServletRequest request,
        @RequestBody WithdrawCommand command
    ) {
        return requestDispatcher.dispatch(request, command);
    }

    @PostMapping("/balance/transfer")
    CompletableFuture<?> transfer(
        HttpServletRequest request,
        @RequestBody TransferCommand command
    ) {
        return requestDispatcher.dispatch(request, command);
    }

    @PostMapping("/balance/batch")
    CompletableFuture<?> batch(
        HttpServletRequest request,
        @RequestBody BatchBalanceCommand command
    ) {
        return requestDispatcher.dispatch(request, command);
    }
}

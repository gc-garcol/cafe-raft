package gc.garcol.caferaft.application.network.rest;

import gc.garcol.caferaft.application.payload.command.CreateBalanceCommand;
import gc.garcol.caferaft.application.payload.command.DepositCommand;
import gc.garcol.caferaft.application.payload.command.TransferCommand;
import gc.garcol.caferaft.application.payload.command.WithdrawCommand;
import gc.garcol.caferaft.core.client.ClientResponse;
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
    CompletableFuture<ClientResponse> createBalance(@RequestBody CreateBalanceCommand command) {
        return requestDispatcher.dispatch(command);
    }

    @PostMapping("/balance/deposit")
    CompletableFuture<ClientResponse> deposit(@RequestBody DepositCommand command) {
        return requestDispatcher.dispatch(command);
    }

    @PostMapping("/balance/withdraw")
    CompletableFuture<ClientResponse> withdraw(@RequestBody WithdrawCommand command) {
        return requestDispatcher.dispatch(command);
    }

    @PostMapping("/balance/transfer")
    CompletableFuture<ClientResponse> transfer(@RequestBody TransferCommand command) {
        return requestDispatcher.dispatch(command);
    }


}

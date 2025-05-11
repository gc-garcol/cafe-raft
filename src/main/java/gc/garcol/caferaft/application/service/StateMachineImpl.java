package gc.garcol.caferaft.application.service;

import gc.garcol.caferaft.application.payload.command.*;
import gc.garcol.caferaft.application.payload.query.BalanceQuery;
import gc.garcol.caferaft.application.payload.query.BalanceQueryResponse;
import gc.garcol.caferaft.core.client.*;
import gc.garcol.caferaft.core.service.StateMachine;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * @author thaivc
 * @since 2025
 */
@Component
@RequiredArgsConstructor
public class StateMachineImpl implements StateMachine {

    private final BalanceStateMachine balanceStateMachine;

    @Override
    public void load() {

    }

    @Override
    public void snapshot() {

    }

    @Override
    public CommandResponse accept(Command command) {
        try {
            switch (command) {
                case CreateBalanceCommand createBalanceCommand ->
                    balanceStateMachine.createBalance(createBalanceCommand.id());
                case DepositCommand depositCommand ->
                    balanceStateMachine.deposit(depositCommand.id(), depositCommand.amount());
                case WithdrawCommand withdrawCommand ->
                    balanceStateMachine.withdraw(withdrawCommand.id(), withdrawCommand.amount());
                case BatchBalanceCommand batchBalanceCommand -> {
                    return balanceStateMachine.batch(batchBalanceCommand);
                }
                default -> {
                    return new CommandResponse(HttpStatus.BAD_REQUEST.value(), "Command not found!!");
                }
            }
            return new CommandResponse(HttpStatus.OK.value(), "OK");
        } catch (Exception e) {
            return new CommandResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Override
    public ClientResponse apply(Query query) {
        return switch (query) {
            case BalanceQuery balanceQuery -> {
                var balance = balanceStateMachine.getBalance(balanceQuery.id());
                yield BalanceQueryResponse.builder()
                    .id(balance.getId())
                    .amount(balance.getAmount())
                    .active(balance.isActive())
                    .build();
            }
            default -> new CommonErrorResponse(404, String.format("Query Not Found %s", query));
        };
    }
}

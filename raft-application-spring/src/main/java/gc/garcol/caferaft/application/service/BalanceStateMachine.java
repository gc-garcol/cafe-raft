package gc.garcol.caferaft.application.service;

import gc.garcol.caferaft.application.model.Balance;
import gc.garcol.caferaft.application.payload.command.BatchBalanceCommand;
import gc.garcol.caferaft.application.payload.command.BatchBalanceResponse;
import gc.garcol.caferaft.application.payload.command.ModifyBalanceResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe state machine for managing account balances.
 * Provides operations for creating, reading, updating, and deleting balances.
 *
 * @author thaivc
 * @since 2025
 */
@Component
public class BalanceStateMachine {

    private final Map<Long, Balance> balances = new ConcurrentHashMap<>();

    /**
     * Retrieves a balance by its ID.
     *
     * @param id The balance ID
     * @return The balance
     * @throws IllegalArgumentException if balance not found
     * @throws NullPointerException     if id is null
     */
    public Balance getBalance(Long id) {
        Objects.requireNonNull(id, "Balance ID cannot be null");
        Balance balance = balances.get(id);
        if (balance == null) {
            throw new IllegalArgumentException(String.format("Balance with id %s not found", id));
        }
        return balance;
    }

    /**
     * Creates a new balance with the specified ID.
     *
     * @param id The balance ID
     * @return The newly created balance
     * @throws IllegalArgumentException if balance already exists
     * @throws NullPointerException     if id is null
     */
    public Balance createBalance(Long id) {
        Objects.requireNonNull(id, "Balance ID cannot be null");
        Balance balance = new Balance();
        balance.setId(id);
        Balance existing = balances.putIfAbsent(id, balance);
        if (existing != null) {
            throw new IllegalArgumentException(String.format("Balance with id %s already exists", id));
        }
        return balance;
    }

    public BatchBalanceResponse batch(BatchBalanceCommand batchBalanceCommand) {
        var results = batchBalanceCommand.getCommands().stream().map(command -> {
            try {
                if (command.getCreateBalanceCommand() != null) {
                    this.createBalance(command.getCreateBalanceCommand().id());
                    return new ModifyBalanceResponse(command.getCorrelationId(), HttpStatus.OK.value(), "OK");
                }

                if (command.getDepositCommand() != null) {
                    this.deposit(command.getDepositCommand().id(), command.getDepositCommand().amount());
                    return new ModifyBalanceResponse(command.getCorrelationId(), HttpStatus.OK.value(), "OK");
                }

                if (command.getWithdrawCommand() != null) {
                    this.deposit(command.getWithdrawCommand().id(), command.getWithdrawCommand().amount());
                    return new ModifyBalanceResponse(command.getCorrelationId(), HttpStatus.OK.value(), "OK");
                }

                // todo add more ...

                return new ModifyBalanceResponse(command.getCorrelationId(), HttpStatus.BAD_REQUEST.value(),
                        "Command not found");
            } catch (Exception e) {
                return new ModifyBalanceResponse(command.getCorrelationId(), HttpStatus.BAD_REQUEST.value(),
                        e.getMessage());
            }
        }).toList();
        return new BatchBalanceResponse(results);
    }

    /**
     * Deposits the specified amount to a balance.
     *
     * @param id     The balance ID
     * @param amount The amount to deposit
     * @return The updated balance
     * @throws IllegalArgumentException if balance not found or amount is negative
     * @throws NullPointerException     if id or amount is null
     */
    public Balance deposit(Long id, BigInteger amount) {
        Objects.requireNonNull(id, "Balance ID cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        if (amount.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("Deposit amount cannot be negative");
        }

        Balance balance = getBalance(id);
        balance.deposit(amount);
        return balance;
    }

    /**
     * Withdraws the specified amount from a balance.
     *
     * @param id     The balance ID
     * @param amount The amount to withdraw
     * @return The updated balance
     * @throws IllegalArgumentException if balance not found, amount is negative, or insufficient funds
     * @throws NullPointerException     if id or amount is null
     */
    public Balance withdraw(Long id, BigInteger amount) {
        Objects.requireNonNull(id, "Balance ID cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        if (amount.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("Withdrawal amount cannot be negative");
        }

        Balance balance = getBalance(id);
        balance.withdraw(amount);
        return balance;
    }

    public void transfer(Long from, Long to, BigInteger amount) {
        Objects.requireNonNull(from, "From balance ID cannot be null");
        Objects.requireNonNull(to, "To balance ID cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        if (amount.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("Transfer amount cannot be negative");
        }

        Balance fromBalance = getBalance(from);
        Balance toBalance = getBalance(to);

        if (fromBalance.getId() == toBalance.getId()) {
            throw new IllegalArgumentException("Source and destination balances cannot be the same");
        }

        if (fromBalance.getAmount().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        fromBalance.withdraw(amount);
        toBalance.deposit(amount);
    }

    /**
     * Sets the active status of a balance.
     *
     * @param id     The balance ID
     * @param active The active status to set
     * @return The updated balance
     * @throws IllegalArgumentException if balance not found
     * @throws NullPointerException     if id is null
     */
    public Balance setActive(Long id, boolean active) {
        Objects.requireNonNull(id, "Balance ID cannot be null");
        Balance balance = getBalance(id);
        balance.setActive(active);
        return balance;
    }

    /**
     * Checks if a balance exists.
     *
     * @param id The balance ID
     * @return true if the balance exists, false otherwise
     * @throws NullPointerException if id is null
     */
    public boolean exists(Long id) {
        Objects.requireNonNull(id, "Balance ID cannot be null");
        return balances.containsKey(id);
    }

    /**
     * Deletes a balance.
     *
     * @param id The balance ID
     * @return true if the balance was deleted, false if it didn't exist
     * @throws NullPointerException if id is null
     */
    public boolean delete(Long id) {
        Objects.requireNonNull(id, "Balance ID cannot be null");
        return balances.remove(id) != null;
    }

    /**
     * Returns an unmodifiable view of all balances.
     *
     * @return Map of all balances
     */
    public Map<Long, Balance> getAllBalances() {
        return Collections.unmodifiableMap(balances);
    }
}

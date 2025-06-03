package gc.garcol.caferaft.application.model;

import lombok.Data;

import java.math.BigInteger;

/**
 * @author thaivc
 * @since 2025
 */
@Data
public class Balance {
    private long       id;
    private BigInteger amount = BigInteger.ZERO;
    private boolean    active = false;

    public Balance deposit(BigInteger amount) {
        this.amount = this.amount.add(amount);
        return this;
    }

    public Balance withdraw(BigInteger amount) {
        if (this.amount.compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    String.format("Insufficient balance %s in account %s", this.amount, this.id));
        }
        this.amount = this.amount.subtract(amount);
        return this;
    }

    public Balance setActive(boolean active) {
        this.active = active;
        return this;
    }
}

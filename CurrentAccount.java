package atm;

import java.math.BigDecimal;

public class CurrentAccount extends Account {
    private BigDecimal overdraftLimit = BigDecimal.valueOf(1000); // allowed negative

    public CurrentAccount(int accountId, String ownerName, BigDecimal balance) {
        super(accountId, ownerName, balance);
    }

    @Override
    public boolean canWithdraw(BigDecimal amount) {
        return balance.subtract(amount).compareTo(overdraftLimit.negate()) >= 0;
    }
}

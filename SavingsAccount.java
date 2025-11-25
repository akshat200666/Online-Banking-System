package atm;

import java.math.BigDecimal;

public class SavingsAccount extends Account {
    private BigDecimal minimumBalance = BigDecimal.valueOf(100); // simple example

    public SavingsAccount(int accountId, String ownerName, BigDecimal balance) {
        super(accountId, ownerName, balance);
    }

    @Override
    public boolean canWithdraw(BigDecimal amount) {
        return balance.subtract(amount).compareTo(minimumBalance) >= 0;
    }
}

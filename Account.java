package atm;

import java.math.BigDecimal;

public abstract class Account {
    protected int accountId;
    protected String ownerName;
    protected BigDecimal balance;

    public Account(int accountId, String ownerName, BigDecimal balance) {
        this.accountId = accountId;
        this.ownerName = ownerName;
        this.balance = balance;
    }

    public int getAccountId() { return accountId; }
    public String getOwnerName() { return ownerName; }

    public synchronized BigDecimal getBalance() { return balance; }

    public abstract boolean canWithdraw(BigDecimal amount);

    // polymorphic withdraw & deposit
    public synchronized void withdraw(BigDecimal amount) throws IllegalArgumentException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be > 0");
        if (!canWithdraw(amount)) throw new IllegalArgumentException("Insufficient funds or rules block withdrawal");
        balance = balance.subtract(amount);
    }

    public synchronized void deposit(BigDecimal amount) throws IllegalArgumentException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be > 0");
        balance = balance.add(amount);
    }

    @Override
    public String toString() {
        return "Account{" + "accountId=" + accountId + ", ownerName='" + ownerName + '\'' + ", balance=" + balance + '}';
    }
}

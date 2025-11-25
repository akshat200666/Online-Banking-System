package atm;

import java.math.BigDecimal;

public class TransactionWorker extends Thread {
    private final AccountDAO dao;
    private final int accountId;
    private final BigDecimal amount;
    private final boolean isWithdraw;

    public TransactionWorker(AccountDAO dao, int accountId, BigDecimal amount, boolean isWithdraw) {
        this.dao = dao;
        this.accountId = accountId;
        this.amount = amount;
        this.isWithdraw = isWithdraw;
    }

    @Override
    public void run() {
        try {
            if (isWithdraw) {
                dao.withdraw(accountId, amount);
                System.out.println("Withdrawn " + amount + " from " + accountId + " by " + Thread.currentThread().getName());
            } else {
                dao.deposit(accountId, amount);
                System.out.println("Deposited " + amount + " to " + accountId + " by " + Thread.currentThread().getName());
            }
        } catch (DatabaseException e) {
            System.err.println("Transaction failed for account " + accountId + ": " + e.getMessage());
        }
    }
}

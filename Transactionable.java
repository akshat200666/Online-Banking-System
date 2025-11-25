package atm;

import java.math.BigDecimal;

public interface Transactionable {
    void deposit(int accountId, BigDecimal amount) throws DatabaseException;
    void withdraw(int accountId, BigDecimal amount) throws DatabaseException;
    void transfer(int fromAccountId, int toAccountId, BigDecimal amount) throws DatabaseException;
}

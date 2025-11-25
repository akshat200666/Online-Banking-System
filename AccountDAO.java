package atm;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO implements Transactionable {
    public AccountDAO() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // driver not found
            e.printStackTrace();
        }
    }

    public Account findById(int id) throws DatabaseException {
        String sql = "SELECT account_id, owner_name, balance, type FROM accounts WHERE account_id = ?";
        try (Connection conn = DriverManager.getConnection(DBConfig.URL, DBConfig.USER, DBConfig.PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String type = rs.getString("type");
                    int accountId = rs.getInt("account_id");
                    String owner = rs.getString("owner_name");
                    BigDecimal bal = rs.getBigDecimal("balance");
                    if ("SAVINGS".equalsIgnoreCase(type)) return new SavingsAccount(accountId, owner, bal);
                    else return new CurrentAccount(accountId, owner, bal);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding account", e);
        }
    }

    public void updateBalance(Connection conn, int accountId, BigDecimal newBalance) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE accounts SET balance = ? WHERE account_id = ?")) {
            ps.setBigDecimal(1, newBalance);
            ps.setInt(2, accountId);
            ps.executeUpdate();
        }
    }

    public List<String> getTransactions(int accountId) throws DatabaseException {
        String sql = "SELECT timestamp, type, amount, remark FROM transactions WHERE account_id = ? ORDER BY timestamp DESC";
        List<String> out = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DBConfig.URL, DBConfig.USER, DBConfig.PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(rs.getTimestamp("timestamp") + " " + rs.getString("type") + " " + rs.getBigDecimal("amount") + " " + rs.getString("remark"));
                }
            }
            return out;
        } catch (SQLException e) {
            throw new DatabaseException("Error loading transactions", e);
        }
    }

    private void insertTransaction(Connection conn, int accountId, String type, BigDecimal amount, String remark) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO transactions(account_id, type, amount, remark) VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, accountId);
            ps.setString(2, type);
            ps.setBigDecimal(3, amount);
            ps.setString(4, remark);
            ps.executeUpdate();
        }
    }

    // Transactional withdraw
    @Override
    public void withdraw(int accountId, java.math.BigDecimal amount) throws DatabaseException {
        String sel = "SELECT balance, type, owner_name FROM accounts WHERE account_id = ? FOR UPDATE";
        try (Connection conn = DriverManager.getConnection(DBConfig.URL, DBConfig.USER, DBConfig.PASS)) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sel)) {
                ps.setInt(1, accountId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new DatabaseException("Account not found", null);
                    BigDecimal balance = rs.getBigDecimal("balance");
                    String type = rs.getString("type");
                    // create a local Account object to use canWithdraw logic
                    Account a = "SAVINGS".equalsIgnoreCase(type) ? new SavingsAccount(accountId, rs.getString("owner_name"), balance)
                            : new CurrentAccount(accountId, rs.getString("owner_name"), balance);
                    synchronized (a) {
                        if (!a.canWithdraw(amount)) throw new DatabaseException("Insufficient funds", null);
                        BigDecimal newBalance = balance.subtract(amount);
                        updateBalance(conn, accountId, newBalance);
                        insertTransaction(conn, accountId, "WITHDRAW", amount, "ATM withdraw");
                    }
                }
            }
            conn.commit();
        } catch (SQLException e) {
            throw new DatabaseException("Error during withdraw", e);
        }
    }

    @Override
    public void deposit(int accountId, java.math.BigDecimal amount) throws DatabaseException {
        try (Connection conn = DriverManager.getConnection(DBConfig.URL, DBConfig.USER, DBConfig.PASS)) {
            conn.setAutoCommit(false);
            BigDecimal balance;
            try (PreparedStatement ps = conn.prepareStatement("SELECT balance, owner_name FROM accounts WHERE account_id = ? FOR UPDATE")) {
                ps.setInt(1, accountId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new DatabaseException("Account not found", null);
                    balance = rs.getBigDecimal("balance");
                    BigDecimal newBalance = balance.add(amount);
                    updateBalance(conn, accountId, newBalance);
                    insertTransaction(conn, accountId, "DEPOSIT", amount, "ATM deposit");
                }
            }
            conn.commit();
        } catch (SQLException e) {
            throw new DatabaseException("Error during deposit", e);
        }
    }

    @Override
    public void transfer(int fromAccountId, int toAccountId, java.math.BigDecimal amount) throws DatabaseException {
        try (Connection conn = DriverManager.getConnection(DBConfig.URL, DBConfig.USER, DBConfig.PASS)) {
            conn.setAutoCommit(false);
            // lock both accounts (order by id to avoid deadlocks)
            int a1 = Math.min(fromAccountId, toAccountId);
            int a2 = Math.max(fromAccountId, toAccountId);
            try (PreparedStatement p1 = conn.prepareStatement("SELECT account_id, balance, type, owner_name FROM accounts WHERE account_id IN (?, ?) FOR UPDATE")) {
                p1.setInt(1, a1);
                p1.setInt(2, a2);
                try (ResultSet rs = p1.executeQuery()) {
                    BigDecimal balFrom = null, balTo = null;
                    String typeFrom = null, ownerFrom = null;
                    while (rs.next()) {
                        int id = rs.getInt("account_id");
                        if (id == fromAccountId) {
                            balFrom = rs.getBigDecimal("balance");
                            typeFrom = rs.getString("type");
                            ownerFrom = rs.getString("owner_name");
                        } else if (id == toAccountId) {
                            balTo = rs.getBigDecimal("balance");
                        }
                    }
                    if (balFrom == null || balTo == null) throw new DatabaseException("One of accounts not found", null);
                    Account fromAcc = "SAVINGS".equalsIgnoreCase(typeFrom) ? new SavingsAccount(fromAccountId, ownerFrom, balFrom) : new CurrentAccount(fromAccountId, ownerFrom, balFrom);
                    synchronized (fromAcc) {
                        if (!fromAcc.canWithdraw(amount)) throw new DatabaseException("Insufficient funds for transfer", null);
                        BigDecimal newFrom = balFrom.subtract(amount);
                        BigDecimal newTo = balTo.add(amount);
                        updateBalance(conn, fromAccountId, newFrom);
                        updateBalance(conn, toAccountId, newTo);
                        insertTransaction(conn, fromAccountId, "TRANSFER_OUT", amount, "Transfer to " + toAccountId);
                        insertTransaction(conn, toAccountId, "TRANSFER_IN", amount, "Transfer from " + fromAccountId);
                    }
                }
            }
            conn.commit();
        } catch (SQLException e) {
            throw new DatabaseException("Error during transfer", e);
        }
    }
}

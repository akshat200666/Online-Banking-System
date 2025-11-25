package atm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.util.List;

public class ATMGui extends JFrame {
    private final AccountDAO dao = new AccountDAO();
    private Account currentAccount;
    private final InMemoryCache cache = new InMemoryCache();

    // UI components
    private final JTextField txtAccountId = new JTextField(10);
    private final JLabel lblName = new JLabel("-");
    private final JLabel lblBalance = new JLabel("-");
    private final JTextArea txtArea = new JTextArea(8, 30);

    public ATMGui() {
        super("ATM Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 420);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JPanel top = new JPanel();
        top.add(new JLabel("Account ID:"));
        top.add(txtAccountId);
        JButton btnLoad = new JButton("Load Account");
        top.add(btnLoad);

        JPanel info = new JPanel(new GridLayout(2, 2));
        info.add(new JLabel("Name:"));
        info.add(lblName);
        info.add(new JLabel("Balance:"));
        info.add(lblBalance);

        JPanel actions = new JPanel();
        JButton btnWithdraw = new JButton("Withdraw");
        JButton btnDeposit = new JButton("Deposit");
        JButton btnTransfer = new JButton("Transfer");
        JButton btnTransactions = new JButton("Transactions");

        actions.add(btnWithdraw);
        actions.add(btnDeposit);
        actions.add(btnTransfer);
        actions.add(btnTransactions);

        JPanel center = new JPanel(new BorderLayout());
        txtArea.setEditable(false);
        center.add(new JScrollPane(txtArea), BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(info, BorderLayout.WEST);
        add(actions, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        btnLoad.addActionListener(this::onLoad);
        btnWithdraw.addActionListener(this::onWithdraw);
        btnDeposit.addActionListener(this::onDeposit);
        btnTransfer.addActionListener(this::onTransfer);
        btnTransactions.addActionListener(this::onTransactions);
    }

    private void onLoad(ActionEvent e) {
    try {
        int id = Integer.parseInt(txtAccountId.getText().trim());
        String pin = JOptionPane.showInputDialog(this, "Enter PIN for account " + id + ":");
        if (pin == null) return; // cancelled
        // verify pin hash from DB
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(DBConfig.URL, DBConfig.USER, DBConfig.PASS);
             java.sql.PreparedStatement ps = conn.prepareStatement("SELECT pin_hash FROM accounts WHERE account_id = ?")) {
            ps.setInt(1, id);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String pinHash = rs.getString("pin_hash"); // may be null if not set
                    if (pinHash == null || pinHash.isEmpty()) {
                        showMessage("No PIN set for this account on server. Contact admin.");
                        return;
                    }
                    if (!HashUtil.sha256Hex(pin).equalsIgnoreCase(pinHash)) {
                        showMessage("Invalid PIN");
                        return;
                    }
                } else {
                    showMessage("Account not found");
                    return;
                }
            }
        } catch (java.sql.SQLException ex) {
            showMessage("DB error during PIN check: " + ex.getMessage());
            return;
        }

        // try cache first
        Account a = cache.get(id);
        if (a == null) {
            a = dao.findById(id);
            if (a != null) cache.put(a);
        }
        if (a == null) {
            showMessage("Account not found");
            return;
        }
        currentAccount = a;
        lblName.setText(a.getOwnerName());
        lblBalance.setText(a.getBalance().toString());
        txtArea.setText("Loaded account: " + a);
    } catch (NumberFormatException ex) {
        showMessage("Invalid account id");
    } catch (DatabaseException ex) {
        showMessage("DB error: " + ex.getMessage());
    }
}
private void onWithdraw(ActionEvent e) {
        if (!checkLoaded()) return;
        String s = JOptionPane.showInputDialog(this, "Amount to withdraw:");
        if (s == null) return;
        try {
            BigDecimal amt = new BigDecimal(s);
            // start worker thread (demo multithreading)
            new TransactionWorker(dao, currentAccount.getAccountId(), amt, true).start();
            refreshBalance();
        } catch (NumberFormatException ex) {
            showMessage("Invalid amount");
        }
    }

    private void onDeposit(ActionEvent e) {
        if (!checkLoaded()) return;
        String s = JOptionPane.showInputDialog(this, "Amount to deposit:");
        if (s == null) return;
        try {
            BigDecimal amt = new BigDecimal(s);
            new TransactionWorker(dao, currentAccount.getAccountId(), amt, false).start();
            refreshBalance();
        } catch (NumberFormatException ex) {
            showMessage("Invalid amount");
        }
    }

    private void onTransfer(ActionEvent e) {
        if (!checkLoaded()) return;
        String sTo = JOptionPane.showInputDialog(this, "Recipient account id:");
        if (sTo == null) return;
        String sAmt = JOptionPane.showInputDialog(this, "Amount to transfer:");
        if (sAmt == null) return;
        try {
            int toId = Integer.parseInt(sTo);
            BigDecimal amt = new BigDecimal(sAmt);
            // perform transfer in background thread
            new Thread(() -> {
                try {
                    dao.transfer(currentAccount.getAccountId(), toId, amt);
                    SwingUtilities.invokeLater(this::refreshBalance);
                } catch (DatabaseException ex) {
                    SwingUtilities.invokeLater(() -> showMessage("Transfer failed: " + ex.getMessage()));
                }
            }).start();
        } catch (NumberFormatException ex) {
            showMessage("Invalid input");
        }
    }

    private void onTransactions(ActionEvent e) {
        if (!checkLoaded()) return;
        try {
            List<String> tx = dao.getTransactions(currentAccount.getAccountId());
            txtArea.setText("Transactions:\n");
            for (String t : tx) txtArea.append(t + "\n");
        } catch (DatabaseException ex) {
            showMessage("Error loading transactions");
        }
    }

    private boolean checkLoaded() {
        if (currentAccount == null) {
            showMessage("Load an account first");
            return false;
        }
        return true;
    }

    private void refreshBalance() {
        try {
            Account a = dao.findById(currentAccount.getAccountId());
            if (a != null) {
                currentAccount = a;
                cache.put(a);
                lblBalance.setText(a.getBalance().toString());
                txtArea.append("\nBalance refreshed: " + a.getBalance());
            }
        } catch (DatabaseException ex) {
            showMessage("Error refreshing: " + ex.getMessage());
        }
    }

    private void showMessage(String s) {
        JOptionPane.showMessageDialog(this, s);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ATMGui gui = new ATMGui();
            gui.setVisible(true);
        });
    }
}

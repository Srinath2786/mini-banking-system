import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main extends JFrame {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/minibank";
    private static final String DB_USER = "root"; // change if needed
    private static final String DB_PASSWORD = ""; // change if needed

    private Connection connection;

    private JPanel mainPanel;
    private JTextField accountNumberField;
    private JTextField amountField;
    private JTextField nameField;
    private JTextField balanceField;
    private JTextArea displayArea;
    private JButton createButton;
    private JButton depositButton;
    private JButton withdrawButton;
    private JButton balanceButton;
    private JButton exitButton;

    public Main() {
        initializeGUI();
        connectDatabase();
    }

    private void connectDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            logInfo("Connected to database successfully.");
            setActionButtonsEnabled(true);
        } catch (ClassNotFoundException e) {
            logError("MySQL JDBC driver not found. Check classpath.");
            setActionButtonsEnabled(false);
        } catch (SQLException e) {
            logError("Database connection failed: " + e.getMessage());
            setActionButtonsEnabled(false);
        }
    }

    private void initializeGUI() {
        setTitle("Mini Banking System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(650, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(240, 248, 255));

        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(70, 130, 180));
        JLabel titleLabel = new JLabel("Mini Banking System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);

        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        inputPanel.setBackground(new Color(240, 248, 255));

        inputPanel.add(new JLabel("Account Number:"));
        accountNumberField = new JTextField();
        inputPanel.add(accountNumberField);

        inputPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        inputPanel.add(nameField);

        inputPanel.add(new JLabel("Initial Balance:"));
        balanceField = new JTextField();
        inputPanel.add(balanceField);

        inputPanel.add(new JLabel("Transaction Amount:"));
        amountField = new JTextField();
        inputPanel.add(amountField);

        inputPanel.add(new JLabel("Messages:"));
        displayArea = new JTextArea(5, 40);
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        inputPanel.add(new JScrollPane(displayArea));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(240, 248, 255));

        createButton = new JButton("Create Account");
        depositButton = new JButton("Deposit");
        withdrawButton = new JButton("Withdraw");
        balanceButton = new JButton("Check Balance");
        exitButton = new JButton("Exit");

        Dimension buttonSize = new Dimension(120, 35);
        createButton.setPreferredSize(buttonSize);
        depositButton.setPreferredSize(buttonSize);
        withdrawButton.setPreferredSize(buttonSize);
        balanceButton.setPreferredSize(buttonSize);
        exitButton.setPreferredSize(buttonSize);

        createButton.addActionListener(e -> createAccount());
        depositButton.addActionListener(e -> deposit());
        withdrawButton.addActionListener(e -> withdraw());
        balanceButton.addActionListener(e -> checkBalance());
        exitButton.addActionListener(e -> exitApplication());

        buttonPanel.add(createButton);
        buttonPanel.add(depositButton);
        buttonPanel.add(withdrawButton);
        buttonPanel.add(balanceButton);
        buttonPanel.add(exitButton);

        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);

        setActionButtonsEnabled(false);
        setVisible(true);
    }

    private void setActionButtonsEnabled(boolean enabled) {
        createButton.setEnabled(enabled);
        depositButton.setEnabled(enabled);
        withdrawButton.setEnabled(enabled);
        balanceButton.setEnabled(enabled);
    }

    private void createAccount() {
        if (connection == null) {
            logError("Database is not connected.");
            return;
        }

        try {
            String accountText = accountNumberField.getText().trim();
            String name = nameField.getText().trim();
            String balanceText = balanceField.getText().trim();

            if (accountText.isEmpty() || name.isEmpty() || balanceText.isEmpty()) {
                logError("Please fill account number, name, and initial balance.");
                return;
            }

            int accountNumber = Integer.parseInt(accountText);
            double openingBalance = Double.parseDouble(balanceText);

            if (openingBalance < 0) {
                logError("Initial balance cannot be negative.");
                return;
            }

            String sql = "INSERT INTO account (acc_no, name, balance) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, accountNumber);
                statement.setString(2, name);
                statement.setDouble(3, openingBalance);
                statement.executeUpdate();
            }

            logInfo("Account created successfully.");
            clearInputFields();
        } catch (NumberFormatException ex) {
            logError("Invalid number format.");
        } catch (SQLException ex) {
            logError("Database error: " + ex.getMessage());
        }
    }

    private void deposit() {
        if (connection == null) {
            logError("Database is not connected.");
            return;
        }

        try {
            String accountText = accountNumberField.getText().trim();
            String amountText = amountField.getText().trim();

            if (accountText.isEmpty() || amountText.isEmpty()) {
                logError("Please enter account number and amount.");
                return;
            }

            int accountNumber = Integer.parseInt(accountText);
            double amount = Double.parseDouble(amountText);

            if (amount <= 0) {
                logError("Deposit amount must be greater than zero.");
                return;
            }

            String sql = "UPDATE account SET balance = balance + ? WHERE acc_no = ?";
            int updatedRows;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setDouble(1, amount);
                statement.setInt(2, accountNumber);
                updatedRows = statement.executeUpdate();
            }

            if (updatedRows > 0) {
                logInfo(String.format("Deposit successful. Amount: INR %.2f", amount));
                clearInputFields();
            } else {
                logError("Account not found.");
            }
        } catch (NumberFormatException ex) {
            logError("Invalid number format.");
        } catch (SQLException ex) {
            logError("Database error: " + ex.getMessage());
        }
    }

    private void withdraw() {
        if (connection == null) {
            logError("Database is not connected.");
            return;
        }

        try {
            String accountText = accountNumberField.getText().trim();
            String amountText = amountField.getText().trim();

            if (accountText.isEmpty() || amountText.isEmpty()) {
                logError("Please enter account number and amount.");
                return;
            }

            int accountNumber = Integer.parseInt(accountText);
            double amount = Double.parseDouble(amountText);

            if (amount <= 0) {
                logError("Withdrawal amount must be greater than zero.");
                return;
            }

            String balanceQuery = "SELECT balance FROM account WHERE acc_no = ?";
            try (PreparedStatement checkStatement = connection.prepareStatement(balanceQuery)) {
                checkStatement.setInt(1, accountNumber);

                try (ResultSet resultSet = checkStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        logError("Account not found.");
                        return;
                    }

                    double currentBalance = resultSet.getDouble("balance");
                    if (currentBalance < amount) {
                        logError("Insufficient balance.");
                        return;
                    }
                }
            }

            String updateSql = "UPDATE account SET balance = balance - ? WHERE acc_no = ?";
            try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                updateStatement.setDouble(1, amount);
                updateStatement.setInt(2, accountNumber);
                updateStatement.executeUpdate();
            }

            logInfo(String.format("Withdrawal successful. Amount: INR %.2f", amount));
            clearInputFields();
        } catch (NumberFormatException ex) {
            logError("Invalid number format.");
        } catch (SQLException ex) {
            logError("Database error: " + ex.getMessage());
        }
    }

    private void checkBalance() {
        if (connection == null) {
            logError("Database is not connected.");
            return;
        }

        try {
            String accountText = accountNumberField.getText().trim();
            if (accountText.isEmpty()) {
                logError("Please enter an account number.");
                return;
            }

            int accountNumber = Integer.parseInt(accountText);

            String sql = "SELECT acc_no, name, balance FROM account WHERE acc_no = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, accountNumber);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        displayArea.append("-------------------------------------\n");
                        displayArea.append("Account No : " + resultSet.getInt("acc_no") + "\n");
                        displayArea.append("Name       : " + resultSet.getString("name") + "\n");
                        displayArea.append(
                            String.format("Balance    : INR %.2f%n", resultSet.getDouble("balance"))
                        );
                        displayArea.append("-------------------------------------\n");
                    } else {
                        logError("Account not found.");
                    }
                }
            }
        } catch (NumberFormatException ex) {
            logError("Invalid number format.");
        } catch (SQLException ex) {
            logError("Database error: " + ex.getMessage());
        }
    }

    private void clearInputFields() {
        accountNumberField.setText("");
        nameField.setText("");
        balanceField.setText("");
        amountField.setText("");
    }

    private void logInfo(String message) {
        displayArea.append("[INFO] " + message + "\n");
    }

    private void logError(String message) {
        displayArea.append("[ERROR] " + message + "\n");
    }

    private void exitApplication() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Failed to close connection: " + e.getMessage());
            }
        }
        JOptionPane.showMessageDialog(this, "Thank you for using Mini Banking System.", "Exit", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Keep default look and feel if system LAF is unavailable.
        }

        SwingUtilities.invokeLater(Main::new);
    }
}

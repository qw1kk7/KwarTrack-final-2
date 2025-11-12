package com.mycompany.labopr.database;

import com.mycompany.labopr.data.TransactionData;
import com.mycompany.labopr.data.BudgetData;
import java.sql.*;
import java.util.*;

/**
 * Enhanced Database Access Object with real-time balance tracking
 * and full CRUD operations for KwarTrack
 */
public class DatabaseDAO {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/kwartrack_db?zeroDateTimeBehavior=CONVERT_TO_NULL";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    private static DatabaseDAO instance;
    private static Integer currentUserId = null;

    private DatabaseDAO() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }
    }

    public static synchronized DatabaseDAO getInstance() {
        if (instance == null) {
            instance = new DatabaseDAO();
        }
        return instance;
    }

    public static void setCurrentUserId(Integer userId) {
        currentUserId = userId;
    }

    public static Integer getCurrentUserId() {
        return currentUserId;
    }

    public static void clearSession() {
        currentUserId = null;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private void initializeDatabase() {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                user_id INT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) UNIQUE NOT NULL,
                password_hash VARCHAR(64) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        String createTransactionsTable = """
            CREATE TABLE IF NOT EXISTS transactions (
                transaction_id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                transaction_type ENUM('Income', 'Expenses') NOT NULL,
                transaction_date DATE NOT NULL,
                category VARCHAR(50) NOT NULL,
                amount DECIMAL(15, 2) NOT NULL,
                comment TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                INDEX idx_user_type (user_id, transaction_type),
                INDEX idx_user_date (user_id, transaction_date)
            )
        """;

        String createBudgetsTable = """
            CREATE TABLE IF NOT EXISTS budgets (
                budget_id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                category VARCHAR(50) NOT NULL,
                month VARCHAR(7) NOT NULL,
                goal_amount DECIMAL(15, 2) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE KEY unique_user_category_month (user_id, category, month),
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                INDEX idx_user_month (user_id, month)
            )
        """;

        String createCategoriesTable = """
            CREATE TABLE IF NOT EXISTS categories (
                category_id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                category_type ENUM('Income', 'Expenses') NOT NULL,
                category_name VARCHAR(50) NOT NULL,
                is_default BOOLEAN NOT NULL DEFAULT TRUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE KEY unique_user_type_name (user_id, category_type, category_name),
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
            )
        """;

        String createBalanceTable = """
            CREATE TABLE IF NOT EXISTS user_balance (
                balance_id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT UNIQUE NOT NULL,
                starting_balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
                current_balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
            )
        """;

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createTransactionsTable);
            stmt.execute(createBudgetsTable);
            stmt.execute(createCategoriesTable);
            stmt.execute(createBalanceTable);
            
            // Add current_balance column if it doesn't exist (for existing databases)
            try {
                stmt.execute("ALTER TABLE user_balance ADD COLUMN current_balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00");
                System.out.println("Added current_balance column to user_balance table");
            } catch (SQLException e) {
                // Column already exists, ignore
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    // ==================== USER OPERATIONS ====================

    public boolean createUser(String username, String passwordHash) {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);
            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int userId = rs.getInt(1);
                        initializeUserDefaults(userId);
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void initializeUserDefaults(int userId) {
        String balanceSql = "INSERT INTO user_balance (user_id, starting_balance, current_balance) VALUES (?, 0.00, 0.00)";
        String insertCategories = """
            INSERT INTO categories (user_id, category_name, category_type, is_default) VALUES
            (?, 'Health', 'Expenses', TRUE), (?, 'Leisure', 'Expenses', TRUE),
            (?, 'Home', 'Expenses', TRUE), (?, 'Food', 'Expenses', TRUE),
            (?, 'Education', 'Expenses', TRUE), (?, 'Gifts', 'Expenses', TRUE),
            (?, 'Groceries', 'Expenses', TRUE), (?, 'Family', 'Expenses', TRUE),
            (?, 'Workout', 'Expenses', TRUE), (?, 'Transportation', 'Expenses', TRUE),
            (?, 'Other', 'Expenses', TRUE), (?, 'Paycheck', 'Income', TRUE),
            (?, 'Gift', 'Income', TRUE), (?, 'Interest', 'Income', TRUE),
            (?, 'Other', 'Income', TRUE)
        """;

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement balanceStmt = conn.prepareStatement(balanceSql);
                 PreparedStatement catStmt = conn.prepareStatement(insertCategories)) {

                balanceStmt.setInt(1, userId);
                balanceStmt.executeUpdate();

                for (int i = 1; i <= 15; i++) catStmt.setInt(i, userId);
                catStmt.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Integer authenticateUser(String username, String passwordHash) {
        String sql = "SELECT user_id FROM users WHERE username = ? AND password_hash = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean userExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== TRANSACTION OPERATIONS ====================

    public boolean createTransaction(TransactionData.Transaction transaction) {
        if (currentUserId == null) return false;
        
        String sql = "INSERT INTO transactions (user_id, transaction_type, transaction_date, category, amount, comment) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentUserId);
            pstmt.setString(2, transaction.type);
            pstmt.setString(3, transaction.date);
            pstmt.setString(4, transaction.category);
            pstmt.setDouble(5, transaction.amount);
            pstmt.setString(6, transaction.comment);

            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                updateCurrentBalanceInDB();
            }
            return success;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTransaction(String originalDate, String originalCategory, double originalAmount, 
                                     TransactionData.Transaction updatedTransaction) {
        if (currentUserId == null) return false;
        
        String sql = """
            UPDATE transactions 
            SET transaction_type = ?, transaction_date = ?, category = ?, amount = ?, comment = ?
            WHERE user_id = ? AND transaction_date = ? AND category = ? AND amount = ?
            LIMIT 1
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, updatedTransaction.type);
            pstmt.setString(2, updatedTransaction.date);
            pstmt.setString(3, updatedTransaction.category);
            pstmt.setDouble(4, updatedTransaction.amount);
            pstmt.setString(5, updatedTransaction.comment);
            pstmt.setInt(6, currentUserId);
            pstmt.setString(7, originalDate);
            pstmt.setString(8, originalCategory);
            pstmt.setDouble(9, originalAmount);

            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                updateCurrentBalanceInDB();
            }
            return success;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTransaction(String date, String category, double amount) {
        if (currentUserId == null) return false;
        
        String sql = """
            DELETE FROM transactions 
            WHERE user_id = ? AND transaction_date = ? AND category = ? AND amount = ?
            LIMIT 1
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentUserId);
            pstmt.setString(2, date);
            pstmt.setString(3, category);
            pstmt.setDouble(4, amount);

            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                updateCurrentBalanceInDB();
            }
            return success;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<TransactionData.Transaction> getTransactionsByType(String type) {
        if (currentUserId == null) return new ArrayList<>();
        
        String sql = "SELECT * FROM transactions WHERE user_id = ? AND transaction_type = ? ORDER BY transaction_date DESC";
        List<TransactionData.Transaction> transactions = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentUserId);
            pstmt.setString(2, type);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new TransactionData.Transaction(
                        rs.getString("transaction_type"),
                        rs.getString("transaction_date"),
                        rs.getString("category"),
                        rs.getDouble("amount"),
                        rs.getString("comment")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    public List<TransactionData.Transaction> getAllTransactions() {
        if (currentUserId == null) return new ArrayList<>();
        
        String sql = "SELECT * FROM transactions WHERE user_id = ? ORDER BY transaction_date DESC";
        List<TransactionData.Transaction> transactions = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentUserId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new TransactionData.Transaction(
                        rs.getString("transaction_type"),
                        rs.getString("transaction_date"),
                        rs.getString("category"),
                        rs.getDouble("amount"),
                        rs.getString("comment")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    // CRITICAL FIX: Replace the replaceAllTransactions method in DatabaseDAO.java
// The issue is that getCurrentBalance() opens a NEW connection which can't see
// uncommitted changes in the transaction. We need to calculate balance using
// the SAME connection.

public boolean replaceAllTransactions(List<TransactionData.Transaction> transactions) {
    if (currentUserId == null) return false;
    
    try (Connection conn = getConnection()) {
        conn.setAutoCommit(false);
        
        try (PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM transactions WHERE user_id = ?");
             PreparedStatement insertStmt = conn.prepareStatement(
                 "INSERT INTO transactions (user_id, transaction_type, transaction_date, category, amount, comment) VALUES (?, ?, ?, ?, ?, ?)")) {

            // Delete all existing transactions
            deleteStmt.setInt(1, currentUserId);
            deleteStmt.executeUpdate();

            // Insert all transactions from snapshot
            for (TransactionData.Transaction t : transactions) {
                insertStmt.setInt(1, currentUserId);
                insertStmt.setString(2, t.type);
                insertStmt.setString(3, t.date);
                insertStmt.setString(4, t.category);
                insertStmt.setDouble(5, t.amount);
                insertStmt.setString(6, t.comment);
                insertStmt.addBatch();
            }

            insertStmt.executeBatch();
            
            // CRITICAL FIX: Calculate balance using the SAME connection
            // so we can see the uncommitted changes
            double startingBalance = 0.0;
            String balanceSql = "SELECT starting_balance FROM user_balance WHERE user_id = ?";
            try (PreparedStatement balanceStmt = conn.prepareStatement(balanceSql)) {
                balanceStmt.setInt(1, currentUserId);
                try (ResultSet balanceRs = balanceStmt.executeQuery()) {
                    if (balanceRs.next()) {
                        startingBalance = balanceRs.getDouble("starting_balance");
                    }
                }
            }
            
            // Sum income using the same connection
            double totalIncome = 0.0;
            String incomeSql = "SELECT COALESCE(SUM(amount), 0) AS total_income FROM transactions WHERE user_id = ? AND transaction_type = 'Income'";
            try (PreparedStatement incomeStmt = conn.prepareStatement(incomeSql)) {
                incomeStmt.setInt(1, currentUserId);
                try (ResultSet incomeRs = incomeStmt.executeQuery()) {
                    if (incomeRs.next()) {
                        totalIncome = incomeRs.getDouble("total_income");
                    }
                }
            }
            
            // Sum expenses using the same connection
            double totalExpenses = 0.0;
            String expenseSql = "SELECT COALESCE(SUM(amount), 0) AS total_expenses FROM transactions WHERE user_id = ? AND transaction_type = 'Expenses'";
            try (PreparedStatement expenseStmt = conn.prepareStatement(expenseSql)) {
                expenseStmt.setInt(1, currentUserId);
                try (ResultSet expenseRs = expenseStmt.executeQuery()) {
                    if (expenseRs.next()) {
                        totalExpenses = expenseRs.getDouble("total_expenses");
                    }
                }
            }
            
            // Calculate current balance
            double computedBalance = startingBalance + totalIncome - totalExpenses;
            
            // Update current_balance using the same connection
            String updateBalanceSql = "UPDATE user_balance SET current_balance = ? WHERE user_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateBalanceSql)) {
                updateStmt.setDouble(1, computedBalance);
                updateStmt.setInt(2, currentUserId);
                updateStmt.executeUpdate();
            }
            
            System.out.println("✓ replaceAllTransactions(): Updated current_balance to ₱" + 
                String.format("%,.2f", computedBalance) + " within transaction");
            
            // Commit all changes together
            conn.commit();
            
            System.out.println("✓ replaceAllTransactions(): Transaction committed successfully");
            return true;
            
        } catch (SQLException e) {
            conn.rollback();
            System.err.println("✗ replaceAllTransactions(): Rolled back due to error");
            e.printStackTrace();
            return false;
        } finally {
            conn.setAutoCommit(true);
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

    // ==================== CATEGORY OPERATIONS ====================

    public Set<String> getCategories(String type) {
        if (currentUserId == null) return new HashSet<>();
        
        String sql = "SELECT category_name FROM categories WHERE user_id = ? AND category_type = ?";
        Set<String> categories = new HashSet<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentUserId);
            pstmt.setString(2, type);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(rs.getString("category_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    public boolean addCustomCategory(String type, String categoryName) {
        if (currentUserId == null) return false;
        
        String sql = "INSERT INTO categories (user_id, category_type, category_name, is_default) VALUES (?, ?, ?, FALSE)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentUserId);
            pstmt.setString(2, type);
            pstmt.setString(3, categoryName);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== BUDGET OPERATIONS ====================

    public boolean saveBudgetGoal(BudgetData.BudgetGoal goal) {
        if (currentUserId == null) return false;
        
        String sql = "INSERT INTO budgets (user_id, category, month, goal_amount) VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE goal_amount = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentUserId);
            pstmt.setString(2, goal.category);
            pstmt.setString(3, goal.month);
            pstmt.setDouble(4, goal.goal);
            pstmt.setDouble(5, goal.goal);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveBudgetGoals(List<BudgetData.BudgetGoal> goals) {
        if (currentUserId == null || goals.isEmpty()) return false;
        
        String sql = "INSERT INTO budgets (user_id, category, month, goal_amount) VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE goal_amount = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (BudgetData.BudgetGoal goal : goals) {
                pstmt.setInt(1, currentUserId);
                pstmt.setString(2, goal.category);
                pstmt.setString(3, goal.month);
                pstmt.setDouble(4, goal.goal);
                pstmt.setDouble(5, goal.goal);
                pstmt.addBatch();
            }

            pstmt.executeBatch();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<BudgetData.BudgetGoal> getAllBudgetGoals() {
        if (currentUserId == null) return new ArrayList<>();
        
        String sql = "SELECT * FROM budgets WHERE user_id = ?";
        List<BudgetData.BudgetGoal> goals = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentUserId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    goals.add(new BudgetData.BudgetGoal(
                        rs.getString("category"),
                        rs.getString("month"),
                        rs.getDouble("goal_amount")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return goals;
    }

    public Double getBudgetGoal(String category, String month) {
        if (currentUserId == null) return null;
        
        String sql = "SELECT goal_amount FROM budgets WHERE user_id = ? AND category = ? AND month = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentUserId);
            pstmt.setString(2, category);
            pstmt.setString(3, month);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("goal_amount");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ==================== BALANCE OPERATIONS ====================

    public Double getBalance() {
        if (currentUserId == null) return null;
        
        String sql = "SELECT starting_balance FROM user_balance WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentUserId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("starting_balance");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean setBalance(double balance) {
        if (currentUserId == null) return false;
        
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // STEP 1: Update the starting balance
                String updateSql = "INSERT INTO user_balance (user_id, starting_balance, current_balance) VALUES (?, ?, 0.00) " +
                                 "ON DUPLICATE KEY UPDATE starting_balance = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                    pstmt.setInt(1, currentUserId);
                    pstmt.setDouble(2, balance);
                    pstmt.setDouble(3, balance);
                    pstmt.executeUpdate();
                }
                
                // STEP 2: Recalculate and update current_balance
                updateCurrentBalanceInDB(conn);
                
                conn.commit();
                
                System.out.println("✓ setBalance(): Starting balance set to ₱" + String.format("%,.2f", balance));
                Double newCurrent = getCurrentBalance();
                System.out.println("✓ setBalance(): Current balance updated to ₱" + String.format("%,.2f", newCurrent));
                
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Computes and returns the logged-in user's real-time total balance.
     * 
     * <p>This method calculates the current balance by combining the user's starting balance
     * with all recorded income and expense transactions from the database:
     * <ol>
     *   <li>Retrieves the user's starting balance from the user_balance table</li>
     *   <li>Sums all Income transactions</li>
     *   <li>Sums all Expenses transactions</li>
     *   <li>Computes: current_balance = starting_balance + total_income - total_expenses</li>
     * </ol>
     * 
     * <p>This ensures the balance reflects real-time data from the database and stays
     * synchronized with the UI display.
     * 
     * @return The computed current balance as a Double, or null if no user is logged in
     *         or if any database query fails
     */
    public Double getCurrentBalance() {
        if (currentUserId == null) return null;
        
        Connection conn = null;
        PreparedStatement balanceStmt = null;
        PreparedStatement incomeStmt = null;
        PreparedStatement expenseStmt = null;
        ResultSet balanceRs = null;
        ResultSet incomeRs = null;
        ResultSet expenseRs = null;
        
        try {
            conn = getConnection();
            
            // Get starting balance
            String balanceSql = "SELECT starting_balance FROM user_balance WHERE user_id = ?";
            balanceStmt = conn.prepareStatement(balanceSql);
            balanceStmt.setInt(1, currentUserId);
            balanceRs = balanceStmt.executeQuery();
            
            double startingBalance = 0.0;
            if (balanceRs.next()) {
                startingBalance = balanceRs.getDouble("starting_balance");
            }
            
            // Sum all income transactions
            String incomeSql = "SELECT COALESCE(SUM(amount), 0) AS total_income FROM transactions WHERE user_id = ? AND transaction_type = 'Income'";
            incomeStmt = conn.prepareStatement(incomeSql);
            incomeStmt.setInt(1, currentUserId);
            incomeRs = incomeStmt.executeQuery();
            
            double totalIncome = 0.0;
            if (incomeRs.next()) {
                totalIncome = incomeRs.getDouble("total_income");
            }
            
            // Sum all expense transactions
            String expenseSql = "SELECT COALESCE(SUM(amount), 0) AS total_expenses FROM transactions WHERE user_id = ? AND transaction_type = 'Expenses'";
            expenseStmt = conn.prepareStatement(expenseSql);
            expenseStmt.setInt(1, currentUserId);
            expenseRs = expenseStmt.executeQuery();
            
            double totalExpenses = 0.0;
            if (expenseRs.next()) {
                totalExpenses = expenseRs.getDouble("total_expenses");
            }
            
            // Calculate and return current balance
            return startingBalance + totalIncome - totalExpenses;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            // Close all resources in reverse order of creation
            try {
                if (expenseRs != null) expenseRs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (incomeRs != null) incomeRs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (balanceRs != null) balanceRs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (expenseStmt != null) expenseStmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (incomeStmt != null) incomeStmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (balanceStmt != null) balanceStmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Updates the current_balance field in the user_balance table to reflect
     * the real-time computed balance.
     * 
     * <p>This method should be called after any transaction operation (create, update, delete)
     * to ensure the database stores the current balance for quick retrieval and transparency.
     * 
     * @return true if the update was successful, false otherwise
     */
    private boolean updateCurrentBalanceInDB() {
        try (Connection conn = getConnection()) {
            return updateCurrentBalanceInDB(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates the current_balance field using an existing connection.
     * Used within transactions to maintain consistency.
     * 
     * @param conn The database connection to use
     * @return true if the update was successful, false otherwise
     */
    private boolean updateCurrentBalanceInDB(Connection conn) {
        if (currentUserId == null) {
            System.err.println("✗ updateCurrentBalanceInDB(): No user logged in");
            return false;
        }
        
        Double computedBalance = getCurrentBalance();
        if (computedBalance == null) {
            System.err.println("✗ updateCurrentBalanceInDB(): Could not compute balance");
            return false;
        }
        
        String sql = "UPDATE user_balance SET current_balance = ? WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, computedBalance);
            pstmt.setInt(2, currentUserId);
            
            int rowsUpdated = pstmt.executeUpdate();
            
            if (rowsUpdated > 0) {
                System.out.println("✓ updateCurrentBalanceInDB(): Updated current_balance to ₱" + 
                    String.format("%,.2f", computedBalance) + " for user " + currentUserId);
                return true;
            } else {
                System.err.println("✗ updateCurrentBalanceInDB(): No rows updated for user " + currentUserId);
                return false;
            }
        } catch (SQLException e) {
            System.err.println("✗ updateCurrentBalanceInDB(): SQL Exception");
            e.printStackTrace();
            return false;
        }
    }

    // ==================== DATA MANAGEMENT ====================

    public boolean resetUserData() {
        if (currentUserId == null) return false;
        
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement deleteTransactions = conn.prepareStatement("DELETE FROM transactions WHERE user_id = ?");
                 PreparedStatement deleteBudgets = conn.prepareStatement("DELETE FROM budgets WHERE user_id = ?");
                 PreparedStatement updateBalance = conn.prepareStatement("UPDATE user_balance SET starting_balance = 0.00, current_balance = 0.00 WHERE user_id = ?")) {

                deleteTransactions.setInt(1, currentUserId);
                deleteTransactions.executeUpdate();

                deleteBudgets.setInt(1, currentUserId);
                deleteBudgets.executeUpdate();

                updateBalance.setInt(1, currentUserId);
                updateBalance.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
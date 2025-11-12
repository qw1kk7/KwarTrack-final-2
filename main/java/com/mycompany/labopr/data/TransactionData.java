package com.mycompany.labopr.data;

import com.mycompany.labopr.database.DatabaseDAO;
import java.util.*;

public class TransactionData {
    
    public static Double getBalance() {
        return DatabaseDAO.getInstance().getBalance();
    }
    
    public static void setBalance(double balance) {
        DatabaseDAO.getInstance().setBalance(balance);
    }
    
    /**
     * Calculate current balance from database in real-time.
     * This method now delegates to DatabaseDAO.getCurrentBalance() which
     * computes the balance directly from the database.
     * 
     * @return The current balance including starting balance and all transactions
     */
    public static double calculateCurrentBalance() {
        Double currentBalance = DatabaseDAO.getInstance().getCurrentBalance();
        return currentBalance != null ? currentBalance : 0.0;
    }
    
    public static void saveTransaction(Transaction transaction) {
        DatabaseDAO.getInstance().createTransaction(transaction);
    }
    
    public static boolean updateTransaction(String originalDate, String originalCategory, 
                                           double originalAmount, Transaction updatedTransaction) {
        return DatabaseDAO.getInstance().updateTransaction(
            originalDate, originalCategory, originalAmount, updatedTransaction
        );
    }
    
    public static boolean deleteTransaction(String date, String category, double amount) {
        return DatabaseDAO.getInstance().deleteTransaction(date, category, amount);
    }
    
    public static List<Transaction> getAllTransactions() {
        return DatabaseDAO.getInstance().getAllTransactions();
    }
    
    public static List<Transaction> getTransactionsByType(String type) {
        return DatabaseDAO.getInstance().getTransactionsByType(type);
    }
    
    public static void replaceAllTransactions(List<Transaction> transactions) {
        DatabaseDAO.getInstance().replaceAllTransactions(transactions);
    }
    
    public static Set<String> getCategories(String type) {
        return DatabaseDAO.getInstance().getCategories(type);
    }
    
    public static void addCustomCategory(String type, String category) {
        DatabaseDAO.getInstance().addCustomCategory(type, category);
    }
    
    public static class Transaction {
        public String type;
        public String date;
        public String category;
        public double amount;
        public String comment;
        
        public Transaction(String type, String date, String category, double amount, String comment) {
            this.type = type;
            this.date = date;
            this.category = category;
            this.amount = amount;
            this.comment = comment;
        }
        
        // Keep these methods for compatibility with Memento pattern
        public String toFileString() {
            return type + "|" + date + "|" + category + "|" + amount + "|" + comment;
        }
        
        public static Transaction fromFileString(String line) {
            String[] parts = line.split("\\|", 5);
            if (parts.length == 5) {
                try {
                    return new Transaction(
                        parts[0],
                        parts[1],
                        parts[2],
                        Double.parseDouble(parts[3]),
                        parts[4]
                    );
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        }
        
        /**
         * Create a deep copy of this transaction
         */
        public Transaction copy() {
            return new Transaction(type, date, category, amount, comment);
        }
    }
}
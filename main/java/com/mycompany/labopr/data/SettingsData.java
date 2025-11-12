package com.mycompany.labopr.data;

import com.mycompany.labopr.database.DatabaseDAO;
import java.io.*;
import java.util.*;

/**
 * Simplified SettingsData - REMOVED currency and date format settings
 * Only handles display name and theme mode
 */
public class SettingsData {
    
    private static final DatabaseDAO dao = DatabaseDAO.getInstance();
    
    public static boolean resetAllData() {
        return dao.resetUserData();
    }
    
    /**
     * Export all user data to CSV file
     * Includes balance, transactions, budgets, and settings
     */
    public static boolean exportData(String filepath) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filepath))) {
            // Export starting balance
            Double balance = TransactionData.getBalance();
            if (balance != null) {
                bw.write("STARTING_BALANCE," + balance);
                bw.newLine();
            }
            
            // Export current balance
            double currentBalance = TransactionData.calculateCurrentBalance();
            bw.write("CURRENT_BALANCE," + currentBalance);
            bw.newLine();
            
            // Export transactions
            bw.newLine();
            bw.write("TRANSACTIONS");
            bw.newLine();
            bw.write("Type,Date,Category,Amount,Comment");
            bw.newLine();
            
            List<TransactionData.Transaction> transactions = TransactionData.getAllTransactions();
            for (TransactionData.Transaction t : transactions) {
                // Escape quotes in comments
                String escapedComment = t.comment.replace("\"", "\"\"");
                bw.write(String.format("%s,%s,%s,%.2f,\"%s\"", 
                    t.type, t.date, t.category, t.amount, escapedComment));
                bw.newLine();
            }
            
            // Export budgets
            bw.newLine();
            bw.write("BUDGETS");
            bw.newLine();
            bw.write("Category,Month,Goal");
            bw.newLine();
            
            List<BudgetData.BudgetGoal> budgets = BudgetData.getAllBudgetGoals();
            for (BudgetData.BudgetGoal b : budgets) {
                bw.write(String.format("%s,%s,%.2f", b.category, b.month, b.goal));
                bw.newLine();
            }
            
            // Export settings (simplified - no currency/date format)
            bw.newLine();
            bw.write("SETTINGS");
            bw.newLine();
            bw.write("Key,Value");
            bw.newLine();
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Import data from CSV file
     * Adds to existing data (does not replace)
     */
    public static boolean importData(String filepath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            String section = "";
            
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                if (line.startsWith("STARTING_BALANCE,")) {
                    double balance = Double.parseDouble(line.split(",")[1]);
                    TransactionData.setBalance(balance);
                } else if (line.startsWith("CURRENT_BALANCE,")) {
                    // Skip current balance as it will be recalculated
                    continue;
                } else if (line.equals("TRANSACTIONS")) {
                    section = "TRANSACTIONS";
                    br.readLine(); // Skip header
                } else if (line.equals("BUDGETS")) {
                    section = "BUDGETS";
                    br.readLine(); // Skip header
                } else if (line.equals("SETTINGS")) {
                    section = "SETTINGS";
                    br.readLine(); // Skip header
                } else if (section.equals("TRANSACTIONS")) {
                    // Parse CSV with quoted fields support
                    String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", 5);
                    if (parts.length == 5) {
                        String comment = parts[4].replaceAll("^\"|\"$", "").replace("\"\"", "\"");
                        TransactionData.Transaction t = new TransactionData.Transaction(
                            parts[0], parts[1], parts[2], Double.parseDouble(parts[3]), comment
                        );
                        TransactionData.saveTransaction(t);
                    }
                } else if (section.equals("BUDGETS")) {
                    String[] parts = line.split(",");
                    if (parts.length == 3) {
                        BudgetData.BudgetGoal b = new BudgetData.BudgetGoal(
                            parts[0], parts[1], Double.parseDouble(parts[2])
                        );
                        BudgetData.saveBudgetGoal(b);
                    }
                } 
            }
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
package com.mycompany.labopr.data;

import com.mycompany.labopr.data.TransactionData.Transaction;
import com.mycompany.labopr.data.BudgetData.BudgetGoal;
import com.mycompany.labopr.data.BudgetData.BudgetStatusInfo;
import com.mycompany.labopr.data.AnalyticsData.MonthlyData;
import com.mycompany.labopr.data.AnalyticsData.CategorySpending;
import java.util.*;

/**
 * Facade Pattern: Single entry point for all data operations
 * Centralizes and simplifies access to TransactionData, BudgetData, SettingsData, and AnalyticsData
 * 
 * Benefits:
 * - Simplifies client code (panels only need DataFacade)
 * - Encapsulates complexity of data layer
 * - Provides unified API for data operations
 * - Easy to add caching, logging, or validation in future
 */
public class DataFacade {
    
    private static DataFacade instance;
    
    private DataFacade() {
        // Private constructor for singleton
    }
    
    /**
     * Get singleton instance of DataFacade
     */
    public static synchronized DataFacade getInstance() {
        if (instance == null) {
            instance = new DataFacade();
        }
        return instance;
    }
    
    // ==================== TRANSACTION OPERATIONS ====================
    
    /**
     * Get all transactions for the current user
     */
    public List<Transaction> getAllTransactions() {
        return TransactionData.getAllTransactions();
    }
    
    /**
     * Get transactions filtered by type (Income/Expenses)
     */
    public List<Transaction> getTransactionsByType(String type) {
        return TransactionData.getTransactionsByType(type);
    }
    
    /**
     * Save a new transaction
     */
    public void saveTransaction(Transaction transaction) {
        TransactionData.saveTransaction(transaction);
    }
    
    /**
     * Update an existing transaction
     */
    public boolean updateTransaction(String originalDate, String originalCategory, 
                                     double originalAmount, Transaction updatedTransaction) {
        return TransactionData.updateTransaction(originalDate, originalCategory, 
                                                originalAmount, updatedTransaction);
    }
    
    /**
     * Delete a transaction
     */
    public boolean deleteTransaction(String date, String category, double amount) {
        return TransactionData.deleteTransaction(date, category, amount);
    }
    
    /**
     * Get starting balance
     */
    public Double getBalance() {
        return TransactionData.getBalance();
    }
    
    /**
     * Set starting balance
     */
    public void setBalance(double balance) {
        TransactionData.setBalance(balance);
    }
    
    /**
     * Calculate current balance (starting balance + income - expenses)
     */
    public double calculateCurrentBalance() {
        return TransactionData.calculateCurrentBalance();
    }
    
    /**
     * Get available categories for a transaction type
     */
    public Set<String> getCategories(String type) {
        return TransactionData.getCategories(type);
    }
    
    /**
     * Add a custom category
     */
    public void addCustomCategory(String type, String category) {
        TransactionData.addCustomCategory(type, category);
    }
    
    /**
     * Replace all transactions (used by Memento pattern)
     */
    public void replaceAllTransactions(List<Transaction> transactions) {
        TransactionData.replaceAllTransactions(transactions);
    }
    
    // ==================== BUDGET OPERATIONS ====================
    
    /**
     * Get all budget goals for the current user
     */
    public List<BudgetGoal> getAllBudgetGoals() {
        return BudgetData.getAllBudgetGoals();
    }
    
    /**
     * Get budget goals for a specific month
     */
    public Map<String, Double> getBudgetGoalsForMonth(String month) {
        return BudgetData.getBudgetGoalsForMonth(month);
    }
    
    /**
     * Get budget goal for a specific category and month
     */
    public Double getBudgetGoal(String category, String month) {
        return BudgetData.getBudgetGoal(category, month);
    }
    
    /**
     * Save a single budget goal
     */
    public boolean saveBudgetGoal(BudgetGoal goal) {
        return BudgetData.saveBudgetGoal(goal);
    }
    
    /**
     * Save multiple budget goals at once
     */
    public boolean saveBudgetGoals(List<BudgetGoal> goals) {
        return BudgetData.saveBudgetGoals(goals);
    }
    
    /**
     * Get total spent for a category in a month
     */
    public double getSpentForCategoryAndMonth(String category, String month) {
        return BudgetData.getSpentForCategoryAndMonth(category, month);
    }
    
    /**
     * Get all relevant categories (with goals or transactions) for a month
     */
    public Set<String> getAllRelevantCategories(String month) {
        return BudgetData.getAllRelevantCategories(month);
    }
    
    /**
     * Get budget status summary for a month
     */
    public Map<String, BudgetStatusInfo> getBudgetStatusForMonth(String month) {
        return BudgetData.getBudgetStatusForMonth(month);
    }
    
    /**
     * Get total budgeted amount for a month
     */
    public double getTotalBudgetForMonth(String month) {
        return BudgetData.getTotalBudgetForMonth(month);
    }
    
    /**
     * Get total spent for a month
     */
    public double getTotalSpentForMonth(String month) {
        return BudgetData.getTotalSpentForMonth(month);
    }
    
    /**
     * Check if user is over budget for the month
     */
    public boolean isOverBudgetForMonth(String month) {
        return BudgetData.isOverBudgetForMonth(month);
    }
    
    // ==================== ANALYTICS OPERATIONS ====================
    
    /**
     * Get total income for a specific month
     */
    public double getTotalIncome(String month) {
        return AnalyticsData.getTotalIncome(month);
    }
    
    /**
     * Get total expenses for a specific month
     */
    public double getTotalExpenses(String month) {
        return AnalyticsData.getTotalExpenses(month);
    }
    
    /**
     * Get net savings for a month
     */
    public double getNetSavings(String month) {
        return AnalyticsData.getNetSavings(month);
    }
    
    /**
     * Get savings rate as percentage
     */
    public double getSavingsRate(String month) {
        return AnalyticsData.getSavingsRate(month);
    }
    
    /**
     * Get spending breakdown by category for a month
     */
    public Map<String, Double> getSpendingByCategory(String month) {
        return AnalyticsData.getSpendingByCategory(month);
    }
    
    /**
     * Get income breakdown by category for a month
     */
    public Map<String, Double> getIncomeByCategory(String month) {
        return AnalyticsData.getIncomeByCategory(month);
    }
    
    /**
     * Get top N expense categories for a month
     */
    public List<CategorySpending> getTopExpenseCategories(String month, int topN) {
        return AnalyticsData.getTopExpenseCategories(month, topN);
    }
    
    /**
     * Get income vs expenses trend for multiple months
     */
    public Map<String, MonthlyData> getIncomeExpensesTrend(List<String> months) {
        return AnalyticsData.getIncomeExpensesTrend(months);
    }
    
    /**
     * Get average monthly spending over a period
     */
    public double getAverageMonthlySpending(List<String> months) {
        return AnalyticsData.getAverageMonthlySpending(months);
    }
    
    /**
     * Get average monthly income over a period
     */
    public double getAverageMonthlyIncome(List<String> months) {
        return AnalyticsData.getAverageMonthlyIncome(months);
    }
    
    /**
     * Get all months that have transactions
     */
    public List<String> getMonthsWithTransactions() {
        return AnalyticsData.getMonthsWithTransactions();
    }
    
    /**
     * Get transaction count for a month
     */
    public int getTransactionCount(String month) {
        return AnalyticsData.getTransactionCount(month);
    }
    
    /**
     * Get highest spending category for a month
     */
    public CategorySpending getHighestSpendingCategory(String month) {
        return AnalyticsData.getHighestSpendingCategory(month);
    }
    
    /**
     * Get budget adherence percentage
     */
    public double getBudgetAdherenceRate(String month) {
        return AnalyticsData.getBudgetAdherenceRate(month);
    }
    
    // ==================== SETTINGS OPERATIONS ====================
    
    /**
     * Reset all data (transactions, budgets, balance)
     */
    public boolean resetAllData() {
        return SettingsData.resetAllData();
    }
    
    /**
     * Export all user data to CSV file
     */
    public boolean exportData(String filepath) {
        return SettingsData.exportData(filepath);
    }
    
    /**
     * Import data from CSV file
     */
    public boolean importData(String filepath) {
        return SettingsData.importData(filepath);
    }
    
    // ==================== CONVENIENCE METHODS ====================
    
    /**
     * Get complete analytics summary for a month
     */
    public AnalyticsSummary getAnalyticsSummary(String month) {
        return new AnalyticsSummary(
            getTotalIncome(month),
            getTotalExpenses(month),
            getNetSavings(month),
            getSavingsRate(month),
            getSpendingByCategory(month),
            getTopExpenseCategories(month, 3),
            getBudgetAdherenceRate(month)
        );
    }
    
    /**
     * Get dashboard data (recent transactions, current balance, quick stats)
     */
    public DashboardData getDashboardData(String currentMonth) {
        List<Transaction> recentTransactions = getAllTransactions();
        if (recentTransactions.size() > 10) {
            recentTransactions = recentTransactions.subList(0, 10);
        }
        
        return new DashboardData(
            calculateCurrentBalance(),
            getTotalIncome(currentMonth),
            getTotalExpenses(currentMonth),
            getNetSavings(currentMonth),
            recentTransactions,
            isOverBudgetForMonth(currentMonth)
        );
    }
    
    // ==================== INNER DATA CLASSES ====================
    
    /**
     * Analytics summary data holder
     */
    public static class AnalyticsSummary {
        public final double totalIncome;
        public final double totalExpenses;
        public final double netSavings;
        public final double savingsRate;
        public final Map<String, Double> spendingByCategory;
        public final List<CategorySpending> topCategories;
        public final double budgetAdherence;
        
        public AnalyticsSummary(double totalIncome, double totalExpenses, 
                               double netSavings, double savingsRate,
                               Map<String, Double> spendingByCategory,
                               List<CategorySpending> topCategories,
                               double budgetAdherence) {
            this.totalIncome = totalIncome;
            this.totalExpenses = totalExpenses;
            this.netSavings = netSavings;
            this.savingsRate = savingsRate;
            this.spendingByCategory = spendingByCategory;
            this.topCategories = topCategories;
            this.budgetAdherence = budgetAdherence;
        }
    }
    
    /**
     * Dashboard data holder
     */
    public static class DashboardData {
        public final double currentBalance;
        public final double monthlyIncome;
        public final double monthlyExpenses;
        public final double monthlySavings;
        public final List<Transaction> recentTransactions;
        public final boolean isOverBudget;
        
        public DashboardData(double currentBalance, double monthlyIncome,
                           double monthlyExpenses, double monthlySavings,
                           List<Transaction> recentTransactions,
                           boolean isOverBudget) {
            this.currentBalance = currentBalance;
            this.monthlyIncome = monthlyIncome;
            this.monthlyExpenses = monthlyExpenses;
            this.monthlySavings = monthlySavings;
            this.recentTransactions = recentTransactions;
            this.isOverBudget = isOverBudget;
        }
    }
}
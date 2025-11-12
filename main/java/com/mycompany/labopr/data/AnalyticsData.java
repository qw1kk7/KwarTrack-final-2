package com.mycompany.labopr.data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Fully Refactored AnalyticsData - Complete database integration
 * All analytics calculations now use real-time database queries through TransactionData
 * Provides comprehensive financial analytics and reporting
 */
public class AnalyticsData {
    
    /**
     * Get total income for a specific month
     * Queries all income transactions and sums amounts for matching month
     */
    public static double getTotalIncome(String month) {
        if (month == null || month.isEmpty()) {
            return 0.0;
        }
        
        List<TransactionData.Transaction> incomeTransactions = 
            TransactionData.getTransactionsByType("Income");
        
        double total = 0.0;
        for (TransactionData.Transaction t : incomeTransactions) {
            String transactionMonth = extractMonth(t.date);
            if (transactionMonth.equals(month)) {
                total += t.amount;
            }
        }
        return total;
    }
    
    /**
     * Get total expenses for a specific month
     * Queries all expense transactions and sums amounts for matching month
     */
    public static double getTotalExpenses(String month) {
        if (month == null || month.isEmpty()) {
            return 0.0;
        }
        
        List<TransactionData.Transaction> expenseTransactions = 
            TransactionData.getTransactionsByType("Expenses");
        
        double total = 0.0;
        for (TransactionData.Transaction t : expenseTransactions) {
            String transactionMonth = extractMonth(t.date);
            if (transactionMonth.equals(month)) {
                total += t.amount;
            }
        }
        return total;
    }
    
    /**
     * Calculate net savings for a month (income - expenses)
     */
    public static double getNetSavings(String month) {
        return getTotalIncome(month) - getTotalExpenses(month);
    }
    
    /**
     * Calculate savings rate as percentage of income
     * Returns 0 if no income
     */
    public static double getSavingsRate(String month) {
        double income = getTotalIncome(month);
        if (income == 0) return 0.0;
        
        double savings = getNetSavings(month);
        return (savings / income) * 100;
    }
    
    /**
     * Get spending breakdown by category for a month
     * Returns map of category -> total amount spent
     */
    public static Map<String, Double> getSpendingByCategory(String month) {
        if (month == null || month.isEmpty()) {
            return new HashMap<>();
        }
        
        List<TransactionData.Transaction> expenseTransactions = 
            TransactionData.getTransactionsByType("Expenses");
        
        Map<String, Double> categorySpending = new HashMap<>();
        
        for (TransactionData.Transaction t : expenseTransactions) {
            String transactionMonth = extractMonth(t.date);
            if (transactionMonth.equals(month)) {
                categorySpending.put(
                    t.category, 
                    categorySpending.getOrDefault(t.category, 0.0) + t.amount
                );
            }
        }
        
        return categorySpending;
    }
    
    /**
     * Get income breakdown by category for a month
     * Returns map of category -> total amount received
     */
    public static Map<String, Double> getIncomeByCategory(String month) {
        if (month == null || month.isEmpty()) {
            return new HashMap<>();
        }
        
        List<TransactionData.Transaction> incomeTransactions = 
            TransactionData.getTransactionsByType("Income");
        
        Map<String, Double> categoryIncome = new HashMap<>();
        
        for (TransactionData.Transaction t : incomeTransactions) {
            String transactionMonth = extractMonth(t.date);
            if (transactionMonth.equals(month)) {
                categoryIncome.put(
                    t.category, 
                    categoryIncome.getOrDefault(t.category, 0.0) + t.amount
                );
            }
        }
        
        return categoryIncome;
    }
    
    /**
     * Get top N expense categories for a month (sorted by amount)
     */
    public static List<CategorySpending> getTopExpenseCategories(String month, int topN) {
        Map<String, Double> spending = getSpendingByCategory(month);
        
        List<CategorySpending> categories = new ArrayList<>();
        for (Map.Entry<String, Double> entry : spending.entrySet()) {
            categories.add(new CategorySpending(entry.getKey(), entry.getValue()));
        }
        
        // Sort by amount descending
        categories.sort((a, b) -> Double.compare(b.amount, a.amount));
        
        // Return top N (or all if fewer than N)
        return categories.subList(0, Math.min(topN, categories.size()));
    }
    
    /**
     * Get income vs expenses trend for multiple months
     * Returns chronologically ordered data
     */
    public static Map<String, MonthlyData> getIncomeExpensesTrend(List<String> months) {
        if (months == null || months.isEmpty()) {
            return new LinkedHashMap<>();
        }
        
        Map<String, MonthlyData> trend = new LinkedHashMap<>();
        
        for (String month : months) {
            double income = getTotalIncome(month);
            double expenses = getTotalExpenses(month);
            trend.put(month, new MonthlyData(income, expenses));
        }
        
        return trend;
    }
    
    /**
     * Get average monthly spending over a period
     */
    public static double getAverageMonthlySpending(List<String> months) {
        if (months == null || months.isEmpty()) {
            return 0.0;
        }
        
        double totalExpenses = 0.0;
        for (String month : months) {
            totalExpenses += getTotalExpenses(month);
        }
        
        return totalExpenses / months.size();
    }
    
    /**
     * Get average monthly income over a period
     */
    public static double getAverageMonthlyIncome(List<String> months) {
        if (months == null || months.isEmpty()) {
            return 0.0;
        }
        
        double totalIncome = 0.0;
        for (String month : months) {
            totalIncome += getTotalIncome(month);
        }
        
        return totalIncome / months.size();
    }
    
    /**
     * Get spending trend (increasing, decreasing, stable)
     * Compares current month to previous month
     */
    public static SpendingTrend getSpendingTrend(String currentMonth, String previousMonth) {
        double currentSpending = getTotalExpenses(currentMonth);
        double previousSpending = getTotalExpenses(previousMonth);
        
        if (previousSpending == 0) {
            return SpendingTrend.STABLE;
        }
        
        double percentageChange = ((currentSpending - previousSpending) / previousSpending) * 100;
        
        if (percentageChange > 10) {
            return SpendingTrend.INCREASING;
        } else if (percentageChange < -10) {
            return SpendingTrend.DECREASING;
        } else {
            return SpendingTrend.STABLE;
        }
    }
    
    /**
     * Get all months that have transactions
     * Returns sorted list of months in YYYY-MM format
     */
    public static List<String> getMonthsWithTransactions() {
        List<TransactionData.Transaction> allTransactions = 
            TransactionData.getAllTransactions();
        
        Set<String> months = new HashSet<>();
        for (TransactionData.Transaction t : allTransactions) {
            String month = extractMonth(t.date);
            if (!month.isEmpty()) {
                months.add(month);
            }
        }
        
        List<String> sortedMonths = new ArrayList<>(months);
        Collections.sort(sortedMonths);
        return sortedMonths;
    }
    
    /**
     * Get transaction count for a month
     */
    public static int getTransactionCount(String month) {
        if (month == null || month.isEmpty()) {
            return 0;
        }
        
        List<TransactionData.Transaction> allTransactions = 
            TransactionData.getAllTransactions();
        
        int count = 0;
        for (TransactionData.Transaction t : allTransactions) {
            String transactionMonth = extractMonth(t.date);
            if (transactionMonth.equals(month)) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Get highest spending category for a month
     */
    public static CategorySpending getHighestSpendingCategory(String month) {
        List<CategorySpending> topCategories = getTopExpenseCategories(month, 1);
        return topCategories.isEmpty() ? null : topCategories.get(0);
    }
    
    /**
     * Get budget adherence percentage for a month
     * Returns percentage of budget goals met (not exceeded)
     */
    public static double getBudgetAdherenceRate(String month) {
        Map<String, Double> budgetGoals = BudgetData.getBudgetGoalsForMonth(month);
        if (budgetGoals.isEmpty()) {
            return 100.0; // No budgets = perfect adherence
        }
        
        int totalGoals = budgetGoals.size();
        int goalsAdhered = 0;
        
        for (Map.Entry<String, Double> entry : budgetGoals.entrySet()) {
            double spent = BudgetData.getSpentForCategoryAndMonth(entry.getKey(), month);
            if (spent <= entry.getValue()) {
                goalsAdhered++;
            }
        }
        
        return (goalsAdhered / (double) totalGoals) * 100;
    }
    
    /**
     * Extract YYYY-MM from date string
     * Handles various date formats
     */
    private static String extractMonth(String date) {
        if (date == null || date.length() < 7) {
            return "";
        }
        return date.substring(0, 7);
    }
    
    // ==================== HELPER CLASSES ====================
    
    /**
     * Category spending data holder
     */
    public static class CategorySpending {
        public final String category;
        public final double amount;
        
        public CategorySpending(String category, double amount) {
            this.category = category;
            this.amount = amount;
        }
        
        @Override
        public String toString() {
            return String.format("CategorySpending[category=%s, amount=%.2f]", 
                category, amount);
        }
    }
    
    /**
     * Monthly income and expenses data holder
     */
    public static class MonthlyData {
        public final double income;
        public final double expenses;
        public final double netSavings;
        public final double savingsRate;
        
        public MonthlyData(double income, double expenses) {
            this.income = income;
            this.expenses = expenses;
            this.netSavings = income - expenses;
            this.savingsRate = (income > 0) ? (netSavings / income) * 100 : 0;
        }
        
        @Override
        public String toString() {
            return String.format("MonthlyData[income=%.2f, expenses=%.2f, savings=%.2f]", 
                income, expenses, netSavings);
        }
    }
    
    /**
     * Spending trend enum
     */
    public enum SpendingTrend {
        INCREASING,
        DECREASING,
        STABLE
    }
}
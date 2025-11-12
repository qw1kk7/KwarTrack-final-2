package com.mycompany.labopr.data;

import com.mycompany.labopr.database.DatabaseDAO;
import java.util.*;

/**
 * Fully Refactored BudgetData - Complete database integration
 * All budget operations now use DatabaseDAO with SQL queries
 */
public class BudgetData {
    
    private static final DatabaseDAO dao = DatabaseDAO.getInstance();
    
    // Budget goal class - immutable data structure
    public static class BudgetGoal {
        public final String category;
        public final String month; // Format: YYYY-MM
        public final double goal;
        
        public BudgetGoal(String category, String month, double goal) {
            this.category = category;
            this.month = month;
            this.goal = goal;
        }
        
        // Compatibility method for Memento pattern
        public String toFileString() {
            return category + "|" + month + "|" + goal;
        }
        
        public static BudgetGoal fromFileString(String line) {
            String[] parts = line.split("\\|", 3);
            if (parts.length == 3) {
                try {
                    return new BudgetGoal(parts[0], parts[1], Double.parseDouble(parts[2]));
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        }
        
        @Override
        public String toString() {
            return String.format("BudgetGoal[category=%s, month=%s, goal=%.2f]", 
                category, month, goal);
        }
    }
    
    /**
     * Save a single budget goal to database
     * Uses UPSERT logic (insert or update if exists)
     */
    public static boolean saveBudgetGoal(BudgetGoal goal) {
        if (goal == null) {
            throw new IllegalArgumentException("BudgetGoal cannot be null");
        }
        return dao.saveBudgetGoal(goal);
    }
    
    /**
     * Save multiple budget goals at once (batch operation)
     * More efficient than saving individually
     */
    public static boolean saveBudgetGoals(List<BudgetGoal> newGoals) {
        if (newGoals == null || newGoals.isEmpty()) {
            return false;
        }
        return dao.saveBudgetGoals(newGoals);
    }
    
    /**
     * Get all budget goals for the current user from database
     */
    public static List<BudgetGoal> getAllBudgetGoals() {
        return dao.getAllBudgetGoals();
    }
    
    /**
     * Get budget goals for a specific month
     * Returns map of category -> goal amount
     */
    public static Map<String, Double> getBudgetGoalsForMonth(String month) {
        if (month == null || month.isEmpty()) {
            return new HashMap<>();
        }
        
        Map<String, Double> goals = new HashMap<>();
        List<BudgetGoal> allGoals = dao.getAllBudgetGoals();
        
        for (BudgetGoal goal : allGoals) {
            if (goal.month.equals(month)) {
                goals.put(goal.category, goal.goal);
            }
        }
        
        return goals;
    }
    
    /**
     * Get budget goal for a specific category and month
     * Returns null if no goal exists
     */
    public static Double getBudgetGoal(String category, String month) {
        if (category == null || month == null) {
            return null;
        }
        return dao.getBudgetGoal(category, month);
    }
    
    /**
     * Calculate total spent for a category in a specific month
     * Queries transactions table directly
     */
    public static double getSpentForCategoryAndMonth(String category, String month) {
        if (category == null || month == null) {
            return 0.0;
        }
        
        List<TransactionData.Transaction> transactions = 
            TransactionData.getTransactionsByType("Expenses");
        double spent = 0.0;
        
        for (TransactionData.Transaction t : transactions) {
            // Extract YYYY-MM from transaction date
            String transactionMonth = t.date.length() >= 7 ? t.date.substring(0, 7) : "";
            
            if (t.category.equals(category) && transactionMonth.equals(month)) {
                spent += t.amount;
            }
        }
        
        return spent;
    }
    
    /**
     * Get all expense categories that have either goals or transactions for a month
     * Combines data from budgets table and transactions table
     */
    public static Set<String> getAllRelevantCategories(String month) {
        if (month == null || month.isEmpty()) {
            return new HashSet<>();
        }
        
        Set<String> categories = new HashSet<>();
        
        // Add categories with budget goals
        List<BudgetGoal> goals = dao.getAllBudgetGoals();
        for (BudgetGoal goal : goals) {
            if (goal.month.equals(month)) {
                categories.add(goal.category);
            }
        }
        
        // Add categories with transactions in this month
        List<TransactionData.Transaction> transactions = 
            TransactionData.getTransactionsByType("Expenses");
        for (TransactionData.Transaction t : transactions) {
            String transactionMonth = t.date.length() >= 7 ? t.date.substring(0, 7) : "";
            if (transactionMonth.equals(month)) {
                categories.add(t.category);
            }
        }
        
        return categories;
    }
    
    /**
     * Get budget status summary for a specific month
     * Returns map of category -> status information
     */
    public static Map<String, BudgetStatusInfo> getBudgetStatusForMonth(String month) {
        if (month == null || month.isEmpty()) {
            return new HashMap<>();
        }
        
        Map<String, BudgetStatusInfo> statusMap = new HashMap<>();
        Set<String> categories = getAllRelevantCategories(month);
        
        for (String category : categories) {
            Double goal = getBudgetGoal(category, month);
            double spent = getSpentForCategoryAndMonth(category, month);
            double goalAmount = (goal != null) ? goal : 0.0;
            
            BudgetStatus status = calculateStatus(goalAmount, spent);
            statusMap.put(category, new BudgetStatusInfo(category, goalAmount, spent, status));
        }
        
        return statusMap;
    }
    
    // Budget status enum
    public enum BudgetStatus {
        UNDER_BUDGET,
        NEARING_LIMIT,
        OVERSPENT,
        NO_GOAL
    }
    
    /**
     * Budget status information holder
     */
    public static class BudgetStatusInfo {
        public final String category;
        public final double goal;
        public final double spent;
        public final double remaining;
        public final BudgetStatus status;
        public final double percentage;
        
        public BudgetStatusInfo(String category, double goal, double spent, BudgetStatus status) {
            this.category = category;
            this.goal = goal;
            this.spent = spent;
            this.remaining = goal - spent;
            this.status = status;
            this.percentage = (goal > 0) ? (spent / goal) * 100 : 0;
        }
        
        @Override
        public String toString() {
            return String.format("BudgetStatus[category=%s, goal=%.2f, spent=%.2f, status=%s]", 
                category, goal, spent, status);
        }
    }
    
    /**
     * Calculate budget status based on goal and spent amount
     * Determines if under budget, nearing limit, or overspent
     */
    public static BudgetStatus calculateStatus(double goal, double spent) {
        if (goal == 0) {
            return BudgetStatus.NO_GOAL;
        }
        
        double percentage = (spent / goal) * 100;
        
        if (percentage >= 100) {
            return BudgetStatus.OVERSPENT;
        } else if (percentage >= 80) {
            return BudgetStatus.NEARING_LIMIT;
        } else {
            return BudgetStatus.UNDER_BUDGET;
        }
    }
    
    /**
     * Get total budgeted amount for a month across all categories
     */
    public static double getTotalBudgetForMonth(String month) {
        if (month == null || month.isEmpty()) {
            return 0.0;
        }
        
        Map<String, Double> goals = getBudgetGoalsForMonth(month);
        return goals.values().stream().mapToDouble(Double::doubleValue).sum();
    }
    
    /**
     * Get total spent for a month across all expense categories
     */
    public static double getTotalSpentForMonth(String month) {
        if (month == null || month.isEmpty()) {
            return 0.0;
        }
        
        Set<String> categories = getAllRelevantCategories(month);
        double total = 0.0;
        
        for (String category : categories) {
            total += getSpentForCategoryAndMonth(category, month);
        }
        
        return total;
    }
    
    /**
     * Check if user is over budget for the month
     */
    public static boolean isOverBudgetForMonth(String month) {
        double totalBudget = getTotalBudgetForMonth(month);
        double totalSpent = getTotalSpentForMonth(month);
        
        return totalBudget > 0 && totalSpent > totalBudget;
    }
}
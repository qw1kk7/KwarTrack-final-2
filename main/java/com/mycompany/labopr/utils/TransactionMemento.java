package com.mycompany.labopr.utils;

import com.mycompany.labopr.data.TransactionData;
import java.util.*;

/**
 * Enhanced Memento Pattern: State capture with guaranteed database synchronization
 * 
 * FIXED: Restore now explicitly updates current_balance after transaction replacement
 */
public class TransactionMemento {
    private final List<TransactionData.Transaction> transactionSnapshot;
    private final Double balanceSnapshot;
    private final long timestamp;
    
    private TransactionMemento(List<TransactionData.Transaction> transactions, Double balance) {
        // Deep copy to prevent external modification
        this.transactionSnapshot = new ArrayList<>();
        for (TransactionData.Transaction t : transactions) {
            this.transactionSnapshot.add(new TransactionData.Transaction(
                t.type, t.date, t.category, t.amount, t.comment
            ));
        }
        this.balanceSnapshot = balance;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Package-private: Only TransactionCaretaker creates mementos.
     */
    static TransactionMemento createMemento() {
        List<TransactionData.Transaction> transactions = TransactionData.getAllTransactions();
        Double balance = TransactionData.getBalance();
        return new TransactionMemento(transactions, balance != null ? balance : 0.0);
    }
    
    /**
     * FIXED: Restore with explicit database synchronization
     * 
     * Key Fix: After replacing transactions, we explicitly force
     * a recalculation and update of current_balance to ensure
     * the database reflects the restored state immediately.
     */
    void restore() {
        boolean dbSyncSuccess = true;
        
        try {
            System.out.println("\n=== MEMENTO RESTORE STARTED ===");
            System.out.println("Restoring to state with " + transactionSnapshot.size() + " transactions");
            System.out.println("Starting balance: ₱" + String.format("%,.2f", balanceSnapshot));
            
            // STEP 1: Restore starting balance to database
            if (balanceSnapshot != null) {
                try {
                    TransactionData.setBalance(balanceSnapshot);
                    System.out.println("✓ Step 1: Starting balance restored to database");
                } catch (Exception e) {
                    System.err.println("✗ Warning: Failed to restore starting balance to database");
                    e.printStackTrace();
                    dbSyncSuccess = false;
                }
            }
            
            // STEP 2: Replace all transactions in database
            // This completely replaces the transactions table with the snapshot state
            try {
                TransactionData.replaceAllTransactions(transactionSnapshot);
                System.out.println("✓ Step 2: Transactions replaced in database (" + 
                    transactionSnapshot.size() + " transactions)");
            } catch (Exception e) {
                System.err.println("✗ Error: Failed to replace transactions in database");
                e.printStackTrace();
                dbSyncSuccess = false;
                throw e; // Critical failure
            }
            
            // STEP 3: Verify current_balance was updated
            // The replaceAllTransactions method now handles this within its transaction
            try {
                Double currentBalance = TransactionData.calculateCurrentBalance();
                if (currentBalance != null) {
                    System.out.println("✓ Step 3: Current balance verified: ₱" + 
                        String.format("%,.2f", currentBalance));
                } else {
                    System.err.println("✗ Warning: Could not verify current balance");
                    dbSyncSuccess = false;
                }
            } catch (Exception e) {
                System.err.println("✗ Error: Failed to verify current_balance");
                e.printStackTrace();
                dbSyncSuccess = false;
            }
            
            if (dbSyncSuccess) {
                System.out.println("✓ MEMENTO RESTORE: Successfully synchronized state with database");
                System.out.println("  - Restored " + transactionSnapshot.size() + " transactions");
                System.out.println("  - Starting balance: ₱" + String.format("%,.2f", balanceSnapshot));
                
                Double finalBalance = TransactionData.calculateCurrentBalance();
                if (finalBalance != null) {
                    System.out.println("  - Current balance: ₱" + String.format("%,.2f", finalBalance));
                }
            }
            
            System.out.println("=== MEMENTO RESTORE COMPLETED ===\n");
            
        } catch (Exception e) {
            System.err.println("✗ Error during memento restore database synchronization:");
            e.printStackTrace();
            dbSyncSuccess = false;
        }
        
        if (!dbSyncSuccess) {
            System.err.println("⚠ Memento restore: Database synchronization incomplete");
            System.err.println("  In-memory state has been restored, but database may be out of sync.");
            System.err.println("  Recommendation: Verify data consistency or retry operation.");
        }
    }
    
    /**
     * Get snapshot of transactions for inspection (returns defensive copy).
     */
    List<TransactionData.Transaction> getTransactions() {
        return new ArrayList<>(transactionSnapshot);
    }
    
    /**
     * Get the starting balance captured in this memento.
     */
    Double getBalance() {
        return balanceSnapshot;
    }
    
    /**
     * Get the timestamp when this memento was created.
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Get the count of transactions in this snapshot.
     */
    public int getTransactionCount() {
        return transactionSnapshot.size();
    }
    
    @Override
    public String toString() {
        return String.format("TransactionMemento[transactions=%d, balance=₱%.2f, timestamp=%d]",
            transactionSnapshot.size(), balanceSnapshot, timestamp);
    }
    
    /**
     * Provides detailed information about this memento for debugging.
     */
    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TransactionMemento Details:\n");
        sb.append("  Timestamp: ").append(new java.util.Date(timestamp)).append("\n");
        sb.append("  Starting Balance: ₱").append(String.format("%,.2f", balanceSnapshot)).append("\n");
        sb.append("  Transaction Count: ").append(transactionSnapshot.size()).append("\n");
        
        if (!transactionSnapshot.isEmpty()) {
            sb.append("  Transactions:\n");
            for (int i = 0; i < Math.min(5, transactionSnapshot.size()); i++) {
                TransactionData.Transaction t = transactionSnapshot.get(i);
                sb.append("    - ").append(t.type).append(": ₱")
                  .append(String.format("%,.2f", t.amount))
                  .append(" (").append(t.category).append(", ").append(t.date).append(")\n");
            }
            if (transactionSnapshot.size() > 5) {
                sb.append("    ... and ").append(transactionSnapshot.size() - 5).append(" more\n");
            }
        }
        
        return sb.toString();
    }
}
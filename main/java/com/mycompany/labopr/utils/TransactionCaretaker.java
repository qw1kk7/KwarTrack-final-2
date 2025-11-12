package com.mycompany.labopr.utils;

import java.util.*;

/**
 * Enhanced Memento Pattern Caretaker with Database Synchronization
 * 
 * FIXED: Undo/Redo now properly synchronize database balance immediately
 */
public class TransactionCaretaker {
    private static final int MAX_HISTORY = 20;
    private final Deque<TransactionMemento> undoStack = new ArrayDeque<>();
    private final Deque<TransactionMemento> redoStack = new ArrayDeque<>();
    private boolean verboseLogging = false;
    
    public synchronized void saveState() {
        try {
            TransactionMemento memento = TransactionMemento.createMemento();
            undoStack.push(memento);
            
            if (verboseLogging) {
                System.out.println("State saved: " + memento);
            }
            
            while (undoStack.size() > MAX_HISTORY) {
                TransactionMemento removed = undoStack.removeLast();
                if (verboseLogging) {
                    System.out.println("Removed oldest state from history: " + removed);
                }
            }
            
            if (!redoStack.isEmpty()) {
                int clearedCount = redoStack.size();
                redoStack.clear();
                if (verboseLogging) {
                    System.out.println("Cleared " + clearedCount + " redo states (invalidated by new action)");
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error saving state: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public synchronized boolean canUndo() {
        return !undoStack.isEmpty();
    }
    
    public synchronized boolean canRedo() {
        return !redoStack.isEmpty();
    }
    
    /**
     * FIXED: Undo now properly synchronizes database balance
     * 
     * Key fix: We DON'T save current state first. Instead, we:
     * 1. Pop the previous state from undo stack
     * 2. Capture the CURRENT state (before restoration)
     * 3. Restore the previous state (this updates DB)
     * 4. Push the captured current state to redo stack
     */
    public synchronized boolean undo() {
        if (!canUndo()) {
            if (verboseLogging) {
                System.out.println("Undo requested but no history available");
            }
            return false;
        }
        
        try {
            if (verboseLogging) {
                System.out.println("\n=== UNDO OPERATION STARTED ===");
            }
            
            // STEP 1: Get the previous state we want to restore
            TransactionMemento previous = undoStack.pop();
            
            if (verboseLogging) {
                System.out.println("Previous state to restore: " + previous);
                System.out.println(previous.toDetailedString());
            }
            
            // STEP 2: Capture CURRENT state BEFORE restoration (for redo)
            TransactionMemento current = TransactionMemento.createMemento();
            
            if (verboseLogging) {
                System.out.println("Current state captured for redo: " + current);
            }
            
            // STEP 3: Restore previous state (this syncs DB automatically)
            previous.restore();
            
            // STEP 4: Push current state to redo stack
            redoStack.push(current);
            
            if (verboseLogging) {
                System.out.println("=== UNDO OPERATION COMPLETED ===\n");
                System.out.println("History status: " + this);
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Error during undo operation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * FIXED: Redo now properly synchronizes database balance
     * 
     * Key fix: Same pattern as undo - capture state before restoration
     */
    public synchronized boolean redo() {
        if (!canRedo()) {
            if (verboseLogging) {
                System.out.println("Redo requested but no redo history available");
            }
            return false;
        }
        
        try {
            if (verboseLogging) {
                System.out.println("\n=== REDO OPERATION STARTED ===");
            }
            
            // STEP 1: Get the next state we want to restore
            TransactionMemento next = redoStack.pop();
            
            if (verboseLogging) {
                System.out.println("Next state to restore: " + next);
                System.out.println(next.toDetailedString());
            }
            
            // STEP 2: Capture CURRENT state BEFORE restoration (for undo)
            TransactionMemento current = TransactionMemento.createMemento();
            
            if (verboseLogging) {
                System.out.println("Current state captured for undo: " + current);
            }
            
            // STEP 3: Restore next state (this syncs DB automatically)
            next.restore();
            
            // STEP 4: Push current state to undo stack
            undoStack.push(current);
            
            if (verboseLogging) {
                System.out.println("=== REDO OPERATION COMPLETED ===\n");
                System.out.println("History status: " + this);
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Error during redo operation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public synchronized void clear() {
        int undoCount = undoStack.size();
        int redoCount = redoStack.size();
        
        undoStack.clear();
        redoStack.clear();
        
        if (verboseLogging) {
            System.out.println("Cleared all history: " + undoCount + " undo states, " + redoCount + " redo states");
        }
    }
    
    public synchronized int getUndoCount() {
        return undoStack.size();
    }
    
    public synchronized int getRedoCount() {
        return redoStack.size();
    }
    
    public int getMaxHistory() {
        return MAX_HISTORY;
    }
    
    public void setVerboseLogging(boolean enabled) {
        this.verboseLogging = enabled;
        if (enabled) {
            System.out.println("Verbose logging enabled for TransactionCaretaker");
        }
    }
    
    public synchronized boolean isHistoryFull() {
        return undoStack.size() >= MAX_HISTORY;
    }
    
    public synchronized TransactionMemento peekUndo() {
        return undoStack.isEmpty() ? null : undoStack.peek();
    }
    
    public synchronized TransactionMemento peekRedo() {
        return redoStack.isEmpty() ? null : redoStack.peek();
    }
    
    public synchronized String getHistorySummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Transaction History Summary:\n");
        sb.append("  Undo available: ").append(undoStack.size()).append(" / ").append(MAX_HISTORY).append("\n");
        sb.append("  Redo available: ").append(redoStack.size()).append("\n");
        sb.append("  History full: ").append(isHistoryFull() ? "Yes" : "No").append("\n");
        
        if (!undoStack.isEmpty()) {
            TransactionMemento latest = undoStack.peek();
            sb.append("  Latest state: ").append(latest.getTransactionCount())
              .append(" transactions, Balance: ₱")
              .append(String.format("%,.2f", latest.getBalance())).append("\n");
        }
        
        return sb.toString();
    }
    
    @Override
    public synchronized String toString() {
        return String.format("TransactionCaretaker[undo=%d/%d, redo=%d]", 
            getUndoCount(), MAX_HISTORY, getRedoCount());
    }
}
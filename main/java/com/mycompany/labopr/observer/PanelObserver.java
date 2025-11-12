package com.mycompany.labopr.observer;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Observer Pattern: Centralized notification system for data changes
 * 
 * This singleton manages all observers (panels) and notifies them when data is updated.
 * Uses CopyOnWriteArraySet to handle concurrent modifications safely.
 * 
 * Benefits:
 * - Automatic UI updates across all panels
 * - Loose coupling between data operations and UI components
 * - Thread-safe observer management
 */
public class PanelObserver {
    
    private static PanelObserver instance;
    private final Set<Refreshable> observers;
    private boolean verboseLogging = false;
    
    private PanelObserver() {
        // CopyOnWriteArraySet ensures thread safety during iteration
        this.observers = new CopyOnWriteArraySet<>();
    }
    
    /**
     * Get the singleton instance of PanelObserver
     */
    public static synchronized PanelObserver getInstance() {
        if (instance == null) {
            instance = new PanelObserver();
        }
        return instance;
    }
    
    /**
     * Register a panel to receive data update notifications
     * 
     * @param observer The panel that implements Refreshable
     */
    public void registerObserver(Refreshable observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer cannot be null");
        }
        
        observers.add(observer);
        
        if (verboseLogging) {
            System.out.println("✓ Registered observer: " + observer.getClass().getSimpleName());
            System.out.println("  Total observers: " + observers.size());
        }
    }
    
    /**
     * Unregister a panel from receiving notifications
     * Should be called when panel is disposed/closed
     * 
     * @param observer The panel to unregister
     */
    public void unregisterObserver(Refreshable observer) {
        if (observer == null) {
            return;
        }
        
        boolean removed = observers.remove(observer);
        
        if (verboseLogging && removed) {
            System.out.println("✓ Unregistered observer: " + observer.getClass().getSimpleName());
            System.out.println("  Total observers: " + observers.size());
        }
    }
    
    /**
     * Notify all registered observers that data has changed
     * This triggers refreshData() on all panels
     * 
     * Call this after any data modification:
     * - Transaction added/edited/deleted
     * - Budget goal updated
     * - Settings changed
     * - Balance updated
     */
    public void notifyObservers() {
        if (verboseLogging) {
            System.out.println("\n=== NOTIFYING ALL OBSERVERS ===");
            System.out.println("Broadcasting update to " + observers.size() + " observer(s)");
        }
        
        int successCount = 0;
        int errorCount = 0;
        
        for (Refreshable observer : observers) {
            try {
                if (verboseLogging) {
                    System.out.println("  → Refreshing: " + observer.getClass().getSimpleName());
                }
                
                observer.refreshData();
                successCount++;
                
            } catch (Exception e) {
                errorCount++;
                System.err.println("✗ Error refreshing observer: " + observer.getClass().getSimpleName());
                e.printStackTrace();
            }
        }
        
        if (verboseLogging) {
            System.out.println("✓ Notification complete: " + successCount + " refreshed, " + errorCount + " errors");
            System.out.println("=== END OBSERVER NOTIFICATION ===\n");
        }
    }
    
    /**
     * Get the current number of registered observers
     */
    public int getObserverCount() {
        return observers.size();
    }
    
    /**
     * Remove all observers (typically used during app shutdown)
     */
    public void clearAllObservers() {
        int count = observers.size();
        observers.clear();
        
        if (verboseLogging) {
            System.out.println("✓ Cleared all observers: " + count + " removed");
        }
    }
    
    /**
     * Enable or disable verbose logging for debugging
     */
    public void setVerboseLogging(boolean enabled) {
        this.verboseLogging = enabled;
        if (enabled) {
            System.out.println("✓ Verbose logging enabled for PanelObserver");
        }
    }
    
    /**
     * Get detailed status of the observer system
     */
    public String getStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("PanelObserver Status:\n");
        sb.append("  Registered observers: ").append(observers.size()).append("\n");
        sb.append("  Observers:\n");
        
        for (Refreshable observer : observers) {
            sb.append("    - ").append(observer.getClass().getSimpleName()).append("\n");
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("PanelObserver[observers=%d]", observers.size());
    }
}
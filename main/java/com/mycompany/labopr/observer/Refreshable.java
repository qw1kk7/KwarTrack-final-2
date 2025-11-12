package com.mycompany.labopr.observer;

/**
 * Observer Pattern: Interface for panels that need to refresh when data changes
 * 
 * Any UI component that displays data should implement this interface
 * to receive automatic updates when the underlying data is modified.
 */
public interface Refreshable {
    /**
     * Called when data has been updated and the panel needs to refresh its display.
     * Implementations should re-fetch data from DataFacade and update their UI components.
     */
    void refreshData();
}
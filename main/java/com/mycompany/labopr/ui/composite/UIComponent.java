package com.mycompany.labopr.ui.composite;

import javax.swing.*;

/**
 * Composite Pattern: FULLY FUNCTIONAL base component interface
 * Defines uniform interface for both leaf and composite components
 */
public interface UIComponent {
    /**
     * Render component and return its visual representation
     */
    JPanel getPanel();
    
    /**
     * Update component's display (recalculate, refresh data)
     */
    void update();
    
    /**
     * Add child component (for composites only)
     * @throws UnsupportedOperationException for leaf components
     */
    default void addChild(UIComponent child) {
        throw new UnsupportedOperationException("This component cannot have children");
    }
    
    /**
     * Remove child component (for composites only)
     * @throws UnsupportedOperationException for leaf components
     */
    default void removeChild(UIComponent child) {
        throw new UnsupportedOperationException("This component cannot remove children");
    }
    
    /**
     * Get all children (for composites only)
     * @return empty list for leaf components
     */
    default java.util.List<UIComponent> getChildren() {
        return java.util.Collections.emptyList();
    }
    
    /**
     * Check if this is a composite (has children)
     */
    default boolean isComposite() {
        return false;
    }
}
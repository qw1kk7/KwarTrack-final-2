package com.mycompany.labopr.ui.composite;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Composite Pattern: FULLY FUNCTIONAL container holding multiple UIComponents
 * Implements recursive operations on tree structure
 */
public class CompositePanel implements UIComponent {
    private final List<UIComponent> children = new ArrayList<>();
    private final JPanel panel;
    private final LayoutManager layout;
    
    public CompositePanel(LayoutManager layout) {
        this.layout = layout;
        this.panel = new JPanel(layout);
    }
    
    @Override
    public void addChild(UIComponent child) {
        if (child == null) {
            throw new IllegalArgumentException("Child cannot be null");
        }
        children.add(child);
        panel.add(child.getPanel());
        panel.revalidate();
        panel.repaint();
    }
    
    @Override
    public void removeChild(UIComponent child) {
        if (children.remove(child)) {
            panel.remove(child.getPanel());
            panel.revalidate();
            panel.repaint();
        }
    }
    
    @Override
    public List<UIComponent> getChildren() {
        return new ArrayList<>(children);
    }
    
    @Override
    public boolean isComposite() {
        return true;
    }
    
    @Override
    public JPanel getPanel() {
        return panel;
    }
    
    @Override
    public void update() {
        // Recursive update: update all children first
        for (UIComponent child : children) {
            child.update();
        }
        
        // Refresh this panel
        panel.revalidate();
        panel.repaint();
    }
    
    /**
     * Clear all children
     */
    public void clear() {
        children.clear();
        panel.removeAll();
        panel.revalidate();
        panel.repaint();
    }
    
    /**
     * Get child count
     */
    public int getChildCount() {
        return children.size();
    }
    
    /**
     * Set panel background
     */
    public void setBackground(Color color) {
        panel.setBackground(color);
    }
    
    /**
     * Set panel border
     */
    public void setBorder(javax.swing.border.Border border) {
        panel.setBorder(border);
    }
    
    /**
     * Set panel opaque property
     */
    public void setOpaque(boolean opaque) {
        panel.setOpaque(opaque);
    }
    
    @Override
    public String toString() {
        return String.format("CompositePanel[children=%d, layout=%s]", 
            children.size(), layout.getClass().getSimpleName());
    }
}
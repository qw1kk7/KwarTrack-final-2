package com.mycompany.labopr.ui.factories;

import javax.swing.*;
import java.awt.*;

/**
 * Abstract Factory Method Pattern: ButtonFactory
 * Defines the interface for creating buttons without specifying concrete classes
 */
public abstract class ButtonFactory {
    
    // Factory Method - subclasses implement this
    public abstract JButton createButton(String text);
    
    // Factory Method with size specification
    public abstract JButton createButton(String text, Dimension size);
    
    // Factory Method for icon buttons
    public abstract JButton createIconButton(String iconText, String tooltip);
    
    // Factory Method for toggle buttons
    public abstract JButton createToggleButton(String text, boolean selected);
    
    // Factory Method for navigation buttons
    public abstract JButton createNavButton(String text);
    
    // Factory Method for theme toggle button
    public abstract JButton createThemeToggleButton();
    
    // Utility method for updating toggle state (can be overridden)
    public void updateToggleButtonState(JButton btn, boolean selected) {
        // Default implementation - can be overridden by concrete factories
    }
    
    // Utility method for theme updates (can be overridden)
    public void updateButtonForThemeChange(JButton btn) {
        // Default implementation - can be overridden by concrete factories
    }
}
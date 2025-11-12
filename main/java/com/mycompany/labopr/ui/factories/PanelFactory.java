package com.mycompany.labopr.ui.factories;

import javax.swing.*;
import java.awt.*;

/**
 * Abstract Factory Method Pattern: PanelFactory
 * Defines the interface for creating panels without specifying concrete classes
 */
public abstract class PanelFactory {
    
    // Factory Method - subclasses implement these
    public abstract JPanel createPanel();
    
    public abstract JPanel createPanel(LayoutManager layout);
    
    public abstract JPanel createPanel(Color bgColor);
    
    public abstract JPanel createPanel(LayoutManager layout, Color bgColor);
        
    public abstract JPanel createFlowPanel(int align, int hgap, int vgap);
    
    public abstract JPanel createPaddedPanel(int top, int left, int bottom, int right);
    
    public abstract JPanel createBorderPanel();
}
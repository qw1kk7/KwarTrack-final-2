package com.mycompany.labopr.ui.composite;

import com.mycompany.labopr.ui.theme.UITheme;
import javax.swing.*;
import java.awt.*;

/**
 * Composite Pattern: FULLY FUNCTIONAL leaf component (metric card)
 * Cannot have children, focuses on rendering single metric
 */
public class MetricCardComponent implements UIComponent {
    private final String label;
    private double value;
    private final Color accentColor;
    private final boolean isPercentage;
    private JPanel panel;
    private JLabel valueLabel;
    
    public MetricCardComponent(String label, double value, Color accentColor) {
        this(label, value, accentColor, false);
    }
    
    public MetricCardComponent(String label, double value, Color accentColor, boolean isPercentage) {
        this.label = label;
        this.value = value;
        this.accentColor = accentColor;
        this.isPercentage = isPercentage;
        buildPanel();
    }
    
    private void buildPanel() {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accentColor, 2),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        panel.setPreferredSize(new Dimension(200, 100));
        panel.setMaximumSize(new Dimension(200, 100));
        
        JLabel labelText = new JLabel(label);
        labelText.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 14));
        labelText.setForeground(Color.DARK_GRAY);
        labelText.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        valueLabel = new JLabel(formatValue());
        valueLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 22));
        valueLabel.setForeground(accentColor);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(Box.createVerticalGlue());
        panel.add(labelText);
        panel.add(Box.createVerticalStrut(10));
        panel.add(valueLabel);
        panel.add(Box.createVerticalGlue());
    }
    
    private String formatValue() {
        if (isPercentage) {
            return String.format("%.1f%%", value);
        } else {
            return "₱" + String.format("%,.2f", value);
        }
    }
    
    @Override
    public JPanel getPanel() {
        return panel;
    }
    
    @Override
    public void update() {
        if (valueLabel != null) {
            valueLabel.setText(formatValue());
            panel.revalidate();
            panel.repaint();
        }
    }
    
    /**
     * Update the value and refresh display
     */
    public void setValue(double newValue) {
        this.value = newValue;
        update();
    }
    
    /**
     * Get current value
     */
    public double getValue() {
        return value;
    }
    
    /**
     * Get label text
     */
    public String getLabel() {
        return label;
    }
    
    @Override
    public String toString() {
        return String.format("MetricCard[label=%s, value=%.2f, percentage=%b]", 
            label, value, isPercentage);
    }
}
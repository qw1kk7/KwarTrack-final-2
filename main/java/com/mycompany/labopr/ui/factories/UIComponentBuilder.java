package com.mycompany.labopr.ui.factories;

import com.mycompany.labopr.data.DataFacade;
import com.mycompany.labopr.ui.panels.*;
import com.mycompany.labopr.ui.composite.CompositePanel;
import com.mycompany.labopr.ui.composite.MetricCardComponent;
import com.mycompany.labopr.ui.theme.UITheme;
import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Helper class for building common UI components using the factory pattern
 * This is NOT a factory itself, but uses factories internally
 */
public class UIComponentBuilder {
    
    private final ButtonFactory buttonFactory;
    private final PanelFactory panelFactory;
    
    public UIComponentBuilder(ButtonFactory buttonFactory, PanelFactory panelFactory) {
        this.buttonFactory = buttonFactory;
        this.panelFactory = panelFactory;
    }
    
    // ==================== LABEL CREATION ====================
    
    public JLabel createLogoLabel(String path, int width, int height) {
        JLabel label = new JLabel();
        ImageIcon icon = UITheme.getCachedLogo(path, width, height);
        if (icon != null) {
            label.setIcon(icon);
        } else {
            label.setText("KwarTrack");
            label.setForeground(UITheme.TEXT_COLOR);
            label.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, UITheme.FONT_LARGE));
        }
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    public JLabel createLabel(String text, int fontSize, boolean bold) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(UITheme.TEXT_COLOR);
        label.setFont(new Font(UITheme.FONT_FAMILY, bold ? Font.BOLD : Font.PLAIN, fontSize));
        return label;
    }
    
    // ==================== NAVIGATION PANELS ====================
    
    public JPanel createNavPanel() {
        JPanel panel = panelFactory.createFlowPanel(FlowLayout.RIGHT, 20, 10);
        panel.setBackground(UITheme.PRIMARY_GREEN);
        return panel;
    }
    
    public SidebarPanel createSidebarPanel(String[] buttonLabels, 
                                          Consumer<Integer> navigationHandler, 
                                          Runnable logoutHandler) {
        return new SidebarPanel(buttonLabels, navigationHandler, logoutHandler, buttonFactory);
    }
    
    // ==================== FEATURE PANELS (WITH DATAFACADE) ====================
    
    public DashboardPanel createDashboardPanel(JFrame parentFrame, DataFacade dataFacade) {
        return new DashboardPanel(parentFrame, dataFacade, buttonFactory, panelFactory);
    }
    
    public TransactionsPanel createTransactionsPanel(JFrame parentFrame, DataFacade dataFacade) {
        return new TransactionsPanel(parentFrame, dataFacade, buttonFactory, panelFactory);
    }
    
    public BudgetsPanel createBudgetsPanel(JFrame parentFrame, DataFacade dataFacade) {
        return new BudgetsPanel(parentFrame, dataFacade, buttonFactory, panelFactory);
    }
    
    public SettingsPanel createSettingsPanel(JFrame parentFrame, DataFacade dataFacade) {
        return new SettingsPanel(parentFrame, dataFacade, buttonFactory, panelFactory);
    }
    
    public ChartPanel createPieChartPanel(String title, java.util.Map<String, Double> data) {
        return new ChartPanel(title, data);
    }
    
    public ChartPanel createBarChartPanel(String title, 
                                         java.util.Map<String, com.mycompany.labopr.data.AnalyticsData.MonthlyData> data) {
        return new ChartPanel(title, data, true);
    }
    
    // ==================== COMPOSITE PANELS ====================
    
    public CompositePanel createCompositePanel(LayoutManager layout) {
        return new CompositePanel(layout);
    }
    
    public MetricCardComponent createMetricCard(String label, double value, Color accentColor) {
        return new MetricCardComponent(label, value, accentColor);
    }
    
    public MetricCardComponent createPercentageMetricCard(String label, double value, Color accentColor) {
        return new MetricCardComponent(label, value, accentColor, true);
    }
    
    // ==================== UTILITY PANELS ====================
    
    public JPanel createSectionPanel(String title) {
        JPanel panel = panelFactory.createPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xcccccc), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        panel.setMaximumSize(new Dimension(800, Integer.MAX_VALUE));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 20));
        titleLabel.setForeground(new Color(0x333333));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));
        
        return panel;
    }
    
    public JPanel createFieldRow(String labelText, JComponent field) {
        JPanel row = panelFactory.createFlowPanel(FlowLayout.LEFT, 10, 0);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 14));
        label.setForeground(new Color(0x333333));
        label.setPreferredSize(new Dimension(120, 25));
        
        if (field instanceof JTextField) {
            field.setPreferredSize(new Dimension(300, 30));
        } else if (field instanceof JComboBox) {
            field.setPreferredSize(new Dimension(200, 30));
        }
        
        row.add(label);
        row.add(field);
        
        return row;
    }
    
    public JPanel createTopCategoryCard(int rank, String category, double amount, Color accentColor) {
        JPanel card = panelFactory.createPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accentColor, 2),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        card.setPreferredSize(new Dimension(220, 90));
        
        JLabel rankLabel = new JLabel("#" + rank);
        rankLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 16));
        rankLabel.setForeground(accentColor);
        rankLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel categoryLabel = new JLabel(category);
        categoryLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 14));
        categoryLabel.setForeground(Color.DARK_GRAY);
        categoryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel amountLabel = new JLabel("₱" + String.format("%,.2f", amount));
        amountLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 18));
        amountLabel.setForeground(Color.BLACK);
        amountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        card.add(rankLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(categoryLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(amountLabel);
        
        return card;
    }
}
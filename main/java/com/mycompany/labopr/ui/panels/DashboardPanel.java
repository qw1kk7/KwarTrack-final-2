package com.mycompany.labopr.ui.panels;

import com.mycompany.labopr.data.DataFacade;
import com.mycompany.labopr.data.DataFacade.DashboardData;
import com.mycompany.labopr.data.DataFacade.AnalyticsSummary;
import com.mycompany.labopr.data.AnalyticsData.MonthlyData;
import com.mycompany.labopr.data.AnalyticsData.CategorySpending;
import com.mycompany.labopr.data.TransactionData.Transaction;
import com.mycompany.labopr.observer.PanelObserver;
import com.mycompany.labopr.observer.Refreshable;
import com.mycompany.labopr.ui.factories.ButtonFactory;
import com.mycompany.labopr.ui.factories.PanelFactory;
import com.mycompany.labopr.ui.composite.CompositePanel;
import com.mycompany.labopr.ui.composite.MetricCardComponent;
import com.mycompany.labopr.ui.theme.UITheme;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * DashboardPanel - REFACTORED to use Composite Pattern
 */
public class DashboardPanel extends JPanel implements UITheme.ThemeChangeListener, Refreshable {
    
    private final DataFacade dataFacade;
    private final JFrame parentFrame;
    private final ButtonFactory buttonFactory;
    private final PanelFactory panelFactory;
    
    private String currentMonth;
    
    // CHANGED: Now using Composite Pattern components
    private MetricCardComponent balanceCard;
    private MetricCardComponent incomeCard;
    private MetricCardComponent expensesCard;
    private MetricCardComponent savingsCard;
    private CompositePanel overviewMetricsContainer;
    
    private MetricCardComponent monthlyIncomeCard;
    private MetricCardComponent monthlyExpensesCard;
    private MetricCardComponent monthlySavingsCard;
    private MetricCardComponent savingsRateCard;
    private CompositePanel analyticsMetricsContainer;
    
    private JComboBox<String> monthSelector;
    private JPanel chartsPanel;
    private JPanel topCategoriesPanel;
    private JTable recentTransactionsTable;
    private DefaultTableModel transactionTableModel;
    private JLabel budgetStatusLabel;
    private JButton themeToggleBtn;
    private JScrollPane mainScrollPane;
    private JPanel titlePanel;
    private JLabel dashboardTitleLabel;
    
    public DashboardPanel(JFrame parentFrame, DataFacade dataFacade, 
                         ButtonFactory buttonFactory, PanelFactory panelFactory) {
        this.parentFrame = parentFrame;
        this.dataFacade = dataFacade;
        this.buttonFactory = buttonFactory;
        this.panelFactory = panelFactory;
        this.currentMonth = new SimpleDateFormat("yyyy-MM").format(new Date());
        
        setLayout(new BorderLayout());
        setBackground(UITheme.isDarkMode() ? Color.BLACK : UITheme.PRIMARY_GREEN);
        
        UITheme.addThemeChangeListener(this);
        PanelObserver.getInstance().registerObserver(this);
        
        initComponents();
        loadDashboardData();
    }
    
    @Override
    public void refreshData() {
        SwingUtilities.invokeLater(() -> {
            loadDashboardData();
        });
    }
    
    private void initComponents() {
        JPanel mainContentPanel = panelFactory.createPanel(new GridBagLayout());
        mainContentPanel.setBackground(UITheme.isDarkMode() ? Color.BLACK : UITheme.PRIMARY_GREEN);
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);
        
        mainContentPanel.add(createTitleSection(), gbc);
        
        gbc.gridy++;
        mainContentPanel.add(createOverviewSection(), gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(10, 0, 10, 0);
        mainContentPanel.add(createMonthSelectorSection(), gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainContentPanel.add(createAnalyticsMetricsSection(), gbc);
        
        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.4;
        mainContentPanel.add(createChartsSection(), gbc);
        
        gbc.gridy++;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainContentPanel.add(createTopCategoriesSection(), gbc);
        
        gbc.gridy++;
        gbc.weighty = 0.3;
        gbc.fill = GridBagConstraints.BOTH;
        mainContentPanel.add(createRecentTransactionsSection(), gbc);
        
        mainScrollPane = new JScrollPane(mainContentPanel);
        mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainScrollPane.getViewport().setBackground(UITheme.isDarkMode() ? Color.BLACK : UITheme.PRIMARY_GREEN);
        mainScrollPane.setBorder(null);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(mainScrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createTitleSection() {
        titlePanel = panelFactory.createPanel(new BorderLayout());
        titlePanel.setBackground(UITheme.isDarkMode() ? Color.BLACK : UITheme.PRIMARY_GREEN);
        
        dashboardTitleLabel = new JLabel("Dashboard Overview", SwingConstants.CENTER);
        dashboardTitleLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 32));
        dashboardTitleLabel.setForeground(UITheme.TEXT_COLOR);
        titlePanel.add(dashboardTitleLabel, BorderLayout.WEST);
        
        JPanel rightPanel = panelFactory.createFlowPanel(FlowLayout.RIGHT, 5, 0);
        rightPanel.setOpaque(false);
        themeToggleBtn = buttonFactory.createThemeToggleButton();
        themeToggleBtn.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.revalidate();
                window.repaint();
            }
        });
        rightPanel.add(themeToggleBtn);
        titlePanel.add(rightPanel, BorderLayout.EAST);
        
        return titlePanel;
    }
    
    private JPanel createOverviewSection() {
        JPanel section = panelFactory.createPanel(new GridBagLayout());
        section.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 15, 0);
        
        JLabel sectionTitle = createSectionTitle("Financial Overview", 24);
        section.add(sectionTitle, gbc);
        
        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        
        // CHANGED: Create Composite container with metric cards
        overviewMetricsContainer = new CompositePanel(new GridLayout(1, 4, 15, 0));
        overviewMetricsContainer.setBackground(UITheme.isDarkMode() ? Color.BLACK : UITheme.PRIMARY_GREEN);
        overviewMetricsContainer.setOpaque(false);
        
        // Create metric card components
        balanceCard = new MetricCardComponent("Current Balance", 0.0, new Color(0x7ed957));
        incomeCard = new MetricCardComponent("Monthly Income", 0.0, new Color(0x4fc3f7));
        expensesCard = new MetricCardComponent("Monthly Expenses", 0.0, new Color(0xe57373));
        savingsCard = new MetricCardComponent("Monthly Savings", 0.0, new Color(0x66bb6a));
        
        // Add cards to composite container
        overviewMetricsContainer.addChild(balanceCard);
        overviewMetricsContainer.addChild(incomeCard);
        overviewMetricsContainer.addChild(expensesCard);
        overviewMetricsContainer.addChild(savingsCard);
        
        section.add(overviewMetricsContainer.getPanel(), gbc);
        
        gbc.gridy++;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(15, 0, 0, 0);
        
        JPanel statusPanel = panelFactory.createFlowPanel(FlowLayout.CENTER, 0, 0);
        statusPanel.setOpaque(false);
        budgetStatusLabel = new JLabel("");
        budgetStatusLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 14));
        statusPanel.add(budgetStatusLabel);
        
        section.add(statusPanel, gbc);
        
        return section;
    }
    
    private JPanel createMonthSelectorSection() {
        JPanel panel = panelFactory.createFlowPanel(FlowLayout.CENTER, 10, 5);
        panel.setOpaque(false);
        
        JLabel monthLabel = new JLabel("Analytics for Month:");
        monthLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 16));
        monthLabel.setForeground(UITheme.TEXT_COLOR);
        
        String[] months = generateMonthOptions();
        monthSelector = new JComboBox<>(months);
        monthSelector.setSelectedItem(currentMonth);
        monthSelector.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 14));
        monthSelector.setPreferredSize(new Dimension(150, 30));
        monthSelector.addActionListener(e -> {
            currentMonth = (String) monthSelector.getSelectedItem();
            loadAnalyticsData();
        });
        
        panel.add(monthLabel);
        panel.add(monthSelector);
        
        return panel;
    }
    
    private JPanel createAnalyticsMetricsSection() {
        JPanel section = panelFactory.createPanel(new GridBagLayout());
        section.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 15, 0);
        
        JLabel sectionTitle = createSectionTitle("Monthly Analytics", 24);
        section.add(sectionTitle, gbc);
        
        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        
        // CHANGED: Create Composite container with metric cards
        analyticsMetricsContainer = new CompositePanel(new GridLayout(1, 4, 15, 0));
        analyticsMetricsContainer.setBackground(UITheme.isDarkMode() ? Color.BLACK : UITheme.PRIMARY_GREEN);
        analyticsMetricsContainer.setOpaque(false);
        
        // Create metric card components
        monthlyIncomeCard = new MetricCardComponent("Total Income", 0.0, new Color(0x7ed957), false);
        monthlyExpensesCard = new MetricCardComponent("Total Expenses", 0.0, new Color(0xe57373), false);
        monthlySavingsCard = new MetricCardComponent("Net Savings", 0.0, new Color(0x66bb6a), false);
        savingsRateCard = new MetricCardComponent("Savings Rate", 0.0, new Color(0x66bb6a), true);
        
        // Add cards to composite container
        analyticsMetricsContainer.addChild(monthlyIncomeCard);
        analyticsMetricsContainer.addChild(monthlyExpensesCard);
        analyticsMetricsContainer.addChild(monthlySavingsCard);
        analyticsMetricsContainer.addChild(savingsRateCard);
        
        section.add(analyticsMetricsContainer.getPanel(), gbc);
        
        return section;
    }
    
    private JPanel createChartsSection() {
        JPanel section = panelFactory.createPanel(new GridBagLayout());
        section.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 15, 0);
        
        JLabel sectionTitle = createSectionTitle("Visual Analytics", 24);
        section.add(sectionTitle, gbc);
        
        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        
        chartsPanel = panelFactory.createPanel(new GridLayout(1, 2, 20, 0));
        chartsPanel.setOpaque(false);
        
        section.add(chartsPanel, gbc);
        
        return section;
    }
    
    private JPanel createTopCategoriesSection() {
        JPanel section = panelFactory.createPanel(new GridBagLayout());
        section.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 15, 0);
        
        JLabel sectionTitle = createSectionTitle("Top Spending Categories", 24);
        section.add(sectionTitle, gbc);
        
        gbc.gridy++;
        topCategoriesPanel = panelFactory.createFlowPanel(FlowLayout.CENTER, 15, 10);
        topCategoriesPanel.setOpaque(false);
        
        section.add(topCategoriesPanel, gbc);
        
        return section;
    }
    
    private JPanel createRecentTransactionsSection() {
        JPanel section = panelFactory.createPanel(new GridBagLayout());
        section.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 15, 0);
        
        JLabel sectionTitle = createSectionTitle("Recent Transactions", 24);
        section.add(sectionTitle, gbc);
        
        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        
        String[] columnNames = {"Date", "Type", "Category", "Amount"};
        transactionTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        recentTransactionsTable = new JTable(transactionTableModel);
        recentTransactionsTable.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 14));
        recentTransactionsTable.setRowHeight(30);
        recentTransactionsTable.getTableHeader().setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 14));
        
        updateTableHeaderColors();
        
        recentTransactionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recentTransactionsTable.setFillsViewportHeight(true);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        recentTransactionsTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        
        JScrollPane scrollPane = new JScrollPane(recentTransactionsTable);
        scrollPane.setBackground(Color.WHITE);
        
        section.add(scrollPane, gbc);
        
        return section;
    }
    
    private JLabel createSectionTitle(String text, int fontSize) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, fontSize));
        label.setForeground(UITheme.TEXT_COLOR);
        return label;
    }
    
    private String[] generateMonthOptions() {
        List<String> months = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        
        cal.add(Calendar.MONTH, -11);
        
        for (int i = 0; i < 24; i++) {
            months.add(sdf.format(cal.getTime()));
            cal.add(Calendar.MONTH, 1);
        }
        
        return months.toArray(new String[0]);
    }
    
    private void loadDashboardData() {
        DashboardData data = dataFacade.getDashboardData(currentMonth);
        
        // CHANGED: Update metric cards using setValue()
        balanceCard.setValue(data.currentBalance);
        incomeCard.setValue(data.monthlyIncome);
        expensesCard.setValue(data.monthlyExpenses);
        savingsCard.setValue(data.monthlySavings);
        
        // Update composite (propagates to all children)
        overviewMetricsContainer.update();
        
        if (data.isOverBudget) {
            budgetStatusLabel.setText("⚠️ Warning: You are over budget for this month");
            budgetStatusLabel.setForeground(new Color(0xe57373));
        } else {
            budgetStatusLabel.setText("✓ You are within your budget");
            budgetStatusLabel.setForeground(UITheme.isDarkMode() ? Color.WHITE : Color.BLACK);
        }
        
        transactionTableModel.setRowCount(0);
        for (Transaction t : data.recentTransactions) {
            transactionTableModel.addRow(new Object[]{
                t.date,
                t.type,
                t.category,
                "₱" + String.format("%,.2f", t.amount)
            });
        }
        
        loadAnalyticsData();
    }
    
    private void loadAnalyticsData() {
        AnalyticsSummary analytics = dataFacade.getAnalyticsSummary(currentMonth);
        
        // CHANGED: Update metric cards and their colors dynamically
        monthlyIncomeCard.setValue(analytics.totalIncome);
        monthlyExpensesCard.setValue(analytics.totalExpenses);
        
        // Update savings card color based on value
        Color savingsColor = analytics.netSavings >= 0 ? new Color(0x66bb6a) : new Color(0xef5350);
        monthlySavingsCard = new MetricCardComponent("Net Savings", analytics.netSavings, savingsColor, false);
        
        // Update savings rate card color based on value
        Color rateColor = analytics.savingsRate >= 0 ? new Color(0x66bb6a) : new Color(0xef5350);
        savingsRateCard = new MetricCardComponent("Savings Rate", analytics.savingsRate, rateColor, true);
        
        // Rebuild the analytics container with updated cards
        analyticsMetricsContainer.removeChild(monthlySavingsCard);
        analyticsMetricsContainer.removeChild(savingsRateCard);
        analyticsMetricsContainer.addChild(monthlySavingsCard);
        analyticsMetricsContainer.addChild(savingsRateCard);
        
        // Update composite (propagates to all children)
        analyticsMetricsContainer.update();
        
        loadCharts(analytics);
        loadTopCategories(analytics);
    }
    
    private void updateTableHeaderColors() {
        boolean isDark = UITheme.isDarkMode();
        
        if (isDark) {
            recentTransactionsTable.getTableHeader().setBackground(Color.BLACK);
            recentTransactionsTable.getTableHeader().setForeground(Color.WHITE);
        } else {
            recentTransactionsTable.getTableHeader().setBackground(Color.WHITE);
            recentTransactionsTable.getTableHeader().setForeground(Color.BLACK);
        }
    }
    
    private void loadCharts(AnalyticsSummary analytics) {
        chartsPanel.removeAll();
        
        ChartPanel pieChart = new ChartPanel("Spending by Category", analytics.spendingByCategory);
        chartsPanel.add(pieChart);
        
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        List<String> trendMonths = new ArrayList<>();
        
        try {
            cal.setTime(sdf.parse(currentMonth));
        } catch (Exception e) {
            cal = Calendar.getInstance();
        }
        
        cal.add(Calendar.MONTH, -2);
        for (int i = 0; i < 3; i++) {
            trendMonths.add(sdf.format(cal.getTime()));
            cal.add(Calendar.MONTH, 1);
        }
        
        Map<String, MonthlyData> trendData = dataFacade.getIncomeExpensesTrend(trendMonths);
        ChartPanel barChart = new ChartPanel("Income vs Expenses Trend", trendData, true);
        chartsPanel.add(barChart);
        
        chartsPanel.revalidate();
        chartsPanel.repaint();
    }
    
    private void loadTopCategories(AnalyticsSummary analytics) {
        topCategoriesPanel.removeAll();
        
        if (analytics.topCategories.isEmpty()) {
            topCategoriesPanel.revalidate();
            topCategoriesPanel.repaint();
            return;
        }
        
        Color[] colors = {
            new Color(0xff6b6b),
            new Color(0xfeca57),
            new Color(0x48dbfb)
        };
        
        for (int i = 0; i < analytics.topCategories.size(); i++) {
            CategorySpending cat = analytics.topCategories.get(i);
            JPanel card = createTopCategoryCard(i + 1, cat.category, cat.amount, colors[i]);
            topCategoriesPanel.add(card);
        }
        
        topCategoriesPanel.revalidate();
        topCategoriesPanel.repaint();
    }
    
    private JPanel createTopCategoryCard(int rank, String category, double amount, Color accentColor) {
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
    
    @Override
    public void onThemeChanged() {
        boolean isDark = UITheme.isDarkMode();
        Color bgColor = isDark ? Color.BLACK : UITheme.PRIMARY_GREEN;
        
        setBackground(bgColor);
        
        if (titlePanel != null) {
            titlePanel.setBackground(bgColor);
        }
        
        if (dashboardTitleLabel != null) {
            dashboardTitleLabel.setForeground(UITheme.TEXT_COLOR);
        }
        
        // Update composite containers background
        if (overviewMetricsContainer != null) {
            overviewMetricsContainer.setBackground(bgColor);
        }
        
        if (analyticsMetricsContainer != null) {
            analyticsMetricsContainer.setBackground(bgColor);
        }
        
        if (mainScrollPane != null) {
            mainScrollPane.getViewport().setBackground(bgColor);
            Component view = mainScrollPane.getViewport().getView();
            if (view instanceof JPanel) {
                ((JPanel) view).setBackground(bgColor);
            }
        }
        
        updateSectionBackgrounds(this, bgColor);
        updateSectionTitleColors(this);
        
        recentTransactionsTable.getTableHeader().setBackground(UITheme.BUTTON_BG);
        recentTransactionsTable.getTableHeader().setForeground(UITheme.BUTTON_TEXT);
        
        if (budgetStatusLabel.getText().contains("within")) {
            budgetStatusLabel.setForeground(isDark ? Color.WHITE : Color.BLACK);
        }
        
        repaint();
    }
    
    private void updateSectionBackgrounds(Container container, Color bgColor) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                if (!panel.getBackground().equals(Color.WHITE)) {
                    panel.setBackground(bgColor);
                    updateSectionBackgrounds(panel, bgColor);
                }
            } else if (comp instanceof JScrollPane) {
                JScrollPane scroll = (JScrollPane) comp;
                if (!scroll.getBackground().equals(Color.WHITE)) {
                    scroll.getViewport().setBackground(bgColor);
                }
            }
        }
    }
    
    private void updateSectionTitleColors(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if (label.getFont().getSize() >= 20 && 
                    !label.getForeground().equals(Color.DARK_GRAY) &&
                    !label.getForeground().equals(Color.BLACK)) {
                    label.setForeground(UITheme.TEXT_COLOR);
                }
            } else if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                if (!panel.getBackground().equals(Color.WHITE)) {
                    updateSectionTitleColors(panel);
                }
            }
        }
    }
    
    public void cleanup() {
        PanelObserver.getInstance().unregisterObserver(this);
        UITheme.removeThemeChangeListener(this);
    }
}

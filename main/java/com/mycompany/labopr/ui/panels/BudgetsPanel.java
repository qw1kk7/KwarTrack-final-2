package com.mycompany.labopr.ui.panels;

import com.mycompany.labopr.data.DataFacade;
import com.mycompany.labopr.data.BudgetData.BudgetStatusInfo;
import com.mycompany.labopr.observer.PanelObserver;
import com.mycompany.labopr.observer.Refreshable;
import com.mycompany.labopr.ui.dialogs.BudgetGoalDialog;
import com.mycompany.labopr.ui.factories.ButtonFactory;
import com.mycompany.labopr.ui.factories.PanelFactory;
import com.mycompany.labopr.ui.theme.UITheme;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * BudgetsPanel - FIXED: Dynamic title panel color (PRIMARY_GREEN in light, BLACK in dark)
 */
public class BudgetsPanel extends JPanel implements UITheme.ThemeChangeListener, Refreshable {
    private final DataFacade dataFacade;
    private final JFrame parentFrame;
    private final ButtonFactory buttonFactory;
    private final PanelFactory panelFactory;
    
    private JComboBox<String> monthSelector;
    private JTable budgetTable;
    private DefaultTableModel tableModel;
    private String currentMonth;
    private JButton themeToggleBtn;
    private JPanel topPanel; // FIXED: Store reference for theme updates
    private JLabel titleLabel; // FIXED: Store reference for text color updates
    
    public BudgetsPanel(JFrame parent, DataFacade dataFacade, 
                       ButtonFactory buttonFactory, PanelFactory panelFactory) {
        this.parentFrame = parent;
        this.dataFacade = dataFacade;
        this.buttonFactory = buttonFactory;
        this.panelFactory = panelFactory;
        this.currentMonth = new SimpleDateFormat("yyyy-MM").format(new Date());
        
        setLayout(new BorderLayout(10, 10));
        // FIXED: Background changes with theme
        setBackground(UITheme.isDarkMode() ? Color.BLACK : UITheme.PRIMARY_GREEN);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        UITheme.addThemeChangeListener(this);
        PanelObserver.getInstance().registerObserver(this);
        
        initComponents();
        loadBudgets();
    }
    
    @Override
    public void refreshData() {
        SwingUtilities.invokeLater(() -> {
            loadBudgets();
        });
    }
    
    private void initComponents() {
        // FIXED: Title panel changes color with theme
        topPanel = panelFactory.createPanel(new GridBagLayout());
        topPanel.setBackground(UITheme.isDarkMode() ? Color.BLACK : UITheme.PRIMARY_GREEN);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 10, 0);
        
        // Title - centered
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        
        titleLabel = new JLabel("Monthly Budget Goals");
        titleLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 32));
        titleLabel.setForeground(UITheme.TEXT_COLOR);
        topPanel.add(titleLabel, gbc);
        
        // Theme toggle - top right
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        
        themeToggleBtn = buttonFactory.createThemeToggleButton();
        themeToggleBtn.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.revalidate();
                window.repaint();
            }
        });
        topPanel.add(themeToggleBtn, gbc);
        
        // Month selector - centered
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel monthPanel = panelFactory.createFlowPanel(FlowLayout.CENTER, 10, 5);
        monthPanel.setOpaque(false);
        
        JLabel monthLabel = new JLabel("Select Month:");
        monthLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 16));
        monthLabel.setForeground(UITheme.TEXT_COLOR);
        
        String[] months = generateMonthOptions();
        monthSelector = new JComboBox<>(months);
        monthSelector.setSelectedItem(currentMonth);
        monthSelector.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 14));
        monthSelector.setPreferredSize(new Dimension(150, 30));
        monthSelector.addActionListener(e -> {
            currentMonth = (String) monthSelector.getSelectedItem();
            loadBudgets();
        });
        
        monthPanel.add(monthLabel);
        monthPanel.add(monthSelector);
        topPanel.add(monthPanel, gbc);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Budget table
        String[] columnNames = {"Category", "Goal (₱)", "Spent (₱)", "Remaining (₱)", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        budgetTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                
                if (column == 4) {
                    String status = (String) getValueAt(row, column);
                    if (status.equals("Under Budget")) {
                        c.setBackground(new Color(0xc8e6c9));
                        c.setForeground(new Color(0x2e7d32));
                    } else if (status.equals("Nearing Limit")) {
                        c.setBackground(new Color(0xfff9c4));
                        c.setForeground(new Color(0xf57f17));
                    } else if (status.equals("Overspent")) {
                        c.setBackground(new Color(0xffcdd2));
                        c.setForeground(new Color(0xc62828));
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    }
                } else {
                    if (!isRowSelected(row)) {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    }
                }
                
                return c;
            }
        };
        
        budgetTable.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 14));
        budgetTable.setRowHeight(35);
        budgetTable.getTableHeader().setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 14));
        budgetTable.getTableHeader().setBackground(UITheme.BUTTON_BG);
        budgetTable.getTableHeader().setForeground(UITheme.BUTTON_TEXT);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        budgetTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        budgetTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        budgetTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        budgetTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        
        budgetTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = budgetTable.getSelectedRow();
                    if (row >= 0) {
                        handleEditGoal(row);
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(budgetTable);
        scrollPane.setBackground(Color.WHITE);
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Bottom buttons - FIXED: Wider buttons for proper text fit
        JPanel bottomPanel = panelFactory.createFlowPanel(FlowLayout.RIGHT, 10, 10);
        bottomPanel.setOpaque(false);
        
        JButton addEditBtn = buttonFactory.createButton("Add/Edit Goal", new Dimension(170, 45));
        addEditBtn.addActionListener(e -> handleAddEditGoal());
        
        JButton setAllBtn = buttonFactory.createButton("Set All Goals", new Dimension(170, 45));
        setAllBtn.addActionListener(e -> handleSetAllGoals());
        
        bottomPanel.add(addEditBtn);
        bottomPanel.add(setAllBtn);
        
        add(bottomPanel, BorderLayout.SOUTH);
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
    
    private void loadBudgets() {
        tableModel.setRowCount(0);
        
        Map<String, BudgetStatusInfo> statusMap = dataFacade.getBudgetStatusForMonth(currentMonth);
        
        if (statusMap.isEmpty()) {
            Set<String> categories = dataFacade.getCategories("Expenses");
            for (String category : categories) {
                tableModel.addRow(new Object[]{
                    category,
                    "-",
                    "0.00",
                    "-",
                    "-"
                });
            }
        } else {
            List<String> sortedCategories = new ArrayList<>(statusMap.keySet());
            Collections.sort(sortedCategories);
            
            for (String category : sortedCategories) {
                BudgetStatusInfo info = statusMap.get(category);
                
                String statusText;
                switch (info.status) {
                    case UNDER_BUDGET:
                        statusText = "Under Budget";
                        break;
                    case NEARING_LIMIT:
                        statusText = "Nearing Limit";
                        break;
                    case OVERSPENT:
                        statusText = "Overspent";
                        break;
                    default:
                        statusText = "-";
                }
                
                if (info.goal == 0) {
                    statusText = "-";
                }
                
                tableModel.addRow(new Object[]{
                    category,
                    info.goal > 0 ? String.format("%.2f", info.goal) : "-",
                    info.spent > 0 ? String.format("%.2f", info.spent) : "0.00",
                    info.goal > 0 ? String.format("%.2f", info.remaining) : "-",
                    statusText
                });
            }
        }
    }
    
    private void handleAddEditGoal() {
        BudgetGoalDialog dialog = new BudgetGoalDialog(parentFrame, currentMonth, 
                                                       buttonFactory, panelFactory);
        
        if (dialog.showDialog()) {
            PanelObserver.getInstance().notifyObservers();
        }
    }
    
    private void handleEditGoal(int row) {
        String category = (String) tableModel.getValueAt(row, 0);
        String goalStr = (String) tableModel.getValueAt(row, 1);
        
        double currentGoal = 0.0;
        if (!goalStr.equals("-")) {
            try {
                currentGoal = Double.parseDouble(goalStr);
            } catch (NumberFormatException ignored) {
            }
        }
        
        BudgetGoalDialog dialog = new BudgetGoalDialog(parentFrame, currentMonth, category, 
                                                       currentGoal, buttonFactory, panelFactory);
        
        if (dialog.showDialog()) {
            PanelObserver.getInstance().notifyObservers();
        }
    }
    
    private void handleSetAllGoals() {
        BudgetGoalDialog dialog = new BudgetGoalDialog(parentFrame, currentMonth, true, 
                                                       buttonFactory, panelFactory);
        
        if (dialog.showDialog()) {
            PanelObserver.getInstance().notifyObservers();
        }
    }
    
    @Override
    public void onThemeChanged() {
        // FIXED: Panel background switches between PRIMARY_GREEN (light) and BLACK (dark)
        boolean isDark = UITheme.isDarkMode();
        Color bgColor = isDark ? Color.BLACK : UITheme.PRIMARY_GREEN;
        
        setBackground(bgColor);
        topPanel.setBackground(bgColor);
        
        // FIXED: Title text color changes with theme
        titleLabel.setForeground(UITheme.TEXT_COLOR);
        
        // Update all labels in the top panel
        Component[] components = topPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                ((JLabel) comp).setForeground(UITheme.TEXT_COLOR);
            } else if (comp instanceof JPanel) {
                updatePanelLabels((JPanel) comp);
            }
        }
        
        // Update table header
        budgetTable.getTableHeader().setBackground(UITheme.BUTTON_BG);
        budgetTable.getTableHeader().setForeground(UITheme.BUTTON_TEXT);
        
        repaint();
    }
    
    private void updatePanelLabels(JPanel panel) {
        Component[] components = panel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                ((JLabel) comp).setForeground(UITheme.TEXT_COLOR);
            } else if (comp instanceof JPanel) {
                updatePanelLabels((JPanel) comp);
            }
        }
    }
    
    public void cleanup() {
        PanelObserver.getInstance().unregisterObserver(this);
        UITheme.removeThemeChangeListener(this);
    }
}
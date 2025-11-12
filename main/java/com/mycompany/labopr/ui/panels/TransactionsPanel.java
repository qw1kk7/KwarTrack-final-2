package com.mycompany.labopr.ui.panels;

import com.mycompany.labopr.data.DataFacade;
import com.mycompany.labopr.data.TransactionData.Transaction;
import com.mycompany.labopr.observer.PanelObserver;
import com.mycompany.labopr.observer.Refreshable;
import com.mycompany.labopr.ui.factories.ButtonFactory;
import com.mycompany.labopr.ui.factories.PanelFactory;
import com.mycompany.labopr.ui.factories.RoundedButtonFactory;
import com.mycompany.labopr.ui.dialogs.TransactionDialog;
import com.mycompany.labopr.ui.dialogs.EditTransactionDialog;
import com.mycompany.labopr.ui.theme.UITheme;
import com.mycompany.labopr.utils.TransactionCaretaker;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * TransactionsPanel - FIXED: Balance edit button always white with black text
 */
public class TransactionsPanel extends JPanel implements UITheme.ThemeChangeListener, Refreshable {
    private final DataFacade dataFacade;
    private final JFrame parentFrame;
    private final ButtonFactory buttonFactory;
    private final PanelFactory panelFactory;
    
    private JLabel balanceLabel;
    private JButton balanceEditBtn; // FIXED: Now uses fixed white button
    private JButton expensesBtn;
    private JButton incomeBtn;
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private String currentType = "Expenses";
    private JButton themeToggleBtn;
    
    private TransactionCaretaker caretaker = new TransactionCaretaker();
    private JButton undoBtn;
    private JButton redoBtn;
    
    private JButton editBtn;
    private JButton deleteBtn;
    private int selectedRow = -1;
    
    private static final Color BUTTON_SELECTED = Color.decode("#66a74b");
    private static final Color BUTTON_UNSELECTED = Color.WHITE;
    private static final Color BUTTON_HOVER = Color.decode("#cfffba");
    private static final Color TEXT_SELECTED = Color.WHITE;
    private static final Color TEXT_UNSELECTED = Color.decode("#333333");
    
    public TransactionsPanel(JFrame parent, DataFacade dataFacade,
                            ButtonFactory buttonFactory, PanelFactory panelFactory) {
        this.parentFrame = parent;
        this.dataFacade = dataFacade;
        this.buttonFactory = buttonFactory;
        this.panelFactory = panelFactory;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.PANEL_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        UITheme.addThemeChangeListener(this);
        PanelObserver.getInstance().registerObserver(this);
        
        checkAndSetBalance();
        
        initComponents();
        loadTransactions();
    }
    
    @Override
    public void refreshData() {
        SwingUtilities.invokeLater(() -> {
            loadTransactions();
        });
    }
    
    private void checkAndSetBalance() {
        Double balance = dataFacade.getBalance();
        if (balance == null) {
            String input = JOptionPane.showInputDialog(
                parentFrame,
                "Enter your starting balance (₱):",
                "Set Starting Balance",
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (input != null && !input.trim().isEmpty()) {
                try {
                    double startBalance = Double.parseDouble(input.trim());
                    if (startBalance >= 0) {
                        dataFacade.setBalance(startBalance);
                        PanelObserver.getInstance().notifyObservers();
                    } else {
                        JOptionPane.showMessageDialog(
                            parentFrame,
                            "Balance must be non-negative. Setting to 0.",
                            "Invalid Balance",
                            JOptionPane.WARNING_MESSAGE
                        );
                        dataFacade.setBalance(0);
                        PanelObserver.getInstance().notifyObservers();
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(
                        parentFrame,
                        "Invalid number format. Setting balance to 0.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                    dataFacade.setBalance(0);
                    PanelObserver.getInstance().notifyObservers();
                }
            } else {
                dataFacade.setBalance(0);
                PanelObserver.getInstance().notifyObservers();
            }
        }
    }
    
    private void initComponents() {
        JPanel topPanel = panelFactory.createPanel(new BorderLayout());
        topPanel.setBackground(UITheme.PANEL_BG);
        
        // Left side: Balance with fixed white button
        double currentBalance = dataFacade.calculateCurrentBalance();
        balanceLabel = new JLabel("Total Balance: ₱" + String.format("%,.2f", currentBalance));
        balanceLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 28));
        balanceLabel.setForeground(UITheme.TEXT_COLOR);
        balanceLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        
        // FIXED: Create fixed white button for balance edit
        RoundedButtonFactory factory = (RoundedButtonFactory) buttonFactory;
        balanceEditBtn = factory.createFixedWhiteButton("✍️️️", new Dimension(20, 20));
        balanceEditBtn.addActionListener(e -> handleEditBalance());
        
        JPanel leftPanel = panelFactory.createFlowPanel(FlowLayout.LEFT, 10, 0);
        leftPanel.add(balanceLabel);
        leftPanel.add(balanceEditBtn);
        topPanel.add(leftPanel, BorderLayout.WEST);
        
        // Right side: Theme toggle
        JPanel rightPanel = panelFactory.createFlowPanel(FlowLayout.RIGHT, 0, 0);
        themeToggleBtn = buttonFactory.createThemeToggleButton();
        themeToggleBtn.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.revalidate();
                window.repaint();
            }
        });
        rightPanel.add(themeToggleBtn);
        topPanel.add(rightPanel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Center: Toggle buttons and table
        JPanel centerPanel = panelFactory.createPanel(new BorderLayout(0, 10));
        centerPanel.setBackground(UITheme.PANEL_BG);
        
        // Toggle buttons with proper hover behavior
        JPanel togglePanel = panelFactory.createFlowPanel(FlowLayout.CENTER, 20, 10);
        
        expensesBtn = factory.createWhiteButton("Expenses", new Dimension(150, 45));
        incomeBtn = factory.createWhiteButton("Income", new Dimension(150, 45));
        
        addToggleButtonHoverEffect(expensesBtn);
        addToggleButtonHoverEffect(incomeBtn);
        
        expensesBtn.addActionListener(e -> switchView("Expenses"));
        incomeBtn.addActionListener(e -> switchView("Income"));
        
        togglePanel.add(expensesBtn);
        togglePanel.add(incomeBtn);
        
        centerPanel.add(togglePanel, BorderLayout.NORTH);
        
        // Transaction table
        String[] columnNames = {"Date", "Category", "Amount", "Comment"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        transactionTable = new JTable(tableModel);
        transactionTable.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 14));
        transactionTable.setRowHeight(30);
        transactionTable.getTableHeader().setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 14));
        
        updateTableHeaderColors();
        
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        transactionTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        
        transactionTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedRow = transactionTable.getSelectedRow();
                updateEditDeleteButtons();
            }
        });
        
        transactionTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && selectedRow != -1) {
                    handleEditTransaction();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBackground(Color.WHITE);
        
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel
        JPanel bottomPanel = panelFactory.createPanel(UITheme.PANEL_BG);
        bottomPanel.setLayout(new BorderLayout());
        
        // Left buttons
        JPanel leftButtonPanel = panelFactory.createFlowPanel(FlowLayout.LEFT, 10, 10);
        
        editBtn = buttonFactory.createButton("Edit", new Dimension(100, 50));
        editBtn.addActionListener(e -> handleEditTransaction());
        editBtn.setEnabled(false);
        
        deleteBtn = buttonFactory.createButton("Delete", new Dimension(100, 50));
        deleteBtn.addActionListener(e -> handleDeleteTransaction());
        deleteBtn.setEnabled(false);
        
        leftButtonPanel.add(editBtn);
        leftButtonPanel.add(deleteBtn);
        
        // Right buttons
        JPanel rightButtonPanel = panelFactory.createFlowPanel(FlowLayout.RIGHT, 10, 10);
        
        undoBtn = buttonFactory.createButton("Undo", new Dimension(100, 50));
        undoBtn.addActionListener(e -> handleUndo());
        undoBtn.setEnabled(false);
        
        redoBtn = buttonFactory.createButton("Redo", new Dimension(100, 50));
        redoBtn.addActionListener(e -> handleRedo());
        redoBtn.setEnabled(false);
        
        JButton addBtn = buttonFactory.createButton("Add", new Dimension(150, 50));
        addBtn.addActionListener(e -> handleAddTransaction());
        
        rightButtonPanel.add(undoBtn);
        rightButtonPanel.add(redoBtn);
        rightButtonPanel.add(addBtn);
        
        bottomPanel.add(leftButtonPanel, BorderLayout.WEST);
        bottomPanel.add(rightButtonPanel, BorderLayout.EAST);
        
        add(bottomPanel, BorderLayout.SOUTH);
        
        updateToggleButtons();
        updateUndoRedoButtons();
    }
    
    private void updateTableHeaderColors() {
        boolean isDark = UITheme.isDarkMode();
        
        if (isDark) {
            transactionTable.getTableHeader().setBackground(Color.BLACK);
            transactionTable.getTableHeader().setForeground(Color.WHITE);
        } else {
            transactionTable.getTableHeader().setBackground(Color.WHITE);
            transactionTable.getTableHeader().setForeground(Color.BLACK);
        }
    }
    
    private void addToggleButtonHoverEffect(JButton btn) {
        for (MouseAdapter adapter : btn.getListeners(MouseAdapter.class)) {
            btn.removeMouseListener(adapter);
        }
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if ((btn == expensesBtn && !currentType.equals("Expenses")) ||
                    (btn == incomeBtn && !currentType.equals("Income"))) {
                    btn.setBackground(BUTTON_HOVER);
                    btn.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn == expensesBtn) {
                    if (currentType.equals("Expenses")) {
                        btn.setBackground(BUTTON_SELECTED);
                        btn.setForeground(TEXT_SELECTED);
                    } else {
                        btn.setBackground(BUTTON_UNSELECTED);
                        btn.setForeground(TEXT_UNSELECTED);
                    }
                } else if (btn == incomeBtn) {
                    if (currentType.equals("Income")) {
                        btn.setBackground(BUTTON_SELECTED);
                        btn.setForeground(TEXT_SELECTED);
                    } else {
                        btn.setBackground(BUTTON_UNSELECTED);
                        btn.setForeground(TEXT_UNSELECTED);
                    }
                }
                btn.repaint();
            }
        });
    }
    
    private void updateToggleButtons() {
        if (currentType.equals("Expenses")) {
            expensesBtn.setBackground(BUTTON_SELECTED);
            expensesBtn.setForeground(TEXT_SELECTED);
            incomeBtn.setBackground(BUTTON_UNSELECTED);
            incomeBtn.setForeground(TEXT_UNSELECTED);
        } else {
            incomeBtn.setBackground(BUTTON_SELECTED);
            incomeBtn.setForeground(TEXT_SELECTED);
            expensesBtn.setBackground(BUTTON_UNSELECTED);
            expensesBtn.setForeground(TEXT_UNSELECTED);
        }
        
        expensesBtn.repaint();
        incomeBtn.repaint();
    }
    
    private void updateEditDeleteButtons() {
        boolean enabled = selectedRow != -1;
        editBtn.setEnabled(enabled);
        deleteBtn.setEnabled(enabled);
    }
    
    private void switchView(String type) {
        currentType = type;
        updateToggleButtons();
        loadTransactions();
    }
    
    private void loadTransactions() {
        tableModel.setRowCount(0);
        
        List<Transaction> transactions = dataFacade.getTransactionsByType(currentType);
        
        for (Transaction t : transactions) {
            tableModel.addRow(new Object[]{
                t.date,
                t.category,
                "₱" + String.format("%,.2f", t.amount),
                t.comment
            });
        }
        
        updateBalanceDisplay();
        updateUndoRedoButtons();
        
        transactionTable.clearSelection();
        selectedRow = -1;
        updateEditDeleteButtons();
    }
    
    private void updateBalanceDisplay() {
        double currentBalance = dataFacade.calculateCurrentBalance();
        balanceLabel.setText("Total Balance: ₱" + String.format("%,.2f", currentBalance));
    }
    
    private void handleEditBalance() {
        Double currentStartingBalance = dataFacade.getBalance();
        double currentBalance = dataFacade.calculateCurrentBalance();

        if (currentStartingBalance == null) currentStartingBalance = 0.0;
        
        String input = JOptionPane.showInputDialog(
            parentFrame,
            "Enter new starting balance (₱):\nCurrent: ₱" + String.format("%,.2f", currentBalance),
            "Edit Starting Balance",
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (input != null && !input.trim().isEmpty()) {
            try {
                double newBalance = Double.parseDouble(input.trim());
                if (newBalance >= 0) {
                    dataFacade.setBalance(newBalance);
                    PanelObserver.getInstance().notifyObservers();
                    
                    JOptionPane.showMessageDialog(
                        parentFrame,
                        "Starting balance updated successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    JOptionPane.showMessageDialog(
                        parentFrame,
                        "Balance must be non-negative.",
                        "Invalid Balance",
                        JOptionPane.WARNING_MESSAGE
                    );
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(
                    parentFrame,
                    "Invalid number format.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    private void handleAddTransaction() {
        caretaker.saveState();
        
        TransactionDialog dialog = new TransactionDialog(parentFrame, currentType, buttonFactory, panelFactory);
        
        if (dialog.showDialog()) {
            PanelObserver.getInstance().notifyObservers();
        } else {
            if (caretaker.canUndo()) {
                caretaker.undo();
            }
        }
        
        updateUndoRedoButtons();
    }
    
    private void handleEditTransaction() {
        if (selectedRow == -1) return;
        
        String originalDate = (String) tableModel.getValueAt(selectedRow, 0);
        String originalCategory = (String) tableModel.getValueAt(selectedRow, 1);
        String amountStr = ((String) tableModel.getValueAt(selectedRow, 2)).replace("₱", "").replace(",", "");
        double originalAmount = Double.parseDouble(amountStr);
        String originalComment = (String) tableModel.getValueAt(selectedRow, 3);
        
        caretaker.saveState();
        
        EditTransactionDialog dialog = new EditTransactionDialog(
            parentFrame, 
            currentType,
            originalDate,
            originalCategory,
            originalAmount,
            originalComment,
            buttonFactory,
            panelFactory
        );
        
        if (dialog.showDialog()) {
            Transaction updatedTransaction = dialog.getUpdatedTransaction();
            
            boolean success = dataFacade.updateTransaction(
                originalDate, 
                originalCategory, 
                originalAmount, 
                updatedTransaction
            );
            
            if (success) {
                PanelObserver.getInstance().notifyObservers();
                
                JOptionPane.showMessageDialog(
                    parentFrame,
                    "Transaction updated successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                if (caretaker.canUndo()) {
                    caretaker.undo();
                }
                JOptionPane.showMessageDialog(
                    parentFrame,
                    "Failed to update transaction.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        } else {
            if (caretaker.canUndo()) {
                caretaker.undo();
            }
        }
        
        updateUndoRedoButtons();
    }
    
    private void handleDeleteTransaction() {
        if (selectedRow == -1) return;
        
        String date = (String) tableModel.getValueAt(selectedRow, 0);
        String category = (String) tableModel.getValueAt(selectedRow, 1);
        String amountStr = ((String) tableModel.getValueAt(selectedRow, 2)).replace("₱", "").replace(",", "");
        double amount = Double.parseDouble(amountStr);
        
        int confirm = JOptionPane.showConfirmDialog(
            parentFrame,
            "Are you sure you want to delete this transaction?\n\n" +
            "Date: " + date + "\n" +
            "Category: " + category + "\n" +
            "Amount: ₱" + String.format("%,.2f", amount),
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            caretaker.saveState();
            
            boolean success = dataFacade.deleteTransaction(date, category, amount);
            
            if (success) {
                PanelObserver.getInstance().notifyObservers();
                
                JOptionPane.showMessageDialog(
                    parentFrame,
                    "Transaction deleted successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                if (caretaker.canUndo()) {
                    caretaker.undo();
                }
                JOptionPane.showMessageDialog(
                    parentFrame,
                    "Failed to delete transaction.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
            
            updateUndoRedoButtons();
        }
    }
    
    private void handleUndo() {
        if (caretaker.undo()) {
            PanelObserver.getInstance().notifyObservers();
            
            JOptionPane.showMessageDialog(
                parentFrame,
                "Transaction undone successfully!",
                "Undo",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
    
    private void handleRedo() {
        if (caretaker.redo()) {
            PanelObserver.getInstance().notifyObservers();
            
            JOptionPane.showMessageDialog(
                parentFrame,
                "Transaction redone successfully!",
                "Redo",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
    
    private void updateUndoRedoButtons() {
        undoBtn.setEnabled(caretaker.canUndo());
        redoBtn.setEnabled(caretaker.canRedo());
    }
    
    @Override
    public void onThemeChanged() {
        setBackground(UITheme.PANEL_BG);
        balanceLabel.setForeground(UITheme.TEXT_COLOR);
        
        // Update all panel backgrounds
        Component[] components = getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                updatePanelBackground((JPanel) comp);
            }
        }
        
        // Update table header colors
        updateTableHeaderColors();
        
        // FIXED: Balance button maintains white background and black text
        // (No theme update needed - it's a fixed-style button)
        
        repaint();
    }
    
    private void updatePanelBackground(JPanel panel) {
        panel.setBackground(UITheme.PANEL_BG);
        Component[] components = panel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                updatePanelBackground((JPanel) comp);
            }
        }
    }
    
    public void cleanup() {
        PanelObserver.getInstance().unregisterObserver(this);
        UITheme.removeThemeChangeListener(this);
    }
}
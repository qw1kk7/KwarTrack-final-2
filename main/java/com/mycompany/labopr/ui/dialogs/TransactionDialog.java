package com.mycompany.labopr.ui.dialogs;

import com.mycompany.labopr.data.TransactionData;
import com.mycompany.labopr.ui.builders.DialogBuilder;
import com.mycompany.labopr.ui.factories.ButtonFactory;
import com.mycompany.labopr.ui.factories.PanelFactory;
import com.mycompany.labopr.ui.theme.UITheme;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

public class TransactionDialog {
    // Maximum value for DECIMAL(15,2): 13 integer digits + 2 decimal digits
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("9999999999999.99");
    
    private final JFrame parent;
    private final String transactionType;
    private final ButtonFactory buttonFactory;
    private final PanelFactory panelFactory;
    
    private JTextField amountField;
    private JComboBox<String> categoryCombo;
    private JTextField dateField;
    private JTextArea commentArea;
    private boolean confirmed = false;
    
    public TransactionDialog(JFrame parent, String type, 
                            ButtonFactory buttonFactory, PanelFactory panelFactory) {
        this.parent = parent;
        this.transactionType = type;
        this.buttonFactory = buttonFactory;
        this.panelFactory = panelFactory;
    }
    
    public boolean showDialog() {
        JPanel contentPanel = buildContentPanel();
        
        JButton addBtn = buttonFactory.createButton("Add", new Dimension(120, 40));
        JButton cancelBtn = buttonFactory.createButton("Cancel", new Dimension(120, 40));
        
        JDialog dialog = new DialogBuilder(parent, "Add " + transactionType, 
                                          buttonFactory, panelFactory)
            .size(450, 450)
            .modal(true)
            .content(contentPanel)
            .buttons(cancelBtn, addBtn)
            .theme(UITheme.LIGHTER_PRIMARY_GREEN)
            .build();
        
        setupActions(dialog, addBtn, cancelBtn);
        
        dialog.setVisible(true);
        return confirmed;
    }
    
    private JPanel buildContentPanel() {
        JPanel contentPanel = DialogBuilder.createStyledPanel(panelFactory);
        
        amountField = new JTextField(20);
        contentPanel.add(DialogBuilder.createFieldRow(panelFactory, "Amount (₱):", amountField));
        contentPanel.add(Box.createVerticalStrut(15));
        
        categoryCombo = createCategoryComboBox();
        contentPanel.add(DialogBuilder.createFieldRow(panelFactory, "Category:", categoryCombo));
        contentPanel.add(Box.createVerticalStrut(15));
        
        dateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()), 20);
        contentPanel.add(DialogBuilder.createFieldRow(panelFactory, "Date (YYYY-MM-DD):", dateField));
        contentPanel.add(Box.createVerticalStrut(15));
        
        JLabel commentLabel = new JLabel("Comment (optional):");
        commentLabel.setForeground(UITheme.TEXT_COLOR);
        commentLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 14));
        commentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(commentLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        
        commentArea = new JTextArea(4, 20);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(commentArea);
        scrollPane.setMaximumSize(new Dimension(350, 100));
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(scrollPane);
        
        return contentPanel;
    }
    
    private JComboBox<String> createCategoryComboBox() {
        Set<String> categories = TransactionData.getCategories(transactionType);
        String[] categoryArray = categories.toArray(new String[0]);
        Arrays.sort(categoryArray);
        
        String[] categoriesWithCreate = new String[categoryArray.length + 1];
        System.arraycopy(categoryArray, 0, categoriesWithCreate, 0, categoryArray.length);
        categoriesWithCreate[categoryArray.length] = "➕ Create Category";
        
        JComboBox<String> combo = new JComboBox<>(categoriesWithCreate);
        combo.setMaximumSize(new Dimension(350, 30));
        combo.addActionListener(e -> handleCategorySelection(combo));
        
        return combo;
    }
    
    private void setupActions(JDialog dialog, JButton addBtn, JButton cancelBtn) {
        addBtn.addActionListener(e -> handleAdd(dialog));
        cancelBtn.addActionListener(e -> dialog.dispose());
    }
    
    private void handleCategorySelection(JComboBox<String> combo) {
        if (combo.getSelectedItem().toString().equals("➕ Create Category")) {
            String newCategory = JOptionPane.showInputDialog(
                parent,
                "Enter new category name:",
                "Create Category",
                JOptionPane.PLAIN_MESSAGE
            );
            
            if (newCategory != null && !newCategory.trim().isEmpty()) {
                newCategory = newCategory.trim();
                TransactionData.addCustomCategory(transactionType, newCategory);
                
                Set<String> categories = TransactionData.getCategories(transactionType);
                String[] categoryArray = categories.toArray(new String[0]);
                Arrays.sort(categoryArray);
                
                String[] categoriesWithCreate = new String[categoryArray.length + 1];
                System.arraycopy(categoryArray, 0, categoriesWithCreate, 0, categoryArray.length);
                categoriesWithCreate[categoryArray.length] = "➕ Create Category";
                
                combo.setModel(new DefaultComboBoxModel<>(categoriesWithCreate));
                combo.setSelectedItem(newCategory);
            } else {
                combo.setSelectedIndex(0);
            }
        }
    }
    
    private void handleAdd(JDialog dialog) {
        // Validate amount
        String amountText = amountField.getText().trim();
        if (amountText.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Please enter an amount.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        BigDecimal amount;
        try {
            amount = new BigDecimal(amountText);
            
            // Check if amount is positive
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(dialog, "Amount must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // VALIDATION: Check if amount exceeds database maximum
            if (amount.compareTo(MAX_AMOUNT) > 0) {
                JOptionPane.showMessageDialog(
                    dialog,
                    "The amount entered exceeds the maximum allowed value (₱9,999,999,999,999.99) and cannot be saved.\n\n" +
                    "Please enter a smaller amount.",
                    "Amount Too Large",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(dialog, "Invalid amount format.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Validate category
        String category = (String) categoryCombo.getSelectedItem();
        if (category == null || category.equals("➕ Create Category")) {
            JOptionPane.showMessageDialog(dialog, "Please select a category.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Validate date
        String date = dateField.getText().trim();
        if (date.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Please enter a date.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(dialog, "Invalid date format. Use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String comment = commentArea.getText().trim();
        
        TransactionData.Transaction transaction = new TransactionData.Transaction(
            transactionType, date, category, amount.doubleValue(), comment
        );
        TransactionData.saveTransaction(transaction);
        
        confirmed = true;
        dialog.dispose();
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
}

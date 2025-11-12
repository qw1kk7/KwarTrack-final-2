package com.mycompany.labopr.ui.dialogs;

import com.mycompany.labopr.data.TransactionData;
import com.mycompany.labopr.ui.builders.DialogBuilder;
import com.mycompany.labopr.ui.factories.ButtonFactory;
import com.mycompany.labopr.ui.factories.PanelFactory;
import com.mycompany.labopr.ui.theme.UITheme;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Set;

/**
 * Refactored EditTransactionDialog using DialogBuilder, ButtonFactory, and PanelFactory
 * Ensures consistency with TransactionDialog structure and UI
 */
public class EditTransactionDialog {
    private final JFrame parent;
    private final String transactionType;
    private final String originalDate;
    private final String originalCategory;
    private final double originalAmount;
    private final String originalComment;
    private final ButtonFactory buttonFactory;
    private final PanelFactory panelFactory;

    private JTextField amountField;
    private JComboBox<String> categoryCombo;
    private JTextField dateField;
    private JTextArea commentArea;
    private boolean confirmed = false;
    private TransactionData.Transaction updatedTransaction;

    public EditTransactionDialog(JFrame parent, String type, String originalDate,
                                 String originalCategory, double originalAmount,
                                 String originalComment, ButtonFactory buttonFactory,
                                 PanelFactory panelFactory) {
        this.parent = parent;
        this.transactionType = type;
        this.originalDate = originalDate;
        this.originalCategory = originalCategory;
        this.originalAmount = originalAmount;
        this.originalComment = originalComment;
        this.buttonFactory = buttonFactory;
        this.panelFactory = panelFactory;
    }

    public boolean showDialog() {
        JPanel contentPanel = buildContentPanel();

        JButton saveBtn = buttonFactory.createButton("Save", new Dimension(120, 40));
        JButton cancelBtn = buttonFactory.createButton("Cancel", new Dimension(120, 40));

        JDialog dialog = new DialogBuilder(parent, "Edit " + transactionType,
                buttonFactory, panelFactory)
                .size(450, 450)
                .modal(true)
                .content(contentPanel)
                .buttons(cancelBtn, saveBtn)
                .theme(UITheme.LIGHTER_PRIMARY_GREEN)
                .build();

        setupActions(dialog, saveBtn, cancelBtn);
        dialog.setVisible(true);
        return confirmed;
    }

    private JPanel buildContentPanel() {
        JPanel contentPanel = DialogBuilder.createStyledPanel(panelFactory);

        // Amount
        amountField = new JTextField(String.valueOf(originalAmount), 20);
        contentPanel.add(DialogBuilder.createFieldRow(panelFactory, "Amount (₱):", amountField));
        contentPanel.add(Box.createVerticalStrut(15));

        // Category
        categoryCombo = createCategoryComboBox();
        contentPanel.add(DialogBuilder.createFieldRow(panelFactory, "Category:", categoryCombo));
        contentPanel.add(Box.createVerticalStrut(15));

        // Date
        dateField = new JTextField(originalDate, 20);
        contentPanel.add(DialogBuilder.createFieldRow(panelFactory, "Date (YYYY-MM-DD):", dateField));
        contentPanel.add(Box.createVerticalStrut(15));

        // Comment
        JLabel commentLabel = new JLabel("Comment (optional):");
        commentLabel.setForeground(UITheme.TEXT_COLOR);
        commentLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 14));
        commentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(commentLabel);
        contentPanel.add(Box.createVerticalStrut(5));

        commentArea = new JTextArea(originalComment, 4, 20);
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
        combo.setSelectedItem(originalCategory);
        combo.addActionListener(e -> handleCategorySelection(combo));

        return combo;
    }

    private void setupActions(JDialog dialog, JButton saveBtn, JButton cancelBtn) {
        saveBtn.addActionListener(e -> handleSave(dialog));
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
                combo.setSelectedItem(originalCategory);
            }
        }
    }

    private void handleSave(JDialog dialog) {
        // Validate amount
        String amountText = amountField.getText().trim();
        if (amountText.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Please enter an amount.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                JOptionPane.showMessageDialog(dialog, "Amount must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
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

        updatedTransaction = new TransactionData.Transaction(
                transactionType, date, category, amount, comment
        );

        confirmed = true;
        dialog.dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public TransactionData.Transaction getUpdatedTransaction() {
        return updatedTransaction;
    }
}
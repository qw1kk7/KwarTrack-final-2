package com.mycompany.labopr.ui.dialogs;

import com.mycompany.labopr.data.BudgetData;
import com.mycompany.labopr.data.TransactionData;
import com.mycompany.labopr.ui.builders.DialogBuilder;
import com.mycompany.labopr.ui.factories.ButtonFactory;
import com.mycompany.labopr.ui.factories.PanelFactory;
import com.mycompany.labopr.ui.theme.UITheme;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Refactored BudgetGoalDialog using DialogBuilder, ButtonFactory, and PanelFactory.
 * Fixed: ScrollPane support for "Set All" dialog (preferred size, viewport background).
 */
public class BudgetGoalDialog {
    private final JFrame parent;
    private final String selectedMonth;
    private final String preselectedCategory;
    private final double currentGoal;
    private final boolean isSetAllMode;
    private final ButtonFactory buttonFactory;
    private final PanelFactory panelFactory;

    private JComboBox<String> categoryCombo;
    private JTextField goalField;
    private Map<String, JTextField> categoryFields;
    private boolean confirmed = false;

    // --- Constructors ---
    public BudgetGoalDialog(JFrame parent, String month, ButtonFactory buttonFactory, PanelFactory panelFactory) {
        this(parent, month, null, 0, false, buttonFactory, panelFactory);
    }

    public BudgetGoalDialog(JFrame parent, String month, String category, double currentGoal,
                            ButtonFactory buttonFactory, PanelFactory panelFactory) {
        this(parent, month, category, currentGoal, false, buttonFactory, panelFactory);
    }

    public BudgetGoalDialog(JFrame parent, String month, boolean setAllMode,
                            ButtonFactory buttonFactory, PanelFactory panelFactory) {
        this(parent, month, null, 0, setAllMode, buttonFactory, panelFactory);
    }

    private BudgetGoalDialog(JFrame parent, String month, String category, double currentGoal, boolean setAllMode,
                             ButtonFactory buttonFactory, PanelFactory panelFactory) {
        this.parent = parent;
        this.selectedMonth = month;
        this.preselectedCategory = category;
        this.currentGoal = currentGoal;
        this.isSetAllMode = setAllMode;
        this.buttonFactory = buttonFactory;
        this.panelFactory = panelFactory;
    }

    // --- Public Dialog Entry ---
    public boolean showDialog() {
        return isSetAllMode ? showSetAllDialog() : showSingleGoalDialog();
    }

    // --- Single Goal Dialog ---
    private boolean showSingleGoalDialog() {
        JPanel contentPanel = createSingleGoalContentPanel();

        JButton cancelBtn = buttonFactory.createButton("Cancel", new Dimension(100, 40));
        JButton saveBtn = buttonFactory.createButton("Save", new Dimension(100, 40));

        cancelBtn.addActionListener(e -> SwingUtilities.getWindowAncestor(cancelBtn).dispose());
        saveBtn.addActionListener(e -> handleSaveSingle((JDialog) SwingUtilities.getWindowAncestor(saveBtn)));

        new DialogBuilder(parent, preselectedCategory == null ? "Add Budget Goal" : "Edit Budget Goal",
                buttonFactory, panelFactory)
            .size(450, 300)
            .content(contentPanel)
            .buttons(cancelBtn, saveBtn)
            .theme(UITheme.LIGHTER_PRIMARY_GREEN)
            .show();

        return confirmed;
    }

    private JPanel createSingleGoalContentPanel() {
        JPanel contentPanel = DialogBuilder.createStyledPanel(panelFactory);
        JLabel monthLabel = new JLabel("Month: " + selectedMonth);
        monthLabel.setForeground(UITheme.TEXT_COLOR);
        monthLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 16));
        monthLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentPanel.add(monthLabel);
        contentPanel.add(Box.createVerticalStrut(20));

        // Category combo
        Set<String> categories = TransactionData.getCategories("Expenses");
        String[] categoryArray = categories.toArray(new String[0]);
        Arrays.sort(categoryArray);
        categoryCombo = new JComboBox<>(categoryArray);
        categoryCombo.setMaximumSize(new Dimension(350, 30));

        if (preselectedCategory != null) {
            categoryCombo.setSelectedItem(preselectedCategory);
            categoryCombo.setEnabled(false);
        }

        contentPanel.add(DialogBuilder.createFieldRow(panelFactory, "Category:", categoryCombo));
        contentPanel.add(Box.createVerticalStrut(15));

        // Goal field
        goalField = new JTextField(currentGoal > 0 ? String.valueOf(currentGoal) : "", 20);
        contentPanel.add(DialogBuilder.createFieldRow(panelFactory, "Goal Amount (₱):", goalField));

        return contentPanel;
    }

    // --- Set All Dialog ---
    private boolean showSetAllDialog() {
        // ensure map initialized before building content
        this.categoryFields = new LinkedHashMap<>(); // preserve order
        JLabel monthLabel = new JLabel("Set Goals for Month: " + selectedMonth);
        monthLabel.setForeground(Color.BLACK);
        monthLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 18));

        JPanel northPanel = panelFactory.createFlowPanel(FlowLayout.LEFT, 0, 0);
        northPanel.setBackground(UITheme.LIGHTER_PRIMARY_GREEN);
        northPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        northPanel.add(monthLabel);

        JPanel contentPanel = createSetAllContentPanel();

        JButton cancelBtn = buttonFactory.createButton("Cancel", new Dimension(120, 40));
        JButton saveBtn = buttonFactory.createButton("Save All", new Dimension(120, 40));

        cancelBtn.addActionListener(e -> SwingUtilities.getWindowAncestor(cancelBtn).dispose());
        saveBtn.addActionListener(e -> handleSaveAll((JDialog) SwingUtilities.getWindowAncestor(saveBtn)));

        new DialogBuilder(parent, "Set All Budget Goals", buttonFactory, panelFactory)
            .size(520, 620)
            .north(northPanel)
            .content(contentPanel)
            .buttons(cancelBtn, saveBtn)
            .theme(UITheme.LIGHTER_PRIMARY_GREEN)
            .show();

        return confirmed;
    }

    private JPanel createSetAllContentPanel() {
        // The categoriesPanel contains rows; we'll place it inside a JScrollPane with preferred size.
        JPanel categoriesPanel = panelFactory.createPanel();
        categoriesPanel.setLayout(new BoxLayout(categoriesPanel, BoxLayout.Y_AXIS));
        categoriesPanel.setOpaque(false);
        categoriesPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        Set<String> categories = TransactionData.getCategories("Expenses");
        List<String> sortedCategories = new ArrayList<>(categories);
        Collections.sort(sortedCategories);
        Map<String, Double> existingGoals = BudgetData.getBudgetGoalsForMonth(selectedMonth);

        for (String category : sortedCategories) {
            JPanel rowPanel = panelFactory.createPanel(new BorderLayout(10, 0));
            rowPanel.setOpaque(false);
            rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

            JLabel categoryLabel = new JLabel(category);
            categoryLabel.setForeground(UITheme.TEXT_COLOR);
            categoryLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 14));
            categoryLabel.setPreferredSize(new Dimension(200, 30));

            JTextField goalField = new JTextField(10);
            goalField.setPreferredSize(new Dimension(150, 30));

            if (existingGoals.containsKey(category)) {
                goalField.setText(String.valueOf(existingGoals.get(category)));
            }

            categoryFields.put(category, goalField);

            JPanel fieldWrapper = panelFactory.createFlowPanel(FlowLayout.LEFT, 0, 0);
            fieldWrapper.setOpaque(false);
            JLabel pesoLabel = new JLabel("₱ ");
            pesoLabel.setForeground(UITheme.TEXT_COLOR);
            pesoLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 14));
            fieldWrapper.add(pesoLabel);
            fieldWrapper.add(goalField);

            rowPanel.add(categoryLabel, BorderLayout.WEST);
            rowPanel.add(fieldWrapper, BorderLayout.CENTER);

            categoriesPanel.add(rowPanel);
            categoriesPanel.add(Box.createVerticalStrut(10));
        }

        // Put categoriesPanel into a JScrollPane and ensure viewport background & preferred size.
        JScrollPane scrollPane = new JScrollPane(categoriesPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(UITheme.LIGHTER_PRIMARY_GREEN);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setPreferredSize(new Dimension(480, 420)); // ensures scroll appears inside dialog

        // Wrap scrollPane in a border panel to match other dialogs' look
        JPanel wrapperPanel = panelFactory.createBorderPanel();
        wrapperPanel.setBackground(UITheme.LIGHTER_PRIMARY_GREEN);
        wrapperPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        wrapperPanel.add(scrollPane, BorderLayout.CENTER);

        return wrapperPanel;
    }

    // --- Save Handlers ---
    private void handleSaveSingle(JDialog dialog) {
        String category = (String) categoryCombo.getSelectedItem();
        if (category == null || category.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Please select a category.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String goalText = goalField.getText().trim();
        if (goalText.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Please enter a goal amount.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double goal = Double.parseDouble(goalText);
            if (goal < 0) {
                JOptionPane.showMessageDialog(dialog, "Goal must be non-negative.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            BudgetData.saveBudgetGoal(new BudgetData.BudgetGoal(category, selectedMonth, goal));
            confirmed = true;
            dialog.dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(dialog, "Invalid amount format.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSaveAll(JDialog dialog) {
        List<BudgetData.BudgetGoal> goals = new ArrayList<>();

        for (Map.Entry<String, JTextField> entry : categoryFields.entrySet()) {
            String category = entry.getKey();
            String goalText = entry.getValue().getText().trim();
            if (!goalText.isEmpty()) {
                try {
                    double goal = Double.parseDouble(goalText);
                    if (goal >= 0) {
                        goals.add(new BudgetData.BudgetGoal(category, selectedMonth, goal));
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(dialog, "Invalid amount for category: " + category,
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        if (goals.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Please enter at least one goal.",
                    "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BudgetData.saveBudgetGoals(goals);
        confirmed = true;
        dialog.dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
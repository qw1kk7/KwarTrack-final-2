package com.mycompany.labopr.ui.builders;

import com.mycompany.labopr.ui.factories.ButtonFactory;
import com.mycompany.labopr.ui.factories.PanelFactory;
import com.mycompany.labopr.ui.theme.UITheme;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder Pattern: Enhanced fluent interface for dialog construction
 * Refactored to use ButtonFactory and PanelFactory
 */
public class DialogBuilder {

    // --- Required ---
    private final JFrame parent;
    private final String title;
    private final ButtonFactory buttonFactory;
    private final PanelFactory panelFactory;

    // --- Optional with defaults ---
    private int width = 400;
    private int height = 300;
    private boolean modal = true;
    private LayoutManager layout = new BorderLayout(10, 10);
    private Color themeColor = UITheme.LIGHTER_PRIMARY_GREEN;
    private ImageIcon icon = null;
    private boolean resizable = false;

    // --- Components ---
    private JPanel contentPanel;
    private final List<JButton> buttons = new ArrayList<>();
    private JPanel customNorthPanel;
    private JPanel customSouthPanel;

    // --- Constructor ---
    public DialogBuilder(JFrame parent, String title, ButtonFactory buttonFactory, PanelFactory panelFactory) {
        if (parent == null || title == null) {
            throw new IllegalArgumentException("Parent and title cannot be null.");
        }
        this.parent = parent;
        this.title = title;
        this.buttonFactory = buttonFactory;
        this.panelFactory = panelFactory;
    }

    // --- Configuration methods ---
    public DialogBuilder size(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive.");
        }
        this.width = width;
        this.height = height;
        return this;
    }

    public DialogBuilder modal(boolean modal) {
        this.modal = modal;
        return this;
    }

    public DialogBuilder layout(LayoutManager layout) {
        this.layout = layout;
        return this;
    }

    public DialogBuilder resizable(boolean resizable) {
        this.resizable = resizable;
        return this;
    }

    public DialogBuilder theme(Color themeColor) {
        this.themeColor = themeColor != null ? themeColor : UITheme.LIGHTER_PRIMARY_GREEN;
        return this;
    }

    public DialogBuilder icon(ImageIcon icon) {
        this.icon = icon;
        return this;
    }

    public DialogBuilder content(JPanel panel) {
        this.contentPanel = panel;
        return this;
    }

    public DialogBuilder buttons(JButton... buttons) {
        for (JButton btn : buttons) {
            if (btn != null) this.buttons.add(btn);
        }
        return this;
    }

    public DialogBuilder north(JPanel panel) {
        this.customNorthPanel = panel;
        return this;
    }

    public DialogBuilder south(JPanel panel) {
        this.customSouthPanel = panel;
        return this;
    }

    // --- Builder Output ---
    public JDialog build() {
        JDialog dialog = new JDialog(parent, title, modal);
        dialog.setSize(width, height);
        dialog.setLocationRelativeTo(parent);
        dialog.setResizable(resizable);
        dialog.setLayout(layout);

        if (icon != null) dialog.setIconImage(icon.getImage());
        if (customNorthPanel != null) dialog.add(customNorthPanel, BorderLayout.NORTH);
        if (contentPanel != null) dialog.add(contentPanel, BorderLayout.CENTER);

        if (customSouthPanel != null) {
            dialog.add(customSouthPanel, BorderLayout.SOUTH);
        } else if (!buttons.isEmpty()) {
            dialog.add(createButtonPanel(), BorderLayout.SOUTH);
        }

        return dialog;
    }

    /**
     * Builds and shows the dialog immediately.
     */
    public JDialog show() {
        JDialog dialog = build();
        dialog.setVisible(true);
        return dialog;
    }

    // --- Utility Components ---
    private JPanel createButtonPanel() {
        JPanel buttonPanel = panelFactory.createFlowPanel(FlowLayout.RIGHT, 10, 10);
        buttonPanel.setBackground(themeColor);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        for (JButton button : buttons) {
            buttonPanel.add(button);
        }

        return buttonPanel;
    }

    // --- Static Factory Methods ---
    public static JPanel createStyledPanel(PanelFactory panelFactory) {
        return createStyledPanel(panelFactory, UITheme.LIGHTER_PRIMARY_GREEN);
    }

    public static JPanel createStyledPanel(PanelFactory panelFactory, Color bgColor) {
        JPanel panel = panelFactory.createPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(bgColor != null ? bgColor : UITheme.LIGHTER_PRIMARY_GREEN);
        return panel;
    }

    public static JPanel createFieldRow(PanelFactory panelFactory, String labelText, JComponent component) {
        return createFieldRow(panelFactory, labelText, component, 350, 30);
    }

    public static JPanel createFieldRow(PanelFactory panelFactory, String labelText, JComponent component, int maxWidth, int height) {
        JPanel panel = panelFactory.createPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setForeground(UITheme.TEXT_COLOR);
        label.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 14));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        component.setMaximumSize(new Dimension(maxWidth, height));
        component.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(5));
        panel.add(component);

        return panel;
    }
}
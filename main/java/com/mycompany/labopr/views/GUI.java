package com.mycompany.labopr.views;

import com.mycompany.labopr.ui.dialogs.AuthDialogs;
import com.mycompany.labopr.ui.factories.ButtonFactory;
import com.mycompany.labopr.ui.factories.PanelFactory;
import com.mycompany.labopr.ui.factories.RoundedButtonFactory;
import com.mycompany.labopr.ui.factories.StandardPanelFactory;
import com.mycompany.labopr.ui.theme.UITheme;
import javax.swing.*;
import java.awt.*;

/**
 * GUI - Factory Method Pattern Refactored
 * Main menu screen using ButtonFactory and PanelFactory
 */
public class GUI extends JFrame implements UITheme.ThemeChangeListener {

    private final ButtonFactory buttonFactory = new RoundedButtonFactory();
    private final PanelFactory panelFactory = new StandardPanelFactory();
    
    private JPanel logoPanel;
    private JPanel contentPanel;
    private JPanel buttonPanel;
    private JLabel title;
    private JLabel subtitle;

    public GUI() {
        setTitle("Main Menu");
        setSize(1300, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        UITheme.addThemeChangeListener(this);

        initializeComponents();
        updateTheme();

        setVisible(true);
    }

    private void initializeComponents() {
        // Top panel with dark mode toggle
        JPanel topPanel = panelFactory.createPanel(new BorderLayout());
        topPanel.setOpaque(false);
        
        JPanel darkModePanel = panelFactory.createFlowPanel(FlowLayout.RIGHT, 0, 0);
        darkModePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        darkModePanel.add(buttonFactory.createThemeToggleButton());
        topPanel.add(darkModePanel, BorderLayout.NORTH);

        // Logo panel
        logoPanel = panelFactory.createPanel();
        logoPanel.setBorder(BorderFactory.createEmptyBorder(UITheme.PANEL_PADDING_TOP, 0, 20, 0));
        
        JLabel logoLabel = new JLabel();
        ImageIcon icon = UITheme.getCachedLogo("/KLOGO.png", 300, 300);
        if (icon != null) {
            logoLabel.setIcon(icon);
        } else {
            logoLabel.setText("KwarTrack");
            logoLabel.setForeground(UITheme.TEXT_COLOR);
            logoLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, UITheme.FONT_LARGE));
        }
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoPanel.add(logoLabel);
        
        topPanel.add(logoPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Middle content panel
        contentPanel = panelFactory.createPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        contentPanel.add(Box.createVerticalGlue());
        
        title = new JLabel("Welcome to KwarTrack!", SwingConstants.CENTER);
        title.setForeground(UITheme.TEXT_COLOR);
        title.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, UITheme.FONT_LARGE));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        subtitle = new JLabel("Your personal finance tracker and manager", SwingConstants.CENTER);
        subtitle.setForeground(UITheme.TEXT_COLOR);
        subtitle.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, UITheme.FONT_MEDIUM));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(title);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        contentPanel.add(subtitle);
        contentPanel.add(Box.createVerticalGlue());

        add(contentPanel, BorderLayout.CENTER);

        // Bottom button panel
        buttonPanel = panelFactory.createPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(
            UITheme.PANEL_PADDING_TOP, 0, UITheme.PANEL_PADDING_BOTTOM, 0));

        JButton loginBtn = buttonFactory.createButton("Login");
        loginBtn.addActionListener(e -> AuthDialogs.handleLogin(this, buttonFactory, panelFactory));
        
        JButton signUpBtn = buttonFactory.createButton("Sign Up");
        signUpBtn.addActionListener(e -> AuthDialogs.handleSignUp(this, buttonFactory, panelFactory));

        JButton[] buttons = {loginBtn, signUpBtn};
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setAlignmentX(Component.CENTER_ALIGNMENT);
            buttonPanel.add(buttons[i]);
            if (i < buttons.length - 1) buttonPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        }

        add(buttonPanel, BorderLayout.SOUTH);
    }

    @Override
    public void onThemeChanged() {
        updateTheme();
    }

    private void updateTheme() {
        getContentPane().setBackground(UITheme.PRIMARY_GREEN);
        logoPanel.setBackground(UITheme.PRIMARY_GREEN);
        title.setForeground(UITheme.TEXT_COLOR);
        subtitle.setForeground(UITheme.TEXT_COLOR);
        repaint();
    }

    @Override
    public void dispose() {
        UITheme.removeThemeChangeListener(this);
        super.dispose();
    }
}
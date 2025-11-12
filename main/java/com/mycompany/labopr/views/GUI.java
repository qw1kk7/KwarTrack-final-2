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
 * GUI - Factory Method Pattern with KBACKGROUND support
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
    private Image backgroundImage;
    private JPanel mainPanel;

    public GUI() {
        setTitle("KwarTrack - Landing Page");
        setSize(1300, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        ImageIcon icon = UITheme.getCachedLogo("/KLOGO.png", 64, 64);
        if (icon != null) {
            setIconImage(icon.getImage());
        }

        UITheme.addThemeChangeListener(this);
        loadBackgroundImage();
        initializeComponents();
        updateTheme();

        setVisible(true);
    }

    private void loadBackgroundImage() {
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/KBACKGROUND.png"));
            backgroundImage = icon.getImage();
            System.out.println("Background image loaded successfully");
        } catch (Exception e) {
            System.err.println("Error loading background image: " + e.getMessage());
            backgroundImage = null;
        }
    }

    private void initializeComponents() {
        // Main panel with background
        mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                
                // Fill with PRIMARY_GREEN
                g2d.setColor(UITheme.PRIMARY_GREEN);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Draw background image if available
                if (backgroundImage != null) {
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        setContentPane(mainPanel);

        // Top panel with dark mode toggle
        JPanel topPanel = panelFactory.createPanel(new BorderLayout());
        topPanel.setOpaque(false);
        
        JPanel darkModePanel = panelFactory.createFlowPanel(FlowLayout.RIGHT, 0, 0);
        darkModePanel.setOpaque(false);
        darkModePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        darkModePanel.add(buttonFactory.createThemeToggleButton());
        topPanel.add(darkModePanel, BorderLayout.NORTH);

        // Logo panel
        logoPanel = panelFactory.createPanel();
        logoPanel.setOpaque(false);
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
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Middle content panel
        contentPanel = panelFactory.createPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        contentPanel.add(Box.createVerticalGlue());
        
        title = new JLabel("Welcome to KwarTrack!", SwingConstants.CENTER);
        title.setForeground(UITheme.TEXT_COLOR);
        title.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, UITheme.FONT_LARGE));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        subtitle = new JLabel("Your Personal Finance Tracker and Manager!", SwingConstants.CENTER);
        subtitle.setForeground(UITheme.TEXT_COLOR);
        subtitle.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, UITheme.FONT_MEDIUM));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(title);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        contentPanel.add(subtitle);
        contentPanel.add(Box.createVerticalGlue());

        mainPanel.add(contentPanel, BorderLayout.CENTER);

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

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    @Override
    public void onThemeChanged() {
        updateTheme();
    }

    private void updateTheme() {
        if (mainPanel != null) {
            mainPanel.repaint();
        }
        if (title != null) {
            title.setForeground(UITheme.TEXT_COLOR);
        }
        if (subtitle != null) {
            subtitle.setForeground(UITheme.TEXT_COLOR);
        }
        repaint();
    }

    @Override
    public void dispose() {
        UITheme.removeThemeChangeListener(this);
        super.dispose();
    }
}

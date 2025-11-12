package com.mycompany.labopr.views;

import com.mycompany.labopr.ui.factories.ButtonFactory;
import com.mycompany.labopr.ui.factories.PanelFactory;
import com.mycompany.labopr.ui.factories.RoundedButtonFactory;
import com.mycompany.labopr.ui.factories.StandardPanelFactory;
import com.mycompany.labopr.ui.theme.UITheme;
import javax.swing.*;
import java.awt.*;

/**
 * Landing - FIXED: Home, About, Contact buttons always white with black text
 */
public class Landing extends JFrame implements UITheme.ThemeChangeListener {

    private final ButtonFactory buttonFactory = new RoundedButtonFactory();
    private final PanelFactory panelFactory = new StandardPanelFactory();
    
    private JPanel topNav;
    private JPanel contentPanel;
    private JPanel buttonPanel;
    private JLabel title;
    private JLabel subtitle;

    public Landing() {
        setTitle("KwarTrack - Landing Page");
        setSize(1300, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        ImageIcon icon = UITheme.getCachedLogo("/KTrack Logo.png", 64, 64);
        if (icon != null) {
            setIconImage(icon.getImage());
        }

        UITheme.addThemeChangeListener(this);

        initializeComponents();
        updateTheme();

        setVisible(true);
    }

    private void initializeComponents() {
        // Top panel with navigation
        JPanel topPanel = panelFactory.createPanel(new BorderLayout());
        topPanel.setOpaque(false);

        topNav = panelFactory.createFlowPanel(FlowLayout.RIGHT, 20, 10);
        topNav.setBackground(UITheme.PANEL_BG);
        topNav.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // FIXED: Use createFixedWhiteButton for navigation buttons
        String[] navItems = {"Home", "About", "Contact"};
        RoundedButtonFactory factory = (RoundedButtonFactory) buttonFactory;
        
        for (String item : navItems) {
            // FIXED: Create fixed white button (always white bg, black text)
            JButton btn = factory.createFixedWhiteButton(item, UITheme.NAV_BUTTON_SIZE);
            btn.addActionListener(e -> showNavDialog(item));
            topNav.add(btn);
        }

        // Dark mode button panel
        JPanel darkModePanel = panelFactory.createFlowPanel(FlowLayout.LEFT, 0, 0);
        darkModePanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 0));
        darkModePanel.add(buttonFactory.createThemeToggleButton());

        topPanel.add(darkModePanel, BorderLayout.WEST);
        topPanel.add(topNav, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Main content panel
        contentPanel = panelFactory.createPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        contentPanel.add(Box.createVerticalStrut(100));
        
        JLabel logoLabel = new JLabel();
        ImageIcon logoIcon = UITheme.getCachedLogo("/KTrack Logo.png", 180, 180);
        if (logoIcon != null) {
            logoLabel.setIcon(logoIcon);
        } else {
            logoLabel.setText("KwarTrack");
            logoLabel.setForeground(UITheme.TEXT_COLOR);
            logoLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 48));
        }
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(logoLabel);
        
        contentPanel.add(Box.createVerticalStrut(20));

        title = new JLabel("Welcome to KwarTrack!", SwingConstants.CENTER);
        title.setForeground(UITheme.TEXT_COLOR);
        title.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, UITheme.FONT_LARGE));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(title);
        
        contentPanel.add(Box.createVerticalStrut(20));

        subtitle = new JLabel("Your personal finance tracker and manager", SwingConstants.CENTER);
        subtitle.setForeground(UITheme.TEXT_COLOR);
        subtitle.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, UITheme.FONT_MEDIUM));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(subtitle);

        add(contentPanel, BorderLayout.CENTER);

        // Bottom button panel
        buttonPanel = panelFactory.createPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(
            UITheme.PANEL_PADDING_TOP, 0, UITheme.PANEL_PADDING_BOTTOM, 0));

        JButton getStartedBtn = buttonFactory.createButton("Get Started");
        getStartedBtn.addActionListener(e -> handleGetStarted());
        getStartedBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(getStartedBtn);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void handleGetStarted() {
        dispose();
        new MainApp().setVisible(true);
    }

    @Override
    public void onThemeChanged() {
        updateTheme();
    }

    private void updateTheme() {
        getContentPane().setBackground(UITheme.PANEL_BG);
        topNav.setBackground(UITheme.PANEL_BG);
        title.setForeground(UITheme.TEXT_COLOR);
        subtitle.setForeground(UITheme.TEXT_COLOR);
        
        // FIXED: Navigation buttons maintain white background and black text
        // (No theme update needed - they're already fixed-style buttons)
        
        repaint();
    }

    private void showNavDialog(String item) {
        String message = switch (item) {
            case "Home" -> "<html><h2>Home</h2><p>This is the Home page draft.</p></html>";
            case "About" -> "<html><h2>About</h2><p>KwarTrack helps you manage and track your finances easily.</p></html>";
            case "Contact" -> "<html><h2>Contact</h2><p>Email: support@kwartrack.com<br>Phone: +123-456-7890</p></html>";
            default -> "";
        };
        JOptionPane.showMessageDialog(this, message, item, JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void dispose() {
        UITheme.removeThemeChangeListener(this);
        super.dispose();
    }
}
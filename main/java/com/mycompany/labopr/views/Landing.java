package com.mycompany.labopr.views;

import com.mycompany.labopr.ui.factories.ButtonFactory;
import com.mycompany.labopr.ui.factories.PanelFactory;
import com.mycompany.labopr.ui.factories.RoundedButtonFactory;
import com.mycompany.labopr.ui.factories.StandardPanelFactory;
import com.mycompany.labopr.ui.theme.UITheme;
import javax.swing.*;
import java.awt.*;

/**
 * Landing - Updated with About/Contact panels, dark mode support, and no Home button
 */
public class Landing extends JFrame implements UITheme.ThemeChangeListener {

    private final ButtonFactory buttonFactory = new RoundedButtonFactory();
    private final PanelFactory panelFactory = new StandardPanelFactory();
    
    private JPanel topNav;
    private JPanel contentPanel;
    private JPanel buttonPanel;
    private JLabel title;
    private JLabel subtitle;
    private JPanel mainPanel;
    private JPanel topPanel;
    private Image backgroundImage;

    public Landing() {
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

    // ================= ABOUT PANEL (Inner Class) =================
    class AboutPanel extends JPanel implements UITheme.ThemeChangeListener {
        
        private Image aboutBackgroundImage;
        private JButton themeToggleBtn;
        private JLabel aboutTitle;
        private JLabel aboutDesc;
        
        public AboutPanel(Runnable onBack) {
            loadAboutBackgroundImage();
            setLayout(new BorderLayout());
            
            UITheme.addThemeChangeListener(this);
            
            // Top panel with dark mode button
            JPanel topPanelAbout = panelFactory.createPanel(new BorderLayout());
            topPanelAbout.setOpaque(false);
            topPanelAbout.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            JPanel darkModePanel = panelFactory.createFlowPanel(FlowLayout.RIGHT, 0, 0);
            darkModePanel.setOpaque(false);
            themeToggleBtn = buttonFactory.createThemeToggleButton();
            themeToggleBtn.addActionListener(e -> {
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window != null) {
                    window.revalidate();
                    window.repaint();
                }
            });
            darkModePanel.add(themeToggleBtn);
            topPanelAbout.add(darkModePanel, BorderLayout.EAST);
            
            add(topPanelAbout, BorderLayout.NORTH);
            
            // Center panel with logo and text
            JPanel centerPanel = panelFactory.createPanel();
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
            centerPanel.setOpaque(false);
            centerPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
            
            centerPanel.add(Box.createVerticalGlue());
            
            // Add logo (smaller size)
            try {
                ImageIcon logoIcon = new ImageIcon(getClass().getResource("/KLOGO.png"));
                Image scaledImage = logoIcon.getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH);
                JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));
                logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                centerPanel.add(logoLabel);
                centerPanel.add(Box.createRigidArea(new Dimension(0, 25)));
            } catch (Exception e) {
                System.err.println("Error loading logo: " + e.getMessage());
            }
            
            // Add about title
            aboutTitle = new JLabel("About KwarTrack", SwingConstants.CENTER);
            aboutTitle.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 32));
            aboutTitle.setForeground(UITheme.TEXT_COLOR);
            aboutTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
            centerPanel.add(aboutTitle);
            
            centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            
            // Add about description - multiline text that fits in panel
            aboutDesc = new JLabel("<html><div style='text-align: center; width: 700px;'>" +
                "KwarTrack is a simple, user-friendly budget and financial tracking system that helps " +
                "individuals and students monitor their income, expenses, and savings in real time through desktop. " +
                "Many individuals, especially students and young professionals, struggle with managing their finances " +
                "due to a lack of proper tools, discipline, and visibility into their spending habits." +
                "</div></html>", SwingConstants.CENTER);
            aboutDesc.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 24));
            aboutDesc.setForeground(UITheme.TEXT_COLOR);
            aboutDesc.setAlignmentX(Component.CENTER_ALIGNMENT);
            centerPanel.add(aboutDesc);
            
            centerPanel.add(Box.createVerticalGlue());
            
            add(centerPanel, BorderLayout.CENTER);
            
            // Bottom panel with back button
            JPanel bottom = panelFactory.createPanel();
            bottom.setOpaque(false);
            bottom.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));
            JButton backButton = buttonFactory.createButton("Back");
            backButton.addActionListener(e -> {
                UITheme.removeThemeChangeListener(this);
                onBack.run();
            });
            bottom.add(backButton);
            add(bottom, BorderLayout.SOUTH);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            // Fill with PRIMARY_GREEN
            g2d.setColor(UITheme.PRIMARY_GREEN);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // Draw background image if available
            if (aboutBackgroundImage != null) {
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.drawImage(aboutBackgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
        
        @Override
        public void onThemeChanged() {
            aboutTitle.setForeground(UITheme.TEXT_COLOR);
            aboutDesc.setForeground(UITheme.TEXT_COLOR);
            repaint();
        }
        
        private void loadAboutBackgroundImage() {
            try {
                ImageIcon icon = new ImageIcon(getClass().getResource("/KBACKGROUND.png"));
                aboutBackgroundImage = icon.getImage();
            } catch (Exception e) {
                System.err.println("Error loading about background image: " + e.getMessage());
                aboutBackgroundImage = null;
            }
        }
    }

    // ================= CONTACT PANEL (Inner Class) =================
    class ContactPanel extends JPanel implements UITheme.ThemeChangeListener {
        
        private Image contactBackgroundImage;
        private ImageIcon PICTURE_1;
        private ImageIcon PICTURE_2;
        private ImageIcon PICTURE_3;
        private JButton themeToggleBtn;
        private JLabel headerLabel;
        
        public ContactPanel(Runnable onBack) {
            loadContactBackgroundImage();
            loadProfileImages();
            setLayout(new BorderLayout());
            
            UITheme.addThemeChangeListener(this);
            
            // Top panel with dark mode button
            JPanel topPanelContact = panelFactory.createPanel(new BorderLayout());
            topPanelContact.setOpaque(false);
            topPanelContact.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            JPanel darkModePanel = panelFactory.createFlowPanel(FlowLayout.RIGHT, 0, 0);
            darkModePanel.setOpaque(false);
            themeToggleBtn = buttonFactory.createThemeToggleButton();
            themeToggleBtn.addActionListener(e -> {
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window != null) {
                    window.revalidate();
                    window.repaint();
                }
            });
            darkModePanel.add(themeToggleBtn);
            topPanelContact.add(darkModePanel, BorderLayout.EAST);
            
            add(topPanelContact, BorderLayout.NORTH);
            
            // Main container with GridBagLayout for centering
            JPanel mainPanelInner = panelFactory.createPanel(new GridBagLayout());
            mainPanelInner.setOpaque(false);
            mainPanelInner.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
            
            // Content container
            JPanel contentPanelInner = panelFactory.createPanel();
            contentPanelInner.setLayout(new BoxLayout(contentPanelInner, BoxLayout.Y_AXIS));
            contentPanelInner.setOpaque(false);
            
            // Header
            headerLabel = new JLabel("CONTACT US");
            headerLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 32));
            headerLabel.setForeground(UITheme.TEXT_COLOR);
            headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanelInner.add(headerLabel);
            contentPanelInner.add(Box.createRigidArea(new Dimension(0, 40)));
            
            // Profile panel (horizontal layout for 3 profiles)
            JPanel profilesPanel = panelFactory.createPanel();
            profilesPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 0));
            profilesPanel.setOpaque(false);
            
            // Add three profiles
            profilesPanel.add(createProfilePanel(PICTURE_1, "LANOHAN,", "DYLAN ALPHONSO", "dlanohan@addu.edu.ph"));
            profilesPanel.add(createProfilePanel(PICTURE_2, "SABANAL,", "DAN CHARBILLE DGRACE", "dsabanal@addu.edu.ph"));
            profilesPanel.add(createProfilePanel(PICTURE_3, "NIEGAS,", "NEIL JHON", "njbniehas@addu.edu.ph"));
            
            contentPanelInner.add(profilesPanel);
            
            mainPanelInner.add(contentPanelInner);
            add(mainPanelInner, BorderLayout.CENTER);
            
            // Bottom panel with back button
            JPanel bottom = panelFactory.createPanel();
            bottom.setOpaque(false);
            bottom.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));
            JButton backButton = buttonFactory.createButton("Back");
            backButton.addActionListener(e -> {
                UITheme.removeThemeChangeListener(this);
                onBack.run();
            });
            bottom.add(backButton);
            add(bottom, BorderLayout.SOUTH);
        }
        
        private JPanel createProfilePanel(ImageIcon icon, String firstName, String lastName, String email) {
            JPanel panel = panelFactory.createPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setOpaque(false);
            
            // Create image label
            JLabel imageLabel = new JLabel();
            imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            if (icon != null) {
                Image img = icon.getImage();
                Image scaledImg = img.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaledImg));
            } else {
                imageLabel.setPreferredSize(new Dimension(150, 150));
                imageLabel.setText("No Image");
                imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                imageLabel.setForeground(UITheme.TEXT_COLOR);
            }
            
            panel.add(imageLabel);
            panel.add(Box.createRigidArea(new Dimension(0, 15)));
            
            // Name and email label
            JLabel nameLabel = new JLabel("<html><div style='text-align: center;'><b>" + 
                firstName + "</b><br>" + lastName + "<br>" + email + "</div></html>");
            nameLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 24));
            nameLabel.setForeground(UITheme.TEXT_COLOR);
            nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            panel.add(nameLabel);
            
            return panel;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            // Fill with PRIMARY_GREEN
            g2d.setColor(UITheme.PRIMARY_GREEN);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // Draw background image if available
            if (contactBackgroundImage != null) {
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.drawImage(contactBackgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
        
        @Override
        public void onThemeChanged() {
            headerLabel.setForeground(UITheme.TEXT_COLOR);
            
            // Update profile name labels
            Component[] components = getComponents();
            for (Component comp : components) {
                if (comp instanceof JPanel) {
                    updateProfileLabels((JPanel) comp);
                }
            }
            
            repaint();
        }
        
        private void updateProfileLabels(JPanel panel) {
            Component[] components = panel.getComponents();
            for (Component comp : components) {
                if (comp instanceof JLabel) {
                    ((JLabel) comp).setForeground(UITheme.TEXT_COLOR);
                } else if (comp instanceof JPanel) {
                    updateProfileLabels((JPanel) comp);
                }
            }
        }
        
        private void loadContactBackgroundImage() {
            try {
                ImageIcon icon = new ImageIcon(getClass().getResource("/KBACKGROUND.png"));
                contactBackgroundImage = icon.getImage();
            } catch (Exception e) {
                System.err.println("Error loading contact background image: " + e.getMessage());
                contactBackgroundImage = null;
            }
        }
        
        private void loadProfileImages() {
            try {
                PICTURE_1 = new ImageIcon(getClass().getResource("/PICTURE_1.png"));
            } catch (Exception e) {
                System.err.println("Error loading PICTURE_1.png: " + e.getMessage());
                PICTURE_1 = null;
            }
            
            try {
                PICTURE_2 = new ImageIcon(getClass().getResource("/PICTURE_2.png"));
            } catch (Exception e) {
                System.err.println("Error loading PICTURE_2.png: " + e.getMessage());
                PICTURE_2 = null;
            }
            
            try {
                PICTURE_3 = new ImageIcon(getClass().getResource("/PICTURE_3.png"));
            } catch (Exception e) {
                System.err.println("Error loading PICTURE_3.png: " + e.getMessage());
                PICTURE_3 = null;
            }
        }
    }

    // ================= MAIN LANDING METHODS =================
    
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

        // Top panel with navigation
        topPanel = panelFactory.createPanel(new BorderLayout());
        topPanel.setOpaque(false);

        topNav = panelFactory.createFlowPanel(FlowLayout.RIGHT, 20, 10);
        topNav.setOpaque(false);
        topNav.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Navigation buttons - REMOVED "Home", only About and Contact
        String[] navItems = {"About", "Contact"};
        RoundedButtonFactory factory = (RoundedButtonFactory) buttonFactory;
        
        for (String item : navItems) {
            JButton btn = factory.createFixedWhiteButton(item, UITheme.NAV_BUTTON_SIZE);
            btn.addActionListener(e -> handleNavigation(item));
            topNav.add(btn);
        }

        // Dark mode button panel
        JPanel darkModePanel = panelFactory.createFlowPanel(FlowLayout.LEFT, 0, 0);
        darkModePanel.setOpaque(false);
        darkModePanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 0));
        darkModePanel.add(buttonFactory.createThemeToggleButton());

        topPanel.add(darkModePanel, BorderLayout.WEST);
        topPanel.add(topNav, BorderLayout.EAST);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Main content panel
        contentPanel = panelFactory.createPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        contentPanel.add(Box.createVerticalStrut(100));
        
        JLabel logoLabel = new JLabel();
        ImageIcon logoIcon = UITheme.getCachedLogo("/KLOGO.png", 180, 180);
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

        subtitle = new JLabel("Your Personal Finance Tracker and Manager", SwingConstants.CENTER);
        subtitle.setForeground(UITheme.TEXT_COLOR);
        subtitle.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, UITheme.FONT_MEDIUM));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(subtitle);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

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

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void handleNavigation(String item) {
        mainPanel.removeAll();
        
        JPanel newPanel;
        switch (item) {
            case "About" -> newPanel = new AboutPanel(this::goBackToMain);
            case "Contact" -> newPanel = new ContactPanel(this::goBackToMain);
            default -> {
                goBackToMain();
                return;
            }
        }
        
        mainPanel.add(newPanel, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void goBackToMain() {
        mainPanel.removeAll();
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.revalidate();
        mainPanel.repaint();
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

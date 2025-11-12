package com.mycompany.labopr.views;

import com.mycompany.labopr.data.DataFacade;
import com.mycompany.labopr.database.DatabaseDAO;
import com.mycompany.labopr.ui.panels.*;
import com.mycompany.labopr.ui.factories.ButtonFactory;
import com.mycompany.labopr.ui.factories.PanelFactory;
import com.mycompany.labopr.ui.factories.RoundedButtonFactory;
import com.mycompany.labopr.ui.factories.StandardPanelFactory;
import com.mycompany.labopr.ui.theme.UITheme;
import javax.swing.*;
import java.awt.*;

/**
 * MainApp - FIXED: Dark theme support for all panels
 */
public class MainApp extends JFrame implements UITheme.ThemeChangeListener {

    private final ButtonFactory buttonFactory = new RoundedButtonFactory();
    private final PanelFactory panelFactory = new StandardPanelFactory();
    private final DataFacade dataFacade;
    
    private SidebarPanel sidebarPanel;
    private JPanel mainContentArea;
    private DashboardPanel dashboardPanel;
    private TransactionsPanel transactionsPanel;
    private BudgetsPanel budgetsPanel;
    private SettingsPanel settingsPanel;

    public MainApp() {
        this.dataFacade = DataFacade.getInstance();
        
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
        initializeComponents();
        updateTheme();

        setVisible(true);
    }

    private void initializeComponents() {
        // Create sidebar using factory
        String[] buttonLabels = {"Dashboard", "Transactions", "Budgets/Goals", "Settings"};
        sidebarPanel = new SidebarPanel(buttonLabels, this::handleNavigation, 
                                       this::handleLogout, buttonFactory);
        
        add(sidebarPanel, BorderLayout.WEST);

        // Create main content area using factory
        mainContentArea = panelFactory.createPanel(new BorderLayout());
        mainContentArea.setBackground(UITheme.PANEL_BG);
        
        // Create welcome panel using factory
        JPanel welcomePanel = panelFactory.createPanel(new BorderLayout());
        welcomePanel.setBackground(UITheme.PANEL_BG);
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel welcomeLabel = new JLabel("Welcome! Select a menu item from the sidebar.", SwingConstants.CENTER);
        welcomeLabel.setForeground(UITheme.TEXT_COLOR);
        welcomeLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 18));
        welcomePanel.add(welcomeLabel, BorderLayout.CENTER);
        
        mainContentArea.add(welcomePanel, BorderLayout.CENTER);
        add(mainContentArea, BorderLayout.CENTER);
        
        // Set default active button and load dashboard
        sidebarPanel.setActiveButton(0);
        handleNavigation(0);
    }

    private void handleNavigation(int index) {
        sidebarPanel.setActiveButton(index);
        
        // Remove existing center component
        Component centerComp = ((BorderLayout) mainContentArea.getLayout())
            .getLayoutComponent(BorderLayout.CENTER);
        if (centerComp != null) {
            mainContentArea.remove(centerComp);
        }
        
        switch (index) {
            case 0: displayDashboard(); break;
            case 1: displayTransactions(); break;
            case 2: displayBudgets(); break;
            case 3: displaySettings(); break;
        }
        
        mainContentArea.revalidate();
        mainContentArea.repaint();
    }
    
    private void displayDashboard() {
        if (dashboardPanel == null) {
            dashboardPanel = new DashboardPanel(this, dataFacade, buttonFactory, panelFactory);
        }
        mainContentArea.add(dashboardPanel, BorderLayout.CENTER);
        mainContentArea.setBackground(UITheme.PANEL_BG);
    }
    
    private void displayTransactions() {
        if (transactionsPanel == null) {
            transactionsPanel = new TransactionsPanel(this, dataFacade, buttonFactory, panelFactory);
        }
        mainContentArea.add(transactionsPanel, BorderLayout.CENTER);
        mainContentArea.setBackground(UITheme.PANEL_BG);
    }
    
    private void displayBudgets() {
        if (budgetsPanel == null) {
            budgetsPanel = new BudgetsPanel(this, dataFacade, buttonFactory, panelFactory);
        }
        mainContentArea.add(budgetsPanel, BorderLayout.CENTER);
        mainContentArea.setBackground(UITheme.PANEL_BG);
    }
    
    private void displaySettings() {
        if (settingsPanel == null) {
            settingsPanel = new SettingsPanel(this, dataFacade, buttonFactory, panelFactory);
        }
        mainContentArea.add(settingsPanel, BorderLayout.CENTER);
        mainContentArea.setBackground(UITheme.PANEL_BG);
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Logout",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            DatabaseDAO.clearSession();
            dispose();
            new GUI().setVisible(true);
        }
    }

    @Override
    public void onThemeChanged() {
        updateTheme();
    }

    private void updateTheme() {
        mainContentArea.setBackground(UITheme.PANEL_BG);
        repaint();
    }

    @Override
    public void dispose() {
        if (dashboardPanel != null) dashboardPanel.cleanup();
        if (transactionsPanel != null) transactionsPanel.cleanup();
        if (budgetsPanel != null) budgetsPanel.cleanup();
        if (settingsPanel != null) settingsPanel.cleanup();
        if (sidebarPanel != null) sidebarPanel.cleanup();
        
        UITheme.removeThemeChangeListener(this);
        super.dispose();
    }
}

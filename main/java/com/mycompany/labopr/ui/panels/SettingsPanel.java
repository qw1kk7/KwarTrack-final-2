package com.mycompany.labopr.ui.panels;

import com.mycompany.labopr.data.DataFacade;
import com.mycompany.labopr.observer.PanelObserver;
import com.mycompany.labopr.ui.factories.ButtonFactory;
import com.mycompany.labopr.ui.factories.PanelFactory;
import com.mycompany.labopr.ui.factories.RoundedButtonFactory;
import com.mycompany.labopr.ui.theme.UITheme;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * SettingsPanel - FIXED: Section labels always black, unselected button gray
 */
public class SettingsPanel extends JPanel implements UITheme.ThemeChangeListener {
    private final DataFacade dataFacade;
    private final JFrame parentFrame;
    private final ButtonFactory buttonFactory;
    private final PanelFactory panelFactory;
    
    private JButton themeToggleBtn;
    private JLabel themeModeLabel;
    private JLabel dataManagementLabel;
    private JButton lightModeBtn;
    private JButton darkModeBtn;
    
    // FIXED: Color constants for theme toggle buttons
    private static final Color BUTTON_SELECTED = Color.decode("#66a74b"); // Green
    private static final Color BUTTON_UNSELECTED = Color.decode("#d3d3d3"); // Light gray
    private static final Color BUTTON_HOVER = Color.decode("#bfbfbf"); // Slightly darker gray
    private static final Color TEXT_SELECTED = Color.WHITE;
    private static final Color TEXT_UNSELECTED = Color.decode("#333333"); // Dark gray/black
    
    public SettingsPanel(JFrame parent, DataFacade dataFacade,
                        ButtonFactory buttonFactory, PanelFactory panelFactory) {
        this.parentFrame = parent;
        this.dataFacade = dataFacade;
        this.buttonFactory = buttonFactory;
        this.panelFactory = panelFactory;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.isDarkMode() ? Color.BLACK : UITheme.PRIMARY_GREEN);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        UITheme.addThemeChangeListener(this);
        
        initComponents();
    }
    
    private void initComponents() {
        // Title - Left-aligned
        JPanel topPanel = panelFactory.createPanel(new BorderLayout());
        topPanel.setBackground(UITheme.isDarkMode() ? Color.BLACK : UITheme.PRIMARY_GREEN);
        
        JLabel titleLabel = new JLabel("Settings and Data Management");
        titleLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 32));
        titleLabel.setForeground(UITheme.TEXT_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        topPanel.add(titleLabel, BorderLayout.WEST);
        
        // Theme toggle in top-right corner
        JPanel rightPanel = panelFactory.createFlowPanel(FlowLayout.RIGHT, 0, 0);
        rightPanel.setOpaque(false);
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
        
        // Center content panel
        JPanel centerPanel = panelFactory.createPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        
        // Add vertical spacing from top
        centerPanel.add(Box.createVerticalStrut(40));
        
        // Appearance section
        centerPanel.add(createAppearanceSection());
        centerPanel.add(Box.createVerticalStrut(30));
        
        // Data Management section
        centerPanel.add(createDataManagementSection());
        
        JScrollPane scrollPane = new JScrollPane(centerPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(UITheme.isDarkMode() ? Color.BLACK : UITheme.PRIMARY_GREEN);
        scrollPane.setBorder(null);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createAppearanceSection() {
        JPanel section = panelFactory.createPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(Color.WHITE);
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xcccccc), 1),
            BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));
        section.setMaximumSize(new Dimension(800, Integer.MAX_VALUE));
        section.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // FIXED: Section title - always black in both themes
        JPanel titlePanel = panelFactory.createFlowPanel(FlowLayout.CENTER, 0, 0);
        titlePanel.setOpaque(false);
        themeModeLabel = new JLabel("Theme Mode");
        themeModeLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 24));
        themeModeLabel.setForeground(Color.BLACK); // FIXED: Always black
        titlePanel.add(themeModeLabel);
        section.add(titlePanel);
        section.add(Box.createVerticalStrut(25));
        
        // FIXED: Theme mode toggle with gray unselected button
        JPanel modePanel = panelFactory.createPanel();
        modePanel.setLayout(new BoxLayout(modePanel, BoxLayout.Y_AXIS));
        modePanel.setOpaque(false);
        modePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JPanel togglePanel = panelFactory.createFlowPanel(FlowLayout.CENTER, 10, 10);
        togglePanel.setOpaque(false);
        
        // FIXED: Create custom styled buttons
        lightModeBtn = createThemeToggleButton("Light Mode");
        darkModeBtn = createThemeToggleButton("Dark Mode");
        
        lightModeBtn.setPreferredSize(new Dimension(140, 45));
        darkModeBtn.setPreferredSize(new Dimension(140, 45));
        
        // Set initial states
        updateThemeButtonStates();
        
        lightModeBtn.addActionListener(e -> {
            if (UITheme.isDarkMode()) {
                UITheme.toggleDarkMode();
                updateThemeButtonStates();
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window != null) {
                    window.revalidate();
                    window.repaint();
                }
            }
        });
        
        darkModeBtn.addActionListener(e -> {
            if (!UITheme.isDarkMode()) {
                UITheme.toggleDarkMode();
                updateThemeButtonStates();
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window != null) {
                    window.revalidate();
                    window.repaint();
                }
            }
        });
        
        togglePanel.add(lightModeBtn);
        togglePanel.add(darkModeBtn);
        
        modePanel.add(Box.createVerticalStrut(12));
        modePanel.add(togglePanel);
        
        section.add(modePanel);
        
        return section;
    }
    
    /**
     * FIXED: Create custom theme toggle button with proper styling
     */
    private JButton createThemeToggleButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 14));
        
        // Add hover effect
        addThemeButtonHoverEffect(btn);
        
        return btn;
    }
    
    /**
     * FIXED: Add hover effect for theme toggle buttons
     */
    private void addThemeButtonHoverEffect(JButton btn) {
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Only apply hover if button is unselected
                boolean isSelected = (btn == lightModeBtn && !UITheme.isDarkMode()) ||
                                   (btn == darkModeBtn && UITheme.isDarkMode());
                
                if (!isSelected) {
                    btn.setBackground(BUTTON_HOVER);
                    btn.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Restore proper state
                updateThemeButtonStates();
            }
        });
    }
    
    /**
     * FIXED: Update theme button states (selected green, unselected gray)
     */
    private void updateThemeButtonStates() {
        boolean isDark = UITheme.isDarkMode();
        
        if (isDark) {
            // Dark mode selected
            darkModeBtn.setBackground(BUTTON_SELECTED);
            darkModeBtn.setForeground(TEXT_SELECTED);
            lightModeBtn.setBackground(BUTTON_UNSELECTED); // Gray
            lightModeBtn.setForeground(TEXT_UNSELECTED); // Dark gray/black
        } else {
            // Light mode selected
            lightModeBtn.setBackground(BUTTON_SELECTED);
            lightModeBtn.setForeground(TEXT_SELECTED);
            darkModeBtn.setBackground(BUTTON_UNSELECTED); // Gray
            darkModeBtn.setForeground(TEXT_UNSELECTED); // Dark gray/black
        }
        
        lightModeBtn.repaint();
        darkModeBtn.repaint();
    }
    
    private JPanel createDataManagementSection() {
        JPanel section = panelFactory.createPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(Color.WHITE);
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xcccccc), 1),
            BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));
        section.setMaximumSize(new Dimension(800, Integer.MAX_VALUE));
        section.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // FIXED: Section title - always black in both themes
        JPanel titlePanel = panelFactory.createFlowPanel(FlowLayout.CENTER, 0, 0);
        titlePanel.setOpaque(false);
        dataManagementLabel = new JLabel("Data Management");
        dataManagementLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 24));
        dataManagementLabel.setForeground(Color.BLACK); // FIXED: Always black
        titlePanel.add(dataManagementLabel);
        section.add(titlePanel);
        section.add(Box.createVerticalStrut(25));
        
        // Buttons panel - centered with green buttons
        JPanel buttonsPanel = panelFactory.createPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        RoundedButtonFactory factory = (RoundedButtonFactory) buttonFactory;
        
        // Export Data button
        JButton exportBtn = factory.createGreenButton("Export Data", new Dimension(240, 50));
        exportBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        exportBtn.addActionListener(e -> handleExportData());
        
        // Import Data button
        JButton importBtn = factory.createGreenButton("Import Data", new Dimension(240, 50));
        importBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        importBtn.addActionListener(e -> handleImportData());
        
        // Reset All Data button
        JButton resetBtn = factory.createGreenButton("Reset All Data", new Dimension(240, 50));
        resetBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        resetBtn.addActionListener(e -> handleResetData());
        
        buttonsPanel.add(exportBtn);
        buttonsPanel.add(Box.createVerticalStrut(15));
        buttonsPanel.add(importBtn);
        buttonsPanel.add(Box.createVerticalStrut(15));
        buttonsPanel.add(resetBtn);
        
        section.add(buttonsPanel);
        
        return section;
    }
    
    private void handleExportData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Data");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
        fileChooser.setSelectedFile(new File("kwartrack_export.csv"));
        
        int result = fileChooser.showSaveDialog(parentFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String filepath = file.getAbsolutePath();
            if (!filepath.endsWith(".csv")) {
                filepath += ".csv";
            }
            
            if (dataFacade.exportData(filepath)) {
                JOptionPane.showMessageDialog(
                    parentFrame,
                    "Data exported successfully to:\n" + filepath,
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                    parentFrame,
                    "Failed to export data. Please try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    private void handleImportData() {
        int confirm = JOptionPane.showConfirmDialog(
            parentFrame,
            "Importing data will add to your existing data.\n" +
            "It's recommended to export your current data first as a backup.\n\n" +
            "Continue with import?",
            "Confirm Import",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Import Data");
            fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
            
            int result = fileChooser.showOpenDialog(parentFrame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                
                if (dataFacade.importData(file.getAbsolutePath())) {
                    JOptionPane.showMessageDialog(
                        parentFrame,
                        "Data imported successfully!\nAll panels will refresh automatically.",
                        "Import Complete",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    PanelObserver.getInstance().notifyObservers();
                } else {
                    JOptionPane.showMessageDialog(
                        parentFrame,
                        "Failed to import data. Please check the file format.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }
    }
    
    private void handleResetData() {
        int confirm = JOptionPane.showConfirmDialog(
            parentFrame,
            "Are you sure you want to reset all data?\n\n" +
            "This will permanently delete:\n" +
            "• All transactions\n" +
            "• All budget goals\n" +
            "• Balance information\n\n" +
            "This action cannot be undone!",
            "Confirm Reset",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (dataFacade.resetAllData()) {
                JOptionPane.showMessageDialog(
                    parentFrame,
                    "All data has been reset successfully.",
                    "Reset Complete",
                    JOptionPane.INFORMATION_MESSAGE
                );
                PanelObserver.getInstance().notifyObservers();
            } else {
                JOptionPane.showMessageDialog(
                    parentFrame,
                    "Failed to reset data. Please try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    @Override
    public void onThemeChanged() {
        boolean isDark = UITheme.isDarkMode();
        Color bgColor = isDark ? Color.BLACK : UITheme.PRIMARY_GREEN;
        
        setBackground(bgColor);
        
        // FIXED: Section labels remain BLACK in both themes
        // (No color update needed - they're already set to black)
        
        // Update theme toggle button states
        updateThemeButtonStates();
        
        // Update all panels except white sections
        Component[] components = getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                updatePanelBackground((JPanel) comp, bgColor);
            } else if (comp instanceof JScrollPane) {
                JScrollPane scroll = (JScrollPane) comp;
                scroll.getViewport().setBackground(bgColor);
                if (scroll.getViewport().getView() instanceof JPanel) {
                    updatePanelBackground((JPanel) scroll.getViewport().getView(), bgColor);
                }
            }
        }
        
        repaint();
    }
    
    private void updatePanelBackground(JPanel panel, Color bgColor) {
        // Don't change white section backgrounds
        if (!panel.getBackground().equals(Color.WHITE)) {
            panel.setBackground(bgColor);
        }
        
        Component[] components = panel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                updatePanelBackground((JPanel) comp, bgColor);
            }
        }
    }
    
    public void cleanup() {
        UITheme.removeThemeChangeListener(this);
    }
}
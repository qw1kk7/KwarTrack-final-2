package com.mycompany.labopr.ui.panels;

import com.mycompany.labopr.ui.factories.ButtonFactory;
import com.mycompany.labopr.ui.theme.UITheme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * SidebarPanel - FINAL FIX: Default #cfffba background, grayish in dark mode
 * Added Logout button at bottom with consistent styling
 */
public class SidebarPanel extends JPanel implements UITheme.ThemeChangeListener {
    
    private static final Color SIDEBAR_BG_LIGHT = new Color(0xf5f5f5);
    private static final Color SIDEBAR_BG_DARK = new Color(0x1a1a1a);
    
    // FIXED: Default button background is #cfffba (light green)
    private static final Color BUTTON_DEFAULT_LIGHT = new Color(0xcfffba);
    private static final Color BUTTON_DEFAULT_DARK = new Color(0x4a4a4a); // Grayish for dark mode
    
    private static final Color BUTTON_HOVER_LIGHT = new Color(0xb8e89c);
    private static final Color BUTTON_HOVER_DARK = new Color(0x5a5a5a);
    
    private static final Color BUTTON_ACTIVE_LIGHT = new Color(0x7ed957); // Darker green when active
    private static final Color BUTTON_ACTIVE_DARK = new Color(0x5ca03f);
    
    private final ButtonFactory buttonFactory;
    private SidebarButton[] buttons;
    private int activeIndex = -1;
    
    public SidebarPanel(String[] buttonLabels, Consumer<Integer> navigationHandler, 
                       Runnable logoutHandler, ButtonFactory buttonFactory) {
        this.buttonFactory = buttonFactory;
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(200, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        
        UITheme.addThemeChangeListener(this);
        updateBackground();
        
        initializeButtons(buttonLabels, navigationHandler, logoutHandler);
    }
    
    private void initializeButtons(String[] buttonLabels, Consumer<Integer> navigationHandler, 
                                   Runnable logoutHandler) {
        buttons = new SidebarButton[buttonLabels.length + 1];
        
        // Navigation buttons
        for (int i = 0; i < buttonLabels.length; i++) {
            SidebarButton btn = new SidebarButton(buttonLabels[i]);
            final int index = i;
            btn.addActionListener(e -> {
                setActiveButton(index);
                navigationHandler.accept(index);
            });
            buttons[i] = btn;
            add(btn);
            if (i < buttonLabels.length - 1) {
                add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }
        
        // Push logout to bottom
        add(Box.createVerticalGlue());
        
        // FIXED: Added Logout button with same styling
        SidebarButton logoutBtn = new SidebarButton("Logout");
        logoutBtn.addActionListener(e -> logoutHandler.run());
        buttons[buttonLabels.length] = logoutBtn;
        add(logoutBtn);
    }
    
    public void setActiveButton(int index) {
        resetAllButtons();
        activeIndex = index;
        if (index >= 0 && index < buttons.length) {
            buttons[index].setActive(true);
        }
    }
    
    public void resetAllButtons() {
        for (SidebarButton btn : buttons) {
            btn.setActive(false);
        }
        activeIndex = -1;
    }
    
    public JButton[] getButtons() {
        return buttons;
    }
    
    private void updateBackground() {
        setBackground(UITheme.isDarkMode() ? SIDEBAR_BG_DARK : SIDEBAR_BG_LIGHT);
    }
    
    @Override
    public void onThemeChanged() {
        updateBackground();
        for (SidebarButton btn : buttons) {
            btn.updateForTheme();
        }
        repaint();
    }
    
    public void cleanup() {
        UITheme.removeThemeChangeListener(this);
    }
    
    /**
     * Custom button class for sidebar with state management
     * FIXED: Default background is #cfffba in light mode, grayish in dark mode
     */
    private class SidebarButton extends JButton {
        private boolean isActive = false;
        private boolean isHovered = false;
        
        public SidebarButton(String text) {
            super(text);
            
            setOpaque(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            
            updateTextStyle();
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setHovered(true);
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    setHovered(false);
                }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            Color bgColor;
            if (isActive) {
                // Active: Darker green in light mode, darker green in dark mode
                bgColor = UITheme.isDarkMode() ? BUTTON_ACTIVE_DARK : BUTTON_ACTIVE_LIGHT;
            } else if (isHovered) {
                // Hover: Slightly darker than default
                bgColor = UITheme.isDarkMode() ? BUTTON_HOVER_DARK : BUTTON_HOVER_LIGHT;
            } else {
                // FIXED: Default is #cfffba in light mode, grayish in dark mode
                bgColor = UITheme.isDarkMode() ? BUTTON_DEFAULT_DARK : BUTTON_DEFAULT_LIGHT;
            }
            
            g2d.setColor(bgColor);
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2d.dispose();
            super.paintComponent(g);
        }
        
        public void setActive(boolean active) {
            this.isActive = active;
            updateTextStyle();
            repaint();
        }
        
        public void setHovered(boolean hovered) {
            this.isHovered = hovered;
            repaint();
        }
        
        private void updateTextStyle() {
            if (isActive) {
                setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 14));
                setForeground(Color.WHITE);
            } else {
                setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 14));
                setForeground(UITheme.isDarkMode() ? new Color(0xcccccc) : new Color(0x333333));
            }
        }
        
        public void updateForTheme() {
            updateTextStyle();
            repaint();
        }
    }
}
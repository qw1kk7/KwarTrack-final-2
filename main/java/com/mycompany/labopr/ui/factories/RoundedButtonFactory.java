package com.mycompany.labopr.ui.factories;

import com.mycompany.labopr.ui.theme.UITheme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Enhanced Concrete Factory: RoundedButtonFactory
 * ADDED: createFixedWhiteButton() for theme-independent white buttons
 */
public class RoundedButtonFactory extends ButtonFactory {
    
    
    
    @Override
    public JButton createButton(String text) {
        return createWhiteButton(text, UITheme.ACTION_BUTTON_SIZE);
    }
    
    @Override
    public JButton createButton(String text, Dimension size) {
        return createWhiteButton(text, size);
    }
    
    /**
     * Create white background button with dark text (theme-dependent)
     */
    public JButton createWhiteButton(String text, Dimension size) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.BUTTON_RADIUS, UITheme.BUTTON_RADIUS);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        
        configureBaseButton(btn);
        
        btn.setBackground(UITheme.WHITE_BG);
        btn.setForeground(Color.decode("#333333"));
        
        int fontSize = calculateFontSize(text, size.width - 50);
        btn.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, fontSize));
        btn.setPreferredSize(size);
        btn.setMaximumSize(size);
        
        addWhiteButtonHoverEffect(btn);
        
        return btn;
    }
    
    /**
     * NEW: Create fixed white button that never changes with theme
     * Always white background, always black text, regardless of light/dark mode
     * Used for: Landing navigation buttons, TransactionsPanel balance button
     */
    public JButton createFixedWhiteButton(String text, Dimension size) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.BUTTON_RADIUS, UITheme.BUTTON_RADIUS);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        
        configureBaseButton(btn);
        
        // FIXED: Always white background, always black text
        btn.setBackground(UITheme.FIXED_WHITE_BG);
        btn.setForeground(UITheme.FIXED_BLACK_TEXT);
        
        int fontSize = calculateFontSize(text, size.width - 50);
        btn.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, fontSize));
        btn.setPreferredSize(size);
        btn.setMaximumSize(size);
        
        // FIXED: Hover effect maintains white/black scheme
        addFixedWhiteButtonHoverEffect(btn);
        
        return btn;
    }
    
    /**
     * Create accent button (#66a74b background) for toggle states
     */
    public JButton createAccentButton(String text, Dimension size) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.BUTTON_RADIUS, UITheme.BUTTON_RADIUS);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        
        configureBaseButton(btn);
        
        btn.setBackground(UITheme.ACCENT_BG);
        btn.setForeground(Color.WHITE);
        
        int fontSize = calculateFontSize(text, size.width - 50);
        btn.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, fontSize));
        btn.setPreferredSize(size);
        btn.setMaximumSize(size);
        
        addAccentButtonHoverEffect(btn);
        
        return btn;
    }
    
    /**
     * Create green button (Primary Green #7ed957 background) for SettingsPanel
     */
    public JButton createGreenButton(String text, Dimension size) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.BUTTON_RADIUS, UITheme.BUTTON_RADIUS);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        
        configureBaseButton(btn);
        
        btn.setBackground(UITheme.LIGHT_PRIMARY_GREEN);
        btn.setForeground(Color.WHITE);
        
        int fontSize = calculateFontSize(text, size.width - 50);
        btn.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, fontSize));
        btn.setPreferredSize(size);
        btn.setMaximumSize(size);
        
        addGreenButtonHoverEffect(btn);
        
        return btn;
    }
    
    @Override
    public JButton createIconButton(String iconText, String tooltip) {
        JButton btn = new JButton(iconText) {
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
        
        configureBaseButton(btn);
        btn.setBackground(UITheme.BUTTON_BG);
        btn.setForeground(UITheme.BUTTON_TEXT);
        btn.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 16));
        btn.setPreferredSize(new Dimension(40, 40));
        btn.setToolTipText(tooltip);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        addHoverEffect(btn, UITheme.BUTTON_BG);
        
        return btn;
    }
    
    @Override
    public JButton createToggleButton(String text, boolean selected) {
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
        
        configureBaseButton(btn);
        btn.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 16));
        btn.setPreferredSize(new Dimension(150, 45));
        
        updateToggleButtonState(btn, selected);
        
        return btn;
    }
    
    @Override
    public JButton createNavButton(String text) {
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
        
        configureBaseButton(btn);
        btn.setBackground(UITheme.BUTTON_BG);
        btn.setForeground(UITheme.isDarkMode() ? Color.BLACK : UITheme.BUTTON_TEXT);
        btn.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, UITheme.FONT_NAV));
        btn.setPreferredSize(UITheme.NAV_BUTTON_SIZE);
        
        addHoverEffect(btn, UITheme.BUTTON_BG);
        
        return btn;
    }
    
    @Override
    public JButton createThemeToggleButton() {
        JButton btn = new JButton() {
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
        
        updateThemeToggleAppearance(btn);
        
        configureBaseButton(btn);
        btn.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 12));
        btn.setPreferredSize(UITheme.DARKMODE_BUTTON_SIZE);
        btn.setMaximumSize(UITheme.DARKMODE_BUTTON_SIZE);
        
        Color originalBg = btn.getBackground();
        addHoverEffect(btn, originalBg);
        
        btn.addActionListener(e -> {
            UITheme.toggleDarkMode();
            updateThemeToggleAppearance(btn);
            Color newOriginalBg = btn.getBackground();
            addHoverEffect(btn, newOriginalBg);
        });
        
        return btn;
    }
    
    @Override
    public void updateToggleButtonState(JButton btn, boolean selected) {
        if (selected) {
            btn.setBackground(UITheme.ACCENT_BG);
            btn.setForeground(Color.WHITE);
            addAccentButtonHoverEffect(btn);
        } else {
            btn.setBackground(UITheme.WHITE_BG);
            btn.setForeground(Color.decode("#333333"));
            addWhiteButtonHoverEffect(btn);
        }
        btn.repaint();
    }
    
    @Override
    public void updateButtonForThemeChange(JButton btn) {
        if (btn.getText().equals("🌙") || btn.getText().equals("☀")) {
            return;
        }
        
        boolean isNavButton = btn.getPreferredSize().equals(UITheme.NAV_BUTTON_SIZE);
        
        Color newBg;
        Color newFg;
        
        if (isNavButton) {
            newBg = UITheme.isDarkMode() ? Color.WHITE : UITheme.BUTTON_BG;
            newFg = UITheme.isDarkMode() ? Color.BLACK : UITheme.BUTTON_TEXT;
        } else {
            newBg = UITheme.BUTTON_BG;
            newFg = UITheme.isDarkMode() ? Color.BLACK : UITheme.BUTTON_TEXT;
        }
        
        btn.setBackground(newBg);
        btn.setForeground(newFg);
        
        addHoverEffect(btn, newBg);
        btn.repaint();
    }
    
    // Private helper methods
    
    private void configureBaseButton(JButton btn) {
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
    }
    
    private void updateThemeToggleAppearance(JButton btn) {
        if (UITheme.isDarkMode()) {
            btn.setText("☀");
            btn.setBackground(UITheme.LIGHT_BUTTON_BG);
            btn.setForeground(Color.BLACK);
        } else {
            btn.setText("🌙");
            btn.setBackground(Color.DARK_GRAY);
            btn.setForeground(Color.WHITE);
        }
    }
    
    private int calculateFontSize(String text, int availableWidth) {
        if (text == null || text.isEmpty()) return UITheme.FONT_BUTTON;
        
        int fontSize = Math.min(UITheme.FONT_BUTTON, 16);
        Font font = new Font(UITheme.FONT_FAMILY, Font.BOLD, fontSize);
        FontMetrics fm = new JLabel().getFontMetrics(font);
        int textWidth = fm.stringWidth(text);
        
        while (textWidth > availableWidth && fontSize > 10) {
            fontSize--;
            font = new Font(UITheme.FONT_FAMILY, Font.BOLD, fontSize);
            fm = new JLabel().getFontMetrics(font);
            textWidth = fm.stringWidth(text);
        }
        
        return fontSize;
    }
    
    /**
     * Hover effect for white buttons (theme-dependent)
     */
    private void addWhiteButtonHoverEffect(JButton btn) {
        for (MouseAdapter adapter : btn.getListeners(MouseAdapter.class)) {
            btn.removeMouseListener(adapter);
        }
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(UITheme.WHITE_HOVER);
                btn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(UITheme.WHITE_BG);
                btn.repaint();
            }
        });
    }
    
    /**
     * NEW: Hover effect for fixed white buttons (theme-independent)
     * Always maintains white background and black text
     */
    private void addFixedWhiteButtonHoverEffect(JButton btn) {
        for (MouseAdapter adapter : btn.getListeners(MouseAdapter.class)) {
            btn.removeMouseListener(adapter);
        }
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // FIXED: Slightly darker white on hover, but always white
                btn.setBackground(UITheme.FIXED_WHITE_HOVER);
                // Text stays black
                btn.setForeground(UITheme.FIXED_BLACK_TEXT);
                btn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // FIXED: Return to pure white, text stays black
                btn.setBackground(UITheme.FIXED_WHITE_BG);
                btn.setForeground(UITheme.FIXED_BLACK_TEXT);
                btn.repaint();
            }
        });
    }
    
    /**
     * Hover effect for accent buttons
     */
    private void addAccentButtonHoverEffect(JButton btn) {
        for (MouseAdapter adapter : btn.getListeners(MouseAdapter.class)) {
            btn.removeMouseListener(adapter);
        }
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(UITheme.ACCENT_HOVER);
                btn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(UITheme.ACCENT_BG);
                btn.repaint();
            }
        });
    }
    
    /**
     * Hover effect for green buttons (SettingsPanel)
     */
    private void addGreenButtonHoverEffect(JButton btn) {
        for (MouseAdapter adapter : btn.getListeners(MouseAdapter.class)) {
            btn.removeMouseListener(adapter);
        }
        
        final Color originalGreen = UITheme.LIGHT_PRIMARY_GREEN;
        final Color hoverGreen = UITheme.LIGHTER_PRIMARY_GREEN;
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverGreen);
                btn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(originalGreen);
                btn.repaint();
            }
        });
    }
    
    private void addHoverEffect(JButton btn, Color originalBg) {
        for (MouseAdapter adapter : btn.getListeners(MouseAdapter.class)) {
            btn.removeMouseListener(adapter);
        }
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                Color hoverColor = UITheme.isDarkMode() ? UITheme.DARK_HOVER_COLOR : UITheme.LIGHT_HOVER_COLOR;
                btn.setBackground(hoverColor);
                btn.setForeground(Color.BLACK);
                btn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(originalBg);
                
                if (btn.getText().equals("🌙")) {
                    btn.setForeground(Color.WHITE);
                } else if (btn.getText().equals("☀")) {
                    btn.setForeground(Color.BLACK);
                } else {
                    if (originalBg.equals(UITheme.PRIMARY_GREEN)) {
                        btn.setForeground(Color.WHITE);
                    } else if (UITheme.isDarkMode()) {
                        btn.setForeground(Color.BLACK);
                    } else {
                        btn.setForeground(UITheme.BUTTON_TEXT);
                    }
                }
                btn.repaint();
            }
        });
    }
}
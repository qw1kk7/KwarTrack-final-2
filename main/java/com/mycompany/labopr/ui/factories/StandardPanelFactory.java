package com.mycompany.labopr.ui.factories;

import javax.swing.*;
import java.awt.*;

/**
 * Concrete Factory Method Pattern: StandardPanelFactory
 * Creates standard panels with the app's visual style (including rounded variants)
 */
public class StandardPanelFactory extends PanelFactory {
    
    @Override
    public JPanel createPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(true);
        return panel;
    }
    
    @Override
    public JPanel createPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(true);
        return panel;
    }
    
    @Override
    public JPanel createPanel(Color bgColor) {
        JPanel panel = new JPanel();
        panel.setBackground(bgColor);
        panel.setOpaque(true);
        return panel;
    }
    
    @Override
    public JPanel createPanel(LayoutManager layout, Color bgColor) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(bgColor);
        panel.setOpaque(true);
        return panel;
    }
    
    @Override
    public JPanel createFlowPanel(int align, int hgap, int vgap) {
        JPanel panel = new JPanel(new FlowLayout(align, hgap, vgap));
        panel.setOpaque(false);
        return panel;
    }
    
    @Override
    public JPanel createPaddedPanel(int top, int left, int bottom, int right) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
        return panel;
    }
    
    @Override
    public JPanel createBorderPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createLineBorder(new Color(0xcccccc), 1));
        return panel;
    }
    
    /**
     * Custom JPanel with rounded corners
     */
    private static class RoundedPanel extends JPanel {
        private Color backgroundColor;
        private int cornerRadius;
        
        public RoundedPanel(Color bgColor, int radius) {
            this.backgroundColor = bgColor;
            this.cornerRadius = radius;
            setOpaque(false);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(backgroundColor);
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            g2d.dispose();
            super.paintComponent(g);
        }
        
        @Override
        public void setBackground(Color bg) {
            this.backgroundColor = bg;
            super.setBackground(bg);
        }
    }
}
package com.mycompany.labopr.ui.dialogs;

import com.mycompany.labopr.database.DatabaseDAO;
import com.mycompany.labopr.views.Landing;
import com.mycompany.labopr.ui.builders.DialogBuilder;
import com.mycompany.labopr.ui.factories.ButtonFactory;
import com.mycompany.labopr.ui.factories.PanelFactory;
import com.mycompany.labopr.ui.theme.UITheme;
import javax.swing.*;
import java.awt.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuthDialogs {

    public static void handleLogin(JFrame parent, ButtonFactory buttonFactory, 
                                  PanelFactory panelFactory) {
        JPanel contentPanel = DialogBuilder.createStyledPanel(panelFactory);
        
        JTextField usernameField = new JTextField(20);
        contentPanel.add(DialogBuilder.createFieldRow(panelFactory, "Username:", usernameField));
        contentPanel.add(Box.createVerticalStrut(15));
        
        JPasswordField passwordField = new JPasswordField(20);
        contentPanel.add(DialogBuilder.createFieldRow(panelFactory, "Password:", passwordField));
        contentPanel.add(Box.createVerticalStrut(20));
        
        JLabel infoLabel = new JLabel("Enter your credentials to login");
        infoLabel.setForeground(UITheme.TEXT_COLOR);
        infoLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.ITALIC, 12));
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(infoLabel);
        
        JButton loginBtn = buttonFactory.createButton("Login", new Dimension(120, 40));
        JButton cancelBtn = buttonFactory.createButton("Cancel", new Dimension(120, 40));
        
        JDialog dialog = new DialogBuilder(parent, "Login", buttonFactory, panelFactory)
            .size(450, 300)
            .modal(true)
            .content(contentPanel)
            .buttons(cancelBtn, loginBtn)
            .theme(UITheme.LIGHTER_PRIMARY_GREEN)
            .build();
        
        loginBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a username.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a password.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String passwordHash = hashPassword(password);
            Integer userId = DatabaseDAO.getInstance().authenticateUser(username, passwordHash);
            
            if (userId != null) {
                DatabaseDAO.setCurrentUserId(userId);
                JOptionPane.showMessageDialog(dialog, "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                parent.dispose();
                new Landing().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(dialog, "Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        passwordField.addActionListener(e -> loginBtn.doClick());
        
        dialog.setVisible(true);
    }

    public static void handleSignUp(JFrame parent, ButtonFactory buttonFactory, 
                                    PanelFactory panelFactory) {
        JPanel contentPanel = DialogBuilder.createStyledPanel(panelFactory);
        
        JTextField usernameField = new JTextField(20);
        contentPanel.add(DialogBuilder.createFieldRow(panelFactory, "Choose Username:", usernameField));
        contentPanel.add(Box.createVerticalStrut(15));
        
        JPasswordField passwordField = new JPasswordField(20);
        contentPanel.add(DialogBuilder.createFieldRow(panelFactory, "Choose Password:", passwordField));
        contentPanel.add(Box.createVerticalStrut(15));
        
        JPasswordField confirmPasswordField = new JPasswordField(20);
        contentPanel.add(DialogBuilder.createFieldRow(panelFactory, "Confirm Password:", confirmPasswordField));
        contentPanel.add(Box.createVerticalStrut(20));
        
        JLabel infoLabel = new JLabel("Create a new account to get started");
        infoLabel.setForeground(UITheme.TEXT_COLOR);
        infoLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.ITALIC, 12));
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(infoLabel);
        
        JButton signUpBtn = buttonFactory.createButton("Sign Up", new Dimension(120, 40));
        JButton cancelBtn = buttonFactory.createButton("Cancel", new Dimension(120, 40));
        
        JDialog dialog = new DialogBuilder(parent, "Sign Up", buttonFactory, panelFactory)
            .size(450, 400)
            .modal(true)
            .content(contentPanel)
            .buttons(cancelBtn, signUpBtn)
            .theme(UITheme.LIGHTER_PRIMARY_GREEN)
            .build();
        
        signUpBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a username.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (username.length() < 3) {
                JOptionPane.showMessageDialog(dialog, "Username must be at least 3 characters.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a password.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (password.length() < 6) {
                JOptionPane.showMessageDialog(dialog, "Password must be at least 6 characters.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(dialog, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (DatabaseDAO.getInstance().userExists(username)) {
                JOptionPane.showMessageDialog(dialog, "Username already exists! Please choose another.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String passwordHash = hashPassword(password);
            boolean success = DatabaseDAO.getInstance().createUser(username, passwordHash);
            
            if (success) {
                JOptionPane.showMessageDialog(dialog, "Sign up successful! You can now log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to create account. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        confirmPasswordField.addActionListener(e -> signUpBtn.doClick());
        
        dialog.setVisible(true);
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
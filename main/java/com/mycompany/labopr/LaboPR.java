package com.mycompany.labopr;

import com.mycompany.labopr.views.GUI;
import javax.swing.SwingUtilities;

public class LaboPR {

    public static void main(String[] args) 
    {
        SwingUtilities.invokeLater(()-> new GUI().setVisible(true));
    }
}
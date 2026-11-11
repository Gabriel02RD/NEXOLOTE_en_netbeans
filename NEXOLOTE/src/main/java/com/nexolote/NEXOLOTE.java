package com.nexolote;

import java.awt.Color;
import java.awt.Font;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class NEXOLOTE {
    public static void main(String[] args) {
        Conexion.interfazActiva = true;
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.out.println("No se pudo aplicar el estilo del sistema.");
            }

            Color azulTabla = new Color(28, 79, 92);
            UIManager.put("TableHeader.background", azulTabla);
            UIManager.put("TableHeader.foreground", Color.WHITE);
            UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 13));
            UIManager.put("TableHeader.cellBorder", javax.swing.BorderFactory.createLineBorder(new Color(22, 58, 67)));

            new VentanaPrincipal().setVisible(true);
        });
    }
}
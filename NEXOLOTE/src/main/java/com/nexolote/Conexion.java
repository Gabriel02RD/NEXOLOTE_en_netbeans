package com.nexolote;

import java.awt.Component;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class Conexion {
    // Tu conexión directa (con la contraseña incluida)
    private static final String URL = "jdbc:mysql://localhost:3306/nexolote?useSSL=false&serverTimezone=America/Bogota&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";
    private static final String USUARIO = "root";
    private static final String CLAVE = "Gabriel0224."; 

    public static boolean interfazActiva = false;

    public static Connection conectar() {
        try {
            return DriverManager.getConnection(URL, USUARIO, CLAVE);
        } catch (SQLException e) {
            System.err.println("No se pudo conectar con MySQL: " + e.getMessage());
            if (interfazActiva) {
                JOptionPane.showMessageDialog(null, 
                    "No se pudo conectar a la base de datos.\n\n"
                    + "Verifica que MySQL esté encendido y que la contraseña sea correcta.\n\n"
                    + "Detalle: " + e.getMessage(), 
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }
    }

    public static void mostrarError(Component padre, SQLException e) {
        System.err.println("SQL Error [" + e.getErrorCode() + "]: " + e.getMessage());
        JOptionPane.showMessageDialog(padre, 
            "Ocurrió un error en la base de datos:\n" + e.getMessage(), 
            "Error", JOptionPane.ERROR_MESSAGE);
    }
}
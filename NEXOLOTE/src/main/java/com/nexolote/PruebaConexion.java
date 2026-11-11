
package com.nexolote;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class PruebaConexion {

    public static void main(String[] args) {

        try (Connection conexion = Conexion.conectar()) {

            if (conexion == null) {
                System.out.println(
                        "Verifica MySQL, la contraseña y el controlador."
                );
                return;
            }

            DatabaseMetaData datos = conexion.getMetaData();

            System.out.println("================================");
            System.out.println("NEXOLOTE - PRUEBA DE CONEXION");
            System.out.println("================================");
            System.out.println("Base de datos: "
                    + conexion.getCatalog());
            System.out.println("Servidor: "
                    + datos.getDatabaseProductName());
            System.out.println("Version: "
                    + datos.getDatabaseProductVersion());
            System.out.println("Conexion: CORRECTA");

        } catch (SQLException e) {
            System.err.println(
                    "Error durante la prueba: " + e.getMessage()
            );
        }
    }
}

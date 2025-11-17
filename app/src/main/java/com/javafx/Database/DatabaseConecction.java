package com.javafx.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConecction {
    private static final String URL = "jdbc:mysql://localhost:3306/proyecto_db";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    private static Connection conexion = null;

    /**
     * Obtiene la conexión a la base de datos (Singleton)
     */
    public static Connection getConnection() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conexion = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver MySQL no encontrado", e);
            }
        }
        return conexion;
    }

    /**
     * Cierra la conexión a la base de datos
     */
    public static void closeConnection() {
        if (conexion != null) {
            try {
                conexion.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
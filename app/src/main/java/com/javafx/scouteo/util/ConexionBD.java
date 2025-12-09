package com.javafx.scouteo.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionBD {
    private static String URL;
    private static String USER;
    private static String PASSWORD;
    private static String DRIVER;

    private static Connection conexion = null;

    static {
        cargarConfiguracion();
    }

    /**
     * Carga la configuración de la base de datos desde bbdd.properties
     */
    private static void cargarConfiguracion() {
        Properties props = new Properties();
        try (InputStream input = ConexionBD.class.getClassLoader().getResourceAsStream("bbdd.properties")) {
            if (input == null) {
                System.err.println("No se pudo encontrar el archivo bbdd.properties");
                // Valores por defecto
                URL = "jdbc:mysql://localhost:3306/scouteo_db";
                USER = "root";
                PASSWORD = "root";
                DRIVER = "com.mysql.cj.jdbc.Driver";
                return;
            }

            props.load(input);

            String host = props.getProperty("db.host", "localhost");
            String port = props.getProperty("db.port", "3306");
            String dbName = props.getProperty("db.name", "scouteo_db");

            URL = "jdbc:mysql://" + host + ":" + port + "/" + dbName;
            USER = props.getProperty("db.user", "root");
            PASSWORD = props.getProperty("db.password", "root");
            DRIVER = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");

        } catch (IOException e) {
            System.err.println("Error al cargar el archivo bbdd.properties: " + e.getMessage());
            // Valores por defecto en caso de error
            URL = "jdbc:mysql://localhost:3306/scouteo_db";
            USER = "root";
            PASSWORD = "root";
            DRIVER = "com.mysql.cj.jdbc.Driver";
        }
    }

    /**
     * Obtiene la conexión a la base de datos (Singleton)
     */
    public static Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed()) {
                try {
                    Class.forName(DRIVER);
                    conexion = DriverManager.getConnection(URL, USER, PASSWORD);
                } catch (ClassNotFoundException e) {
                    throw new SQLException("Driver MySQL no encontrado: " + DRIVER, e);
                }
            }
            return conexion;
        } catch (SQLException e) {
            System.err.println("Error al conectar con la base de datos:");
            System.err.println("URL: " + URL);
            System.err.println("Usuario: " + USER);
            e.printStackTrace();
            return null;
        }
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

    /**
     * Verifica si la conexión a la base de datos es válida
     * @return true si la conexión es válida, false en caso contrario
     */
    public static boolean isConexionValida() {
        Connection conn = getConexion();
        if (conn == null) {
            return false;
        }
        try {
            return !conn.isClosed() && conn.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
}

package com.javafx.scouteo.util;

import com.javafx.Database.DatabaseConecction;
import java.sql.Connection;
import java.sql.SQLException;

public class ConexionBD {
    public static Connection getConexion() {
        try {
            return DatabaseConecction.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}

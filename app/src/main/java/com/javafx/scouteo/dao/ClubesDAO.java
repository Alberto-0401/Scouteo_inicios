package com.javafx.scouteo.dao;

import com.javafx.scouteo.util.ConexionBD;

import java.sql.*;

public class ClubesDAO {

    /**
     * Crea un nuevo club y devuelve su ID generado, o -1 si falla.
     */
    public int insertar(String nombre) {
        String sql = "INSERT INTO clubes (nombre, activo) VALUES (?, 1)";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, nombre);
            if (pstmt.executeUpdate() > 0) {
                ResultSet keys = pstmt.getGeneratedKeys();
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}

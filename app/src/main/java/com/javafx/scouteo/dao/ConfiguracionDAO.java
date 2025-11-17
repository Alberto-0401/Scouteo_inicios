package com.javafx.scouteo.dao;

import com.javafx.scouteo.model.Configuracion;
import com.javafx.scouteo.util.ConexionBD;
import java.sql.*;

public class ConfiguracionDAO {

    public Configuracion obtener() {
        String sql = "SELECT * FROM configuracion LIMIT 1";
        Configuracion config = null;

        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                config = new Configuracion();
                config.setIdConfig(rs.getInt("id_config"));
                config.setNombreClub(rs.getString("nombre_club"));
                config.setLocalidad(rs.getString("localidad"));
                config.setPresidente(rs.getString("presidente"));
                config.setEmail(rs.getString("email"));
                config.setTelefono(rs.getString("telefono"));
                config.setEscudo(rs.getString("escudo"));
                config.setTemporadaActual(rs.getString("temporada_actual"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return config;
    }

    public boolean actualizar(Configuracion config) {
        String sql = "UPDATE configuracion SET nombre_club = ?, localidad = ?, presidente = ?, email = ?, telefono = ?, temporada_actual = ? WHERE id_config = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, config.getNombreClub());
            pstmt.setString(2, config.getLocalidad());
            pstmt.setString(3, config.getPresidente());
            pstmt.setString(4, config.getEmail());
            pstmt.setString(5, config.getTelefono());
            pstmt.setString(6, config.getTemporadaActual());
            pstmt.setInt(7, config.getIdConfig());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

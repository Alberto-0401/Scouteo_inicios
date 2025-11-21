package com.javafx.scouteo.dao;

import com.javafx.scouteo.model.Partido;
import com.javafx.scouteo.util.ConexionBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PartidoDAO {

    public List<Partido> obtenerTodos() {
        List<Partido> partidos = new ArrayList<>();
        String sql = "SELECT * FROM partidos ORDER BY fecha_partido DESC";

        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Partido partido = new Partido();
                partido.setIdPartido(rs.getInt("id_partido"));
                partido.setFechaPartido(rs.getDate("fecha_partido").toLocalDate());
                partido.setRival(rs.getString("rival"));
                partido.setResultado(rs.getString("resultado"));
                partido.setLocalVisitante(rs.getString("local_visitante"));
                partido.setObservaciones(rs.getString("observaciones"));
                partidos.add(partido);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return partidos;
    }

    public Partido obtenerPorId(int id) {
        String sql = "SELECT * FROM partidos WHERE id_partido = ?";
        Partido partido = null;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                partido = new Partido();
                partido.setIdPartido(rs.getInt("id_partido"));
                partido.setFechaPartido(rs.getDate("fecha_partido").toLocalDate());
                partido.setRival(rs.getString("rival"));
                partido.setResultado(rs.getString("resultado"));
                partido.setLocalVisitante(rs.getString("local_visitante"));
                partido.setObservaciones(rs.getString("observaciones"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return partido;
    }

    public int contarTotal() {
        String sql = "SELECT COUNT(*) as total FROM partidos";
        int total = 0;

        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                total = rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return total;
    }

}

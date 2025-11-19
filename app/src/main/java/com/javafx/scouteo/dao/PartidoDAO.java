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

    public boolean insertar(Partido partido) {
        String sql = "INSERT INTO partidos (fecha_partido, rival, resultado, local_visitante, observaciones) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(partido.getFechaPartido()));
            pstmt.setString(2, partido.getRival());
            pstmt.setString(3, partido.getResultado());
            pstmt.setString(4, partido.getLocalVisitante());
            pstmt.setString(5, partido.getObservaciones());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizar(Partido partido) {
        String sql = "UPDATE partidos SET fecha_partido = ?, rival = ?, resultado = ?, local_visitante = ?, observaciones = ? WHERE id_partido = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(partido.getFechaPartido()));
            pstmt.setString(2, partido.getRival());
            pstmt.setString(3, partido.getResultado());
            pstmt.setString(4, partido.getLocalVisitante());
            pstmt.setString(5, partido.getObservaciones());
            pstmt.setInt(6, partido.getIdPartido());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM partidos WHERE id_partido = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

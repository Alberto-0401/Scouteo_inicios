package com.javafx.scouteo.dao;

import com.javafx.scouteo.model.EstadisticaPartido;
import com.javafx.scouteo.util.ConexionBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EstadisticaPartidoDAO {

    public Integer insertar(EstadisticaPartido estadistica) {
        String sql = "INSERT INTO estadisticas_partido (goles, asistencias, paradas, recuperaciones, " +
                     "tarjetas_amarillas, tarjetas_rojas, minutos_jugados, valoracion, observaciones) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, estadistica.getGoles());
            pstmt.setInt(2, estadistica.getAsistencias());
            if (estadistica.getParadas() != null) {
                pstmt.setInt(3, estadistica.getParadas());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            if (estadistica.getRecuperaciones() != null) {
                pstmt.setInt(4, estadistica.getRecuperaciones());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            pstmt.setInt(5, estadistica.getTarjetasAmarillas());
            pstmt.setInt(6, estadistica.getTarjetasRojas());
            pstmt.setInt(7, estadistica.getMinutosJugados());
            pstmt.setDouble(8, estadistica.getValoracion());
            pstmt.setString(9, estadistica.getObservaciones());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public EstadisticaPartido obtenerPorId(int idEstadistica) {
        String sql = "SELECT * FROM estadisticas_partido WHERE id_estadistica = ?";
        EstadisticaPartido estadistica = null;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idEstadistica);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                estadistica = new EstadisticaPartido();
                estadistica.setIdEstadistica(rs.getInt("id_estadistica"));
                estadistica.setGoles(rs.getInt("goles"));
                estadistica.setAsistencias(rs.getInt("asistencias"));
                estadistica.setParadas(rs.getObject("paradas", Integer.class));
                estadistica.setRecuperaciones(rs.getObject("recuperaciones", Integer.class));
                estadistica.setTarjetasAmarillas(rs.getInt("tarjetas_amarillas"));
                estadistica.setTarjetasRojas(rs.getInt("tarjetas_rojas"));
                estadistica.setMinutosJugados(rs.getInt("minutos_jugados"));
                estadistica.setValoracion(rs.getDouble("valoracion"));
                estadistica.setObservaciones(rs.getString("observaciones"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return estadistica;
    }

    public int contarTotalGoles() {
        String sql = "SELECT SUM(goles) as total_goles FROM estadisticas_partido";
        int total = 0;

        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                total = rs.getInt("total_goles");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return total;
    }
}

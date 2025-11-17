package com.javafx.scouteo.dao;

import com.javafx.scouteo.controller.PartidosController;
import com.javafx.scouteo.util.ConexionBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EstadisticaPartidoDAO {

    public List<PartidosController.EstadisticaPartido> obtenerPorJugador(int idJugador) {
        List<PartidosController.EstadisticaPartido> estadisticas = new ArrayList<>();
        String sql = "SELECT p.fecha_partido, p.rival, p.resultado, " +
                     "e.minutos_jugados, e.goles, e.asistencias, e.tarjetas_amarillas, e.tarjetas_rojas " +
                     "FROM estadisticas_partido e " +
                     "INNER JOIN partidos p ON e.id_partido = p.id_partido " +
                     "WHERE e.id_jugador = ? " +
                     "ORDER BY p.fecha_partido DESC";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idJugador);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String fecha = rs.getDate("fecha_partido").toString();
                String rival = rs.getString("rival");
                String resultado = rs.getString("resultado");
                Integer minutos = rs.getInt("minutos_jugados");
                Integer goles = rs.getInt("goles");
                Integer asistencias = rs.getInt("asistencias");
                Integer tarjetasA = rs.getInt("tarjetas_amarillas");
                Integer tarjetasR = rs.getInt("tarjetas_rojas");

                estadisticas.add(new PartidosController.EstadisticaPartido(
                    fecha, rival, resultado, minutos, goles, asistencias, tarjetasA, tarjetasR
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return estadisticas;
    }

    public int[] obtenerTotales(int idJugador) {
        String sql = "SELECT SUM(goles) as total_goles, SUM(asistencias) as total_asistencias, COUNT(*) as partidos FROM estadisticas_partido WHERE id_jugador = ?";
        int[] totales = new int[3]; // [goles, asistencias, partidos]

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idJugador);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                totales[0] = rs.getInt("total_goles");
                totales[1] = rs.getInt("total_asistencias");
                totales[2] = rs.getInt("partidos");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return totales;
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

package com.javafx.scouteo.dao;

import com.javafx.scouteo.controller.RankingController;
import com.javafx.scouteo.util.ConexionBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RankingDAO {

    public List<RankingController.JugadorRanking> obtenerRanking(String ordenarPor, String posicionFiltro) {
        List<RankingController.JugadorRanking> ranking = new ArrayList<>();

        String columnaOrden = switch (ordenarPor) {
            case "Asistencias" -> "total_asistencias";
            case "Partidos Jugados" -> "total_partidos";
            case "Promedio Goles" -> "promedio_goles";
            default -> "total_goles";
        };

        String sql = "SELECT j.nombre, j.apellidos, j.dorsal, j.posicion, " +
                     "COUNT(DISTINCT jp.id_partido) as total_partidos, " +
                     "COALESCE(SUM(e.goles), 0) as total_goles, " +
                     "COALESCE(SUM(e.asistencias), 0) as total_asistencias, " +
                     "CASE WHEN COUNT(DISTINCT jp.id_partido) > 0 THEN COALESCE(SUM(e.goles), 0) / COUNT(DISTINCT jp.id_partido) ELSE 0 END as promedio_goles " +
                     "FROM jugadores j " +
                     "LEFT JOIN jugadores_partidos jp ON j.id_jugador = jp.id_jugador " +
                     "LEFT JOIN estadisticas_partido e ON jp.id_estadistica = e.id_estadistica " +
                     (posicionFiltro.equals("Todas") ? "" : "WHERE j.posicion = ? ") +
                     "GROUP BY j.id_jugador, j.nombre, j.apellidos, j.dorsal, j.posicion " +
                     "ORDER BY " + columnaOrden + " DESC";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (!posicionFiltro.equals("Todas")) {
                pstmt.setString(1, posicionFiltro);
            }

            ResultSet rs = pstmt.executeQuery();
            int posicion = 1;

            while (rs.next()) {
                String nombreCompleto = rs.getString("nombre") + " " + rs.getString("apellidos");
                Integer dorsal = rs.getInt("dorsal");
                String posicionJuego = rs.getString("posicion");
                Integer partidos = rs.getInt("total_partidos");
                Integer goles = rs.getInt("total_goles");
                Integer asistencias = rs.getInt("total_asistencias");
                Double promedio = rs.getDouble("promedio_goles");

                ranking.add(new RankingController.JugadorRanking(
                    posicion++, nombreCompleto, dorsal, posicionJuego,
                    partidos, goles, asistencias, promedio
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ranking;
    }
}

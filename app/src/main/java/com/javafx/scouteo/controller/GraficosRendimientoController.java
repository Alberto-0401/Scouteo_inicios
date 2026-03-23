package com.javafx.scouteo.controller;

import com.javafx.scouteo.util.ConexionBD;
import com.javafx.scouteo.util.SesionUsuario;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GraficosRendimientoController {

    @FXML private PieChart pieChartPosicion;
    @FXML private BarChart<String, Number> barChartGoleadores;
    @FXML private CategoryAxis xAxisGoleadores;
    @FXML private NumberAxis yAxisGoleadores;
    @FXML private Label lblEstado;

    @FXML
    public void initialize() {
        cargarGraficoPosiciones();
        cargarGraficoGoleadores();
        lblEstado.setText("Datos cargados desde la base de datos");
        lblEstado.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 11px; -fx-font-weight: bold;");
    }

    /** Devuelve club_id del usuario actual, o null si no hay sesión. */
    private Integer getClubId() {
        SesionUsuario sesion = SesionUsuario.getInstance();
        if (sesion.haySesionActiva() && sesion.getUsuarioActual().getClubId() != null) {
            return sesion.getUsuarioActual().getClubId();
        }
        return null;
    }

    /**
     * Fragmento SQL extra para filtrar por equipo cuando el usuario es entrenador.
     * Devuelve "AND e.id = X" o "" (sin filtro adicional).
     */
    private String filtroEquipo() {
        SesionUsuario sesion = SesionUsuario.getInstance();
        if (!sesion.esEntrenador()) return "";
        // Buscar el equipo del entrenador directamente en BD
        Integer userId = sesion.getUsuarioActual().getId();
        try (java.sql.Connection con = ConexionBD.getConexion();
             java.sql.PreparedStatement ps = con.prepareStatement(
                 "SELECT equipo_id FROM equipos_entrenadores WHERE usuario_id = ? AND activo = 1 LIMIT 1")) {
            ps.setInt(1, userId);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) return " AND e.id = " + rs.getInt("equipo_id");
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void cargarGraficoPosiciones() {
        Integer clubId = getClubId();
        if (clubId == null) return;

        String sql = """
            SELECT
              CASE
                WHEN j.posicion = 'portero' THEN 'Porteros'
                WHEN j.posicion IN ('defensa_central','lateral_derecho','lateral_izquierdo') THEN 'Defensas'
                WHEN j.posicion IN ('mediocentro','medio_derecho','medio_izquierdo','mediapunta') THEN 'Medios'
                WHEN j.posicion IN ('extremo_derecho','extremo_izquierdo','delantero_centro') THEN 'Delanteros'
                ELSE 'Otros'
              END AS grupo,
              COUNT(*) AS total
            FROM jugadores j
            JOIN equipos e ON j.equipo_id = e.id
            WHERE e.club_id = ? AND j.estado != 'baja'
            """ + filtroEquipo() + """

            GROUP BY grupo
            ORDER BY total DESC
            """;

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, clubId);
            ResultSet rs = ps.executeQuery();

            ObservableList<PieChart.Data> datos = FXCollections.observableArrayList();
            while (rs.next()) {
                long total = rs.getLong("total");
                datos.add(new PieChart.Data(rs.getString("grupo") + " (" + total + ")", total));
            }
            pieChartPosicion.setData(datos);
            pieChartPosicion.setTitle("Plantilla activa por posición");

        } catch (SQLException e) {
            e.printStackTrace();
            lblEstado.setText("Error al cargar gráfico de posiciones: " + e.getMessage());
            lblEstado.setStyle("-fx-text-fill: #F44336; -fx-font-size: 11px;");
        }
    }

    private void cargarGraficoGoleadores() {
        Integer clubId = getClubId();
        if (clubId == null) return;

        String sql = """
            SELECT
              CONCAT(j.nombre, ' ', j.apellidos) AS jugador,
              COALESCE(SUM(a.goles), 0) AS goles,
              COALESCE(SUM(a.asistencias), 0) AS asistencias
            FROM jugadores j
            JOIN equipos e ON j.equipo_id = e.id
            LEFT JOIN alineaciones a ON a.jugador_id = j.id
            WHERE e.club_id = ? AND j.estado != 'baja'
            """ + filtroEquipo() + """

            GROUP BY j.id, j.nombre, j.apellidos
            ORDER BY goles DESC, asistencias DESC
            LIMIT 10
            """;

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, clubId);
            ResultSet rs = ps.executeQuery();

            XYChart.Series<String, Number> serieGoles = new XYChart.Series<>();
            serieGoles.setName("Goles");
            XYChart.Series<String, Number> serieAsistencias = new XYChart.Series<>();
            serieAsistencias.setName("Asistencias");

            while (rs.next()) {
                String nombre = abreviarNombre(rs.getString("jugador"));
                serieGoles.getData().add(new XYChart.Data<>(nombre, rs.getInt("goles")));
                serieAsistencias.getData().add(new XYChart.Data<>(nombre, rs.getInt("asistencias")));
            }

            barChartGoleadores.getData().add(serieGoles);
            barChartGoleadores.getData().add(serieAsistencias);
            barChartGoleadores.setTitle("Top 10 — Goles y asistencias");

        } catch (SQLException e) {
            e.printStackTrace();
            lblEstado.setText("Error al cargar gráfico de goleadores: " + e.getMessage());
            lblEstado.setStyle("-fx-text-fill: #F44336; -fx-font-size: 11px;");
        }
    }

    /** Abrevia nombres largos para que quepan en el eje X */
    private String abreviarNombre(String nombreCompleto) {
        if (nombreCompleto == null) return "";
        String[] partes = nombreCompleto.split(" ");
        if (partes.length >= 2 && nombreCompleto.length() > 13) {
            return partes[0] + " " + partes[partes.length - 1].charAt(0) + ".";
        }
        return nombreCompleto;
    }
}

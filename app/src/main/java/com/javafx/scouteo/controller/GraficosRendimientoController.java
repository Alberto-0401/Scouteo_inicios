package com.javafx.scouteo.controller;

import com.google.gson.JsonObject;
import com.javafx.scouteo.dao.EquipoDAO;
import com.javafx.scouteo.dao.JugadorDAO;
import com.javafx.scouteo.dao.PartidoDAO;
import com.javafx.scouteo.model.Jugador;
import com.javafx.scouteo.model.Partido;
import com.javafx.scouteo.util.ApiClient;
import com.javafx.scouteo.util.SesionUsuario;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class GraficosRendimientoController {

    @FXML private PieChart pieChartPosicion;
    @FXML private BarChart<String, Number> barChartGoleadores;
    @FXML private CategoryAxis xAxisGoleadores;
    @FXML private NumberAxis yAxisGoleadores;
    @FXML private Label lblEstado;

    private final JugadorDAO jugadorDAO = new JugadorDAO();
    private final PartidoDAO partidoDAO = new PartidoDAO();
    private final EquipoDAO  equipoDAO  = new EquipoDAO();

    @FXML
    public void initialize() {
        lblEstado.setText("Cargando gráficos...");
        lblEstado.setStyle("-fx-text-fill: #FF9800; -fx-font-size: 11px; -fx-font-weight: bold;");
        cargarGraficos();
    }

    private Integer getClubId() {
        SesionUsuario s = SesionUsuario.getInstance();
        return (s.haySesionActiva() && s.getUsuarioActual().getClubId() != null)
                ? s.getUsuarioActual().getClubId() : null;
    }

    private Integer getEquipoId() {
        SesionUsuario s = SesionUsuario.getInstance();
        if (!s.esEntrenador()) return null;
        var equipos = equipoDAO.obtenerPorEntrenador(s.getUsuarioActual().getId());
        return equipos.isEmpty() ? null : equipos.get(0).getId();
    }

    private void cargarGraficos() {
        Thread t = new Thread(() -> {
            Integer clubId   = getClubId();
            Integer equipoId = getEquipoId();
            if (clubId == null) {
                Platform.runLater(() -> {
                    lblEstado.setText("Sin sesión activa");
                    lblEstado.setStyle("-fx-text-fill: #F44336; -fx-font-size: 11px;");
                });
                return;
            }

            // ── 1. Jugadores y partidos ─────────────────────────────────────
            List<Jugador> jugadores = (equipoId != null)
                    ? jugadorDAO.obtenerPorEquipo(equipoId)
                    : jugadorDAO.obtenerPorClub(clubId);
            List<Partido> partidos  = (equipoId != null)
                    ? partidoDAO.obtenerPorEquipo(equipoId)
                    : partidoDAO.obtenerPorClub(clubId);

            Map<Integer, Jugador> jugMap = jugadores.stream()
                    .filter(j -> j.getId() != null)
                    .collect(Collectors.toMap(Jugador::getId, j -> j, (a, b) -> a));

            // ── 2. Distribución por posición (PieChart) ─────────────────────
            Map<String, Long> distPosicion = jugadores.stream()
                    .filter(j -> !"baja".equals(j.getEstado()))
                    .collect(Collectors.groupingBy(j -> {
                        switch (j.getGrupoPosicion()) {
                            case "POR": return "Porteros";
                            case "DEF": return "Defensas";
                            case "MED": return "Medios";
                            default:    return "Delanteros";
                        }
                    }, Collectors.counting()));

            // ── 3. Alineaciones en paralelo → goles + asistencias ───────────
            ConcurrentHashMap<Integer, int[]> agg = new ConcurrentHashMap<>(); // [goles, asist]
            ApiClient api = ApiClient.getInstance();
            ExecutorService exec = Executors.newFixedThreadPool(8);
            for (Partido p : partidos) {
                final int pid = p.getId();
                exec.submit(() -> {
                    String json = api.get("/alineaciones/partido/" + pid);
                    if (json == null) return;
                    for (JsonObject o : api.fromJsonList(json, JsonObject.class)) {
                        if (!o.has("jugadorId") || o.get("jugadorId").isJsonNull()) continue;
                        int jid = o.get("jugadorId").getAsInt();
                        int goles = o.has("goles")        && !o.get("goles").isJsonNull()        ? o.get("goles").getAsInt()        : 0;
                        int asist = o.has("asistencias")  && !o.get("asistencias").isJsonNull()  ? o.get("asistencias").getAsInt()  : 0;
                        agg.merge(jid, new int[]{goles, asist}, (ex, in) -> new int[]{ex[0]+in[0], ex[1]+in[1]});
                    }
                });
            }
            exec.shutdown();
            try { exec.awaitTermination(30, TimeUnit.SECONDS); } catch (InterruptedException ignored) {}

            // Top 10 por goles (luego asistencias como desempate)
            List<Map.Entry<Integer, int[]>> top10 = agg.entrySet().stream()
                    .filter(e -> jugMap.containsKey(e.getKey()))
                    .sorted((a, b) -> {
                        int cmp = Integer.compare(b.getValue()[0], a.getValue()[0]);
                        return cmp != 0 ? cmp : Integer.compare(b.getValue()[1], a.getValue()[1]);
                    })
                    .limit(10)
                    .collect(Collectors.toList());

            // ── 4. Actualizar UI ────────────────────────────────────────────
            Platform.runLater(() -> {
                // PieChart
                ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
                distPosicion.forEach((pos, cnt) ->
                        pieData.add(new PieChart.Data(pos + " (" + cnt + ")", cnt)));
                pieChartPosicion.setData(pieData);
                pieChartPosicion.setTitle("Plantilla activa por posición");

                // BarChart
                XYChart.Series<String, Number> serieGoles = new XYChart.Series<>();
                serieGoles.setName("Goles");
                XYChart.Series<String, Number> serieAsist = new XYChart.Series<>();
                serieAsist.setName("Asistencias");

                for (Map.Entry<Integer, int[]> e : top10) {
                    Jugador j   = jugMap.get(e.getKey());
                    String nombre = abreviarNombre(j.getNombreCompleto());
                    serieGoles.getData().add(new XYChart.Data<>(nombre, e.getValue()[0]));
                    serieAsist.getData().add(new XYChart.Data<>(nombre, e.getValue()[1]));
                }
                barChartGoleadores.getData().add(serieGoles);
                barChartGoleadores.getData().add(serieAsist);
                barChartGoleadores.setTitle("Top 10 — Goles y asistencias");

                String estado = top10.isEmpty() ? "Sin datos de alineaciones aún" : "Datos cargados correctamente";
                String color  = top10.isEmpty() ? "#FF9800" : "#4CAF50";
                lblEstado.setText(estado);
                lblEstado.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 11px; -fx-font-weight: bold;");
            });

        }, "graficos-loader");
        t.setDaemon(true);
        t.start();
    }

    private String abreviarNombre(String nombreCompleto) {
        if (nombreCompleto == null) return "";
        String[] partes = nombreCompleto.split(" ");
        if (partes.length >= 2 && nombreCompleto.length() > 13)
            return partes[0] + " " + partes[partes.length - 1].charAt(0) + ".";
        return nombreCompleto;
    }
}

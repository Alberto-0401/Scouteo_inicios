package com.javafx.scouteo.controller;

import com.google.gson.JsonObject;
import com.javafx.scouteo.model.Equipo;
import com.javafx.scouteo.model.Partido;
import com.javafx.scouteo.model.Jugador;
import com.javafx.scouteo.dao.PartidoDAO;
import com.javafx.scouteo.dao.JugadorDAO;
import com.javafx.scouteo.util.ApiClient;
import com.javafx.scouteo.util.SesionUsuario;
import com.javafx.scouteo.utils.StageUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConvocatoriasController {

    @FXML private TableView<ConvocatoriaItem> tablaConvocatorias;
    @FXML private TableColumn<ConvocatoriaItem, String>    colPartido;
    @FXML private TableColumn<ConvocatoriaItem, LocalDate> colFecha;
    @FXML private TableColumn<ConvocatoriaItem, String>    colJugador;
    @FXML private TableColumn<ConvocatoriaItem, Integer>   colDorsal;
    @FXML private TableColumn<ConvocatoriaItem, String>    colPosicion;
    @FXML private TableColumn<ConvocatoriaItem, String>    colTitular;
    @FXML private TableColumn<ConvocatoriaItem, String>    colConvocado;
    @FXML private TableColumn<ConvocatoriaItem, Void>      colAcciones;
    @FXML private ComboBox<String> cmbPartido;
    @FXML private Label lblTotal;

    private ObservableList<ConvocatoriaItem> listaConvocatorias;
    private final PartidoDAO partidoDAO = new PartidoDAO();
    private final JugadorDAO jugadorDAO = new JugadorDAO();
    private List<Partido> listaPartidos;
    private Equipo equipoActivo;
    private DashboardController dashboardController;
    // Flag para evitar que setValue en el combo dispare filtrar() en bucle
    private boolean actualizandoCombo = false;

    public void setDashboardController(DashboardController dc) {
        this.dashboardController = dc;
    }

    public void setEquipoActivo(Equipo equipo) {
        this.equipoActivo = equipo;
        cargarDatosAsync(null);
    }

    @FXML
    public void initialize() {
        // Solo estructura de la tabla; los datos los carga setEquipoActivo en background
        configurarTabla();
    }

    private void configurarTabla() {
        colPartido.setCellValueFactory(new PropertyValueFactory<>("partido"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colJugador.setCellValueFactory(new PropertyValueFactory<>("jugador"));
        colDorsal.setCellValueFactory(new PropertyValueFactory<>("dorsal"));
        colPosicion.setCellValueFactory(new PropertyValueFactory<>("posicion"));
        colConvocado.setCellValueFactory(new PropertyValueFactory<>("convocado"));
        colTitular.setCellValueFactory(new PropertyValueFactory<>("titular"));

        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar   = new Button("\u270E");
            private final Button btnEliminar = new Button("\u2716");
            private final HBox contenedor    = new HBox(5, btnEditar, btnEliminar);
            {
                btnEditar.setOnAction(e -> editarConvocatoria(getTableView().getItems().get(getIndex())));
                btnEliminar.setOnAction(e -> eliminarConvocatoria(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : contenedor);
            }
        });
    }

    /**
     * Carga partidos y convocatorias en un hilo de fondo para no congelar la UI.
     * @param idPartidoFiltro null = todos los partidos, distinto de null = solo ese partido.
     */
    private void cargarDatosAsync(Integer idPartidoFiltro) {
        int clubId = SesionUsuario.getInstance().getUsuarioActual().getClubId();
        int equipoId = equipoActivo != null ? equipoActivo.getId() : -1;

        System.err.println("[CONV] cargarDatosAsync — equipoId=" + equipoId + " clubId=" + clubId + " filtro=" + idPartidoFiltro);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                // ── 1. Obtener partidos (solo si no los tenemos ya) ──────────
                if (listaPartidos == null || idPartidoFiltro == null) {
                    listaPartidos = (equipoId > 0)
                            ? partidoDAO.obtenerPorEquipo(equipoId)
                            : partidoDAO.obtenerPorClub(clubId);
                    System.err.println("[CONV] Partidos cargados: " + (listaPartidos == null ? "null" : listaPartidos.size()) + " (equipoId=" + equipoId + ")");
                    if (listaPartidos != null) {
                        listaPartidos.forEach(p ->
                            System.err.println("[CONV]   partido id=" + p.getId() + " rival=" + p.getRival()));
                    }
                }

                // ── 2. Determinar qué partidos consultar ─────────────────────
                List<Partido> partidos = (idPartidoFiltro != null)
                        ? listaPartidos.stream().filter(p -> p.getId() == idPartidoFiltro).toList()
                        : listaPartidos;

                // ── 3. Obtener convocatorias en paralelo (8 hilos) ───────────
                ApiClient api = ApiClient.getInstance();
                List<ConvocatoriaItem> items = Collections.synchronizedList(new ArrayList<>());
                ExecutorService pool = Executors.newFixedThreadPool(Math.min(Math.max(partidos.size(), 1), 8));
                for (Partido p : partidos) {
                    pool.submit(() -> {
                        String json = api.get("/convocatorias/partido/" + p.getId());
                        System.err.println("[CONV]   /convocatorias/partido/" + p.getId()
                                + " -> " + (json == null ? "NULL/ERROR" : json.substring(0, Math.min(80, json.length()))));
                        if (json == null) return;
                        List<ConvocatoriaItem> batch = new ArrayList<>();
                        for (JsonObject o : api.fromJsonList(json, JsonObject.class)) {
                            int id       = o.get("id").getAsInt();
                            int pId      = o.has("partidoId") && !o.get("partidoId").isJsonNull() ? o.get("partidoId").getAsInt() : p.getId();
                            int jId      = o.has("jugadorId") && !o.get("jugadorId").isJsonNull() ? o.get("jugadorId").getAsInt() : 0;
                            String nombre = nvl(getStr(o, "jugadorNombre")) + " " + nvl(getStr(o, "jugadorApellidos"));
                            Integer dorsal = o.has("jugadorDorsal") && !o.get("jugadorDorsal").isJsonNull() ? o.get("jugadorDorsal").getAsInt() : null;
                            LocalDate fecha = p.getFechaHora() != null ? p.getFechaHora().toLocalDate() : null;
                            batch.add(new ConvocatoriaItem(id, pId, jId, p.getRival(), fecha,
                                    nombre.trim(), dorsal, getStr(o, "jugadorPosicion"),
                                    getStr(o, "tipo"), getStr(o, "motivoBaja")));
                        }
                        items.addAll(batch);
                    });
                }
                pool.shutdown();
                try { pool.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS); }
                catch (InterruptedException ie) { Thread.currentThread().interrupt(); }

                System.err.println("[CONV] Total items: " + items.size());

                // ── 4. Actualizar UI en el hilo JavaFX ───────────────────────
                Platform.runLater(() -> {
                    try {
                        System.err.println("[CONV] Platform.runLater — items=" + items.size()
                                + " tabla=" + (tablaConvocatorias != null ? "OK" : "NULL")
                                + " combo=" + (cmbPartido != null ? "OK" : "NULL"));
                        if (idPartidoFiltro == null) {
                            actualizandoCombo = true;
                            cmbPartido.getItems().clear();
                            cmbPartido.getItems().add("Todos los partidos");
                            for (Partido p : listaPartidos) cmbPartido.getItems().add(textoPartido(p));
                            cmbPartido.setValue("Todos los partidos");
                            actualizandoCombo = false;
                        }
                        listaConvocatorias = FXCollections.observableArrayList(items);
                        tablaConvocatorias.setItems(listaConvocatorias);
                        tablaConvocatorias.setPlaceholder(new Label(
                                items.isEmpty() ? "No hay convocatorias registradas" : ""));
                        lblTotal.setText("Total: " + items.size() + " convocatorias");
                        if (dashboardController != null) dashboardController.ocultarCargando();
                        System.err.println("[CONV] Platform.runLater FIN — tabla.size=" + tablaConvocatorias.getItems().size());
                    } catch (Exception ex) {
                        System.err.println("[CONV] EXCEPCION en Platform.runLater: " + ex.getMessage());
                        ex.printStackTrace(System.err);
                    }
                });
                return null;
            }
        };

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            System.err.println("[CONV] TASK FAILED: " + (ex != null ? ex.getMessage() : "null"));
            if (ex != null) ex.printStackTrace();
            lblTotal.setText("Error al cargar. Revisa la conexión.");
        });

        Thread hilo = new Thread(task);
        hilo.setDaemon(true);
        hilo.start();
    }

    private static String nvl(String s) { return s != null ? s : ""; }

    private String getStr(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : null;
    }

    @FXML private void filtrar() {
        if (actualizandoCombo) return;
        String sel = cmbPartido.getValue();
        if (sel == null || sel.equals("Todos los partidos")) {
            cargarDatosAsync(null);
        } else {
            for (Partido p : listaPartidos) {
                if (textoPartido(p).equals(sel)) { cargarDatosAsync(p.getId()); break; }
            }
        }
    }

    @FXML private void limpiarFiltro() {
        cmbPartido.setValue("Todos los partidos");
        cargarDatosAsync(null);
    }

    @FXML
    private void nuevaConvocatoria() {
        ComboBox<String> cmbPartidoD = new ComboBox<>();
        ComboBox<String> cmbJugadorD = new ComboBox<>();
        ComboBox<String> cmbTipo     = new ComboBox<>();
        cmbTipo.getItems().addAll("titular", "suplente", "no_convocado");
        cmbTipo.setValue("titular");

        for (Partido p : listaPartidos) cmbPartidoD.getItems().add(textoPartido(p));
        List<Jugador> jugadores = (equipoActivo != null)
                ? jugadorDAO.obtenerActivosPorEquipo(equipoActivo.getId())
                : jugadorDAO.obtenerPorClub(com.javafx.scouteo.util.SesionUsuario.getInstance().getUsuarioActual().getClubId());
        for (Jugador j : jugadores)
            cmbJugadorD.getItems().add(j.getDorsal() + " - " + j.getNombre() + " " + j.getApellidos());

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setStyle("-fx-padding: 20;");
        grid.add(new Label("Partido:"), 0, 0); grid.add(cmbPartidoD, 1, 0);
        grid.add(new Label("Jugador:"), 0, 1); grid.add(cmbJugadorD, 1, 1);
        grid.add(new Label("Tipo:"),    0, 2); grid.add(cmbTipo,     1, 2);
        cmbPartidoD.setPrefWidth(250); cmbJugadorD.setPrefWidth(250);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nueva Convocatoria");
        dialog.setHeaderText("Anadir jugador a partido");
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/scouteo.css").toExternalForm());
        dialog.setOnShowing(e -> StageUtils.setAppIcon((Stage) dialog.getDialogPane().getScene().getWindow()));

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String selP = cmbPartidoD.getValue();
            String selJ = cmbJugadorD.getValue();
            if (selP == null || selJ == null) { mostrarError("Selecciona partido y jugador"); return; }

            Partido partido = listaPartidos.stream()
                    .filter(p -> textoPartido(p).equals(selP)).findFirst().orElse(null);
            Jugador jugador = jugadores.stream()
                    .filter(j -> (j.getDorsal() + " - " + j.getNombre() + " " + j.getApellidos()).equals(selJ))
                    .findFirst().orElse(null);

            if (partido == null || jugador == null) { mostrarError("Error al obtener datos"); return; }
            if (existeConvocatoria(jugador.getId(), partido.getId())) {
                mostrarError("Este jugador ya esta convocado para este partido"); return;
            }
            if (insertarConvocatoria(partido.getId(), jugador.getId(), cmbTipo.getValue())) {
                cargarDatosAsync(null);
                mostrarInfo("Convocatoria anadida correctamente");
            } else {
                mostrarError("Error al anadir convocatoria");
            }
        }
    }

    private void editarConvocatoria(ConvocatoriaItem item) {
        ComboBox<String> cmbTipo = new ComboBox<>();
        cmbTipo.getItems().addAll("titular", "suplente", "no_convocado");
        cmbTipo.setValue(item.getTipo());

        javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(10,
                new Label("Tipo de convocatoria:"), cmbTipo);
        vbox.setStyle("-fx-padding: 20;");

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Editar Convocatoria");
        dialog.setHeaderText(item.getJugador() + " — " + item.getPartido());
        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/scouteo.css").toExternalForm());
        dialog.setOnShowing(e -> StageUtils.setAppIcon((Stage) dialog.getDialogPane().getScene().getWindow()));

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            ApiClient api = ApiClient.getInstance();
            Map<String, Object> body = new HashMap<>();
            body.put("partidoId", item.getIdPartido());
            body.put("jugadorId", item.getIdJugador());
            body.put("tipo", cmbTipo.getValue());
            if (api.put("/convocatorias/" + item.getId(), body) != null) {
                cargarDatosAsync(null);
            } else {
                mostrarError("Error al actualizar convocatoria");
            }
        }
    }

    private void eliminarConvocatoria(ConvocatoriaItem item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminacion");
        alert.setContentText("Eliminar a " + item.getJugador() + " del partido contra " + item.getPartido() + "?");
        alert.setOnShowing(e -> StageUtils.setAppIcon((Stage) alert.getDialogPane().getScene().getWindow()));

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (ApiClient.getInstance().delete("/convocatorias/" + item.getId())) {
                cargarDatosAsync(null);
            } else {
                mostrarError("Error al eliminar convocatoria");
            }
        }
    }

    // ==================== Helpers ====================

    private boolean existeConvocatoria(int jugadorId, int partidoId) {
        String json = ApiClient.getInstance().get("/convocatorias/partido/" + partidoId);
        if (json == null) return false;
        for (JsonObject o : ApiClient.getInstance().fromJsonList(json, JsonObject.class)) {
            if (o.has("jugadorId") && !o.get("jugadorId").isJsonNull()
                    && o.get("jugadorId").getAsInt() == jugadorId) return true;
        }
        return false;
    }

    private boolean insertarConvocatoria(int partidoId, int jugadorId, String tipo) {
        Map<String, Object> body = new HashMap<>();
        body.put("partidoId", partidoId);
        body.put("jugadorId", jugadorId);
        body.put("tipo", tipo);
        return ApiClient.getInstance().post("/convocatorias", body) != null;
    }

    private String textoPartido(Partido p) {
        String fecha = p.getFechaHora() != null ? p.getFechaHora().toLocalDate().toString() : "?";
        return fecha + " - " + p.getRival();
    }

    private void mostrarError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error"); a.setContentText(msg);
        a.setOnShowing(e -> StageUtils.setAppIcon((Stage) a.getDialogPane().getScene().getWindow()));
        a.showAndWait();
    }

    private void mostrarInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Info"); a.setContentText(msg);
        a.setOnShowing(e -> StageUtils.setAppIcon((Stage) a.getDialogPane().getScene().getWindow()));
        a.showAndWait();
    }

    // ==================== Inner class ====================

    public static class ConvocatoriaItem {
        private final int id, partidoId, jugadorId;
        private final String partido, jugador, posicion, tipo, motivoBaja;
        private final Integer dorsal;
        private final LocalDate fecha;

        public ConvocatoriaItem(int id, int partidoId, int jugadorId,
                                String partido, LocalDate fecha, String jugador,
                                Integer dorsal, String posicion, String tipo, String motivoBaja) {
            this.id = id; this.partidoId = partidoId; this.jugadorId = jugadorId;
            this.partido = partido; this.fecha = fecha; this.jugador = jugador;
            this.dorsal = dorsal; this.posicion = posicion;
            this.tipo = tipo != null ? tipo : "suplente";
            this.motivoBaja = motivoBaja;
        }

        public int    getId()         { return id; }
        public int    getIdPartido()  { return partidoId; }
        public int    getIdJugador()  { return jugadorId; }
        public String getPartido()    { return partido; }
        public LocalDate getFecha()   { return fecha; }
        public String getJugador()    { return jugador; }
        public Integer getDorsal()    { return dorsal; }
        public String getPosicion()   { return posicion; }
        public String getTipo()       { return tipo; }
        public String getMotivoBaja() { return motivoBaja; }

        public String getConvocado() { return "no_convocado".equals(tipo) ? "No" : "Si"; }
        public String getTitular()   { return "titular".equals(tipo) ? "Si" : "No"; }
        public boolean isTitularBool()   { return "titular".equals(tipo); }
        public boolean isConvocadoBool() { return !"no_convocado".equals(tipo); }
    }
}

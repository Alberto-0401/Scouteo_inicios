package com.javafx.scouteo.controller;

import com.javafx.scouteo.dao.EquipoDAO;
import com.javafx.scouteo.dao.JugadorDAO;
import com.javafx.scouteo.dao.PartidoDAO;
import com.javafx.scouteo.model.Equipo;
import com.javafx.scouteo.model.Jugador;
import com.javafx.scouteo.model.Partido;
import com.javafx.scouteo.util.SesionUsuario;
import com.javafx.scouteo.utils.FootballLoadingPane;
import com.javafx.scouteo.utils.StageUtils;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.chart.ChartData;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardController {

    // ---- Barra superior ----
    @FXML private Label lblUsuarioNombre;
    @FXML private Label lblUsuarioRol;
    @FXML private ComboBox<Equipo> cmbEquipoActivo;
    @FXML private Button btnCerrarSesion;

    // ---- Panel lateral ----
    @FXML private Label lblTotalJugadores;
    @FXML private Label lblTotalPartidos;
    @FXML private Label lblTotalGoles;
    @FXML private Pane  chartPosiciones;

    // ---- Navegación ----
    @FXML private Button btnInfoClub;
    @FXML private Button btnListadoJugadores;
    @FXML private Button btnEquipos;
    @FXML private Button btnRanking;
    @FXML private Button btnEstadisticasJugador;
    @FXML private Button btnPartidos;
    @FXML private Button btnConvocatorias;
    @FXML private Button btnHistorialMedico;
    @FXML private Button btnEntrenamientos;
    @FXML private Button btnObjetivos;
    @FXML private Button btnInformes;

    @FXML private StackPane contenedorCentral;

    private final JugadorDAO jugadorDAO = new JugadorDAO();
    private final PartidoDAO partidoDAO = new PartidoDAO();
    private final EquipoDAO  equipoDAO  = new EquipoDAO();

    private Equipo equipoActivo;
    private Runnable vistaActual;

    // ---- Overlay de carga ----
    private FootballLoadingPane overlayPane;

    @FXML
    public void initialize() {
        crearOverlay();
        configurarUsuario();
        cargarEquiposAsync();
    }

    private void configurarUsuario() {
        SesionUsuario sesion = SesionUsuario.getInstance();
        if (sesion.haySesionActiva()) {
            lblUsuarioNombre.setText(sesion.getUsuarioActual().getNombreCompleto());
            lblUsuarioRol.setText(sesion.getUsuarioActual().getRolDisplay());
        }
        // Solo directiva puede ver todos los equipos
        btnEquipos.setVisible(sesion.esDirectiva());
        btnEquipos.setManaged(sesion.esDirectiva());
    }

    // Carga equipos en background; cuando termina configura el combo y arranca cargarDatosAsync + mostrarInfoClub
    private void cargarEquiposAsync() {
        SesionUsuario sesion = SesionUsuario.getInstance();
        Thread t = new Thread(() -> {
            List<Equipo> equipos = sesion.esDirectiva()
                    ? equipoDAO.obtenerPorClub(sesion.getUsuarioActual().getClubId())
                    : equipoDAO.obtenerPorEntrenador(sesion.getUsuarioActual().getId());
            Platform.runLater(() -> {
                cmbEquipoActivo.setItems(FXCollections.observableArrayList(equipos));
                if (!equipos.isEmpty()) {
                    equipoActivo = equipos.get(0);
                    cmbEquipoActivo.setValue(equipoActivo);
                }
                cmbEquipoActivo.setOnAction(e -> {
                    Equipo sel = cmbEquipoActivo.getValue();
                    if (sel != null) {
                        equipoActivo = sel;
                        cargarDatosAsync();
                        if (vistaActual != null) vistaActual.run();
                    }
                });
                cargarDatosAsync();
                mostrarInfoClub();
            });
        }, "equipos-loader");
        t.setDaemon(true);
        t.start();
    }

    // 2 llamadas HTTP en background (jugadores + partidos) en vez de 5 en el hilo FX
    private void cargarDatosAsync() {
        final int idEquipo = equipoActivo != null ? equipoActivo.getId() : 0;
        final int clubId = SesionUsuario.getInstance().getUsuarioActual().getClubId();
        Thread t = new Thread(() -> {
            // Una sola petición de jugadores → cuenta + distribución
            List<Jugador> jugadores = idEquipo > 0
                    ? jugadorDAO.obtenerPorEquipo(idEquipo)
                    : jugadorDAO.obtenerPorClub(clubId);
            int totalJug = jugadores.size();
            Map<String, Integer> dist = new HashMap<>(Map.of("POR", 0, "DEF", 0, "MED", 0, "DEL", 0));
            for (Jugador j : jugadores) dist.merge(j.getGrupoPosicion(), 1, Integer::sum);

            // Una sola petición de partidos → cuenta + goles
            List<Partido> partidos = idEquipo > 0
                    ? partidoDAO.obtenerPorEquipo(idEquipo)
                    : partidoDAO.obtenerPorClub(clubId);
            int totalPar   = partidos.size();
            int totalGoles = partidos.stream()
                    .mapToInt(p -> p.getGolesFavor() != null ? p.getGolesFavor() : 0).sum();

            Platform.runLater(() -> {
                lblTotalJugadores.setText(String.valueOf(totalJug));
                animarActualizacion(lblTotalJugadores);
                lblTotalPartidos.setText(String.valueOf(totalPar));
                animarActualizacion(lblTotalPartidos);
                lblTotalGoles.setText(String.valueOf(totalGoles));
                animarActualizacion(lblTotalGoles);
                renderGraficoPosiciones(dist);
            });
        }, "datos-loader");
        t.setDaemon(true);
        t.start();
    }

    private void renderGraficoPosiciones(Map<String, Integer> dist) {
        chartPosiciones.getChildren().clear();
        ChartData dPor = new ChartData("Porteros",       dist.getOrDefault("POR", 0), Color.web("#ff7802ff"));
        ChartData dDef = new ChartData("Defensas",       dist.getOrDefault("DEF", 0), Color.web("#f3c221ff"));
        ChartData dMed = new ChartData("Mediocampistas", dist.getOrDefault("MED", 0), Color.web("#00fd15ff"));
        ChartData dDel = new ChartData("Delanteros",     dist.getOrDefault("DEL", 0), Color.web("#1201ffff"));
        Tile chart = TileBuilder.create()
            .skinType(Tile.SkinType.DONUT_CHART)
            .prefSize(200, 120)
            .title("").textVisible(true)
            .chartData(dPor, dDef, dMed, dDel)
            .animated(true)
            .backgroundColor(Color.web("#ffffffff"))
            .foregroundColor(Color.BLACK)
            .textColor(Color.BLACK)
            .build();
        chartPosiciones.getChildren().add(chart);
    }

    // ==================== Navegación ====================

    @FXML private void mostrarInfoClub() {
        vistaActual = this::mostrarInfoClub;
        navegarConCarga("Cargando información del club...", () -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/InfoClub.fxml"));
                Node vista = loader.load();
                InfoClubController ctrl = loader.getController();
                ctrl.setEquipoActivo(equipoActivo);
                mostrarEnCentral(vista);
                actualizarEstiloBotones(btnInfoClub);
            } catch (IOException e) { e.printStackTrace(); }
            ocultarCargando(); // vista sin carga async pesada
        });
    }

    @FXML public void mostrarListadoJugadores() {
        vistaActual = this::mostrarListadoJugadores;
        navegarConCarga("Cargando jugadores...", () -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ListadoJugadores.fxml"));
                Node vista = loader.load();
                ListadoJugadoresController ctrl = loader.getController();
                ctrl.setDashboardController(this);
                ctrl.setEquipoActivo(equipoActivo);
                mostrarEnCentral(vista);
                actualizarEstiloBotones(btnListadoJugadores);
            } catch (IOException e) { e.printStackTrace(); }
        });
    }

    @FXML private void mostrarEquipos() {
        vistaActual = this::mostrarEquipos;
        navegarConCarga("Cargando equipos...", () -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Equipos.fxml"));
                Node vista = loader.load();
                EquiposController ctrl = loader.getController();
                ctrl.setDashboardController(this); // ctrl llama ocultarCargando cuando la tabla se llena
                mostrarEnCentral(vista);
                actualizarEstiloBotones(btnEquipos);
            } catch (IOException e) { e.printStackTrace(); ocultarCargando(); }
        });
    }

    @FXML private void mostrarRanking() {
        vistaActual = this::mostrarRanking;
        navegarConCarga("Cargando ranking...", () -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Ranking.fxml"));
                Node vista = loader.load();
                RankingController ctrl = loader.getController();
                ctrl.setEquipoActivo(equipoActivo);
                mostrarEnCentral(vista);
                actualizarEstiloBotones(btnRanking);
            } catch (IOException e) { e.printStackTrace(); }
        });
    }

    @FXML private void mostrarEstadisticasJugadorDirect() {
        vistaActual = this::mostrarEstadisticasJugadorDirect;
        navegarConCarga("Cargando estadísticas...", () -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EstadisticasJugador.fxml"));
                Node vista = loader.load();
                EstadisticasJugadorController ctrl = loader.getController();
                ctrl.setDashboardController(this);
                ctrl.setEquipoActivo(equipoActivo);
                mostrarEnCentral(vista);
                actualizarEstiloBotones(btnEstadisticasJugador);
            } catch (IOException e) { e.printStackTrace(); }
        });
    }

    @FXML private void mostrarPartidos() {
        vistaActual = this::mostrarPartidos;
        navegarConCarga("Cargando partidos...", () -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Partidos.fxml"));
                Node vista = loader.load();
                PartidosController ctrl = loader.getController();
                ctrl.setEquipoActivo(equipoActivo);
                mostrarEnCentral(vista);
                actualizarEstiloBotones(btnPartidos);
            } catch (IOException e) { e.printStackTrace(); }
        });
    }

    @FXML private void mostrarConvocatorias() {
        vistaActual = this::mostrarConvocatorias;
        navegarConCarga("Cargando convocatorias...", () -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Convocatorias.fxml"));
                Node vista = loader.load();
                ConvocatoriasController ctrl = loader.getController();
                ctrl.setDashboardController(this); // ctrl llama ocultarCargando cuando la tabla se llena
                ctrl.setEquipoActivo(equipoActivo);
                mostrarEnCentral(vista);
                actualizarEstiloBotones(btnConvocatorias);
            } catch (IOException e) { e.printStackTrace(); ocultarCargando(); }
        });
    }

    @FXML public void mostrarHistorialMedico() {
        vistaActual = this::mostrarHistorialMedico;
        navegarConCarga("Cargando historial médico...", () -> {
            cargarVista("/views/HistorialMedico.fxml");
            actualizarEstiloBotones(btnHistorialMedico);
            ocultarCargando();
        });
    }

    @FXML private void mostrarEntrenamientos() {
        vistaActual = this::mostrarEntrenamientos;
        navegarConCarga("Cargando entrenamientos...", () -> {
            cargarVista("/views/Entrenamientos.fxml");
            actualizarEstiloBotones(btnEntrenamientos);
            ocultarCargando();
        });
    }

    @FXML private void mostrarObjetivos() {
        vistaActual = this::mostrarObjetivos;
        navegarConCarga("Cargando objetivos...", () -> {
            cargarVista("/views/Objetivos.fxml");
            actualizarEstiloBotones(btnObjetivos);
            ocultarCargando();
        });
    }

    @FXML private void mostrarInformes() {
        vistaActual = this::mostrarInformes;
        navegarConCarga("Cargando informes...", () -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Informes.fxml"));
                Node vista = loader.load();
                InformesController ctrl = loader.getController();
                ctrl.setDashboardController(this);
                mostrarEnCentral(vista);
                actualizarEstiloBotones(btnInformes);
            } catch (IOException e) { e.printStackTrace(); }
            ocultarCargando(); // Informes no auto-carga tabla; el overlay de generación lo gestiona ctrl
        });
    }

    @FXML
    private void cerrarSesion() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cerrar sesion");
        alert.setHeaderText("Seguro que quieres cerrar sesion?");
        alert.setOnShowing(e -> StageUtils.setAppIcon((Stage) alert.getDialogPane().getScene().getWindow()));

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            SesionUsuario.getInstance().cerrarSesion();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Login.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) btnCerrarSesion.getScene().getWindow();
                String css = getClass().getClassLoader().getResource("scouteo.css").toExternalForm();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(css);
                StageUtils.setAppIcon(stage);
                stage.setScene(scene);
                stage.setTitle("SCOUTEO - Iniciar Sesion");
                stage.setWidth(900);
                stage.setHeight(600);
                stage.setResizable(false);
                stage.centerOnScreen();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // ==================== Overlay de carga ====================

    private void crearOverlay() {
        overlayPane = new FootballLoadingPane();
        contenedorCentral.getChildren().add(overlayPane);
    }

    public void mostrarCargando(String mensaje) {
        overlayPane.setMensaje(mensaje);
        overlayPane.toFront();
        overlayPane.mostrar();
    }

    public void ocultarCargando() {
        overlayPane.ocultar();
    }

    /**
     * Muestra el overlay y ejecuta el trabajo en el siguiente pulso FX.
     * Las vistas que cargan datos de forma asíncrona deben llamar a ocultarCargando()
     * cuando su tabla se haya poblado. Como red de seguridad se oculta tras 8 s.
     */
    private void navegarConCarga(String mensaje, Runnable trabajo) {
        mostrarCargando(mensaje);
        Platform.runLater(() -> {
            try {
                trabajo.run();
            } catch (Exception e) {
                e.printStackTrace();
                ocultarCargando();
                return;
            }
            // Red de seguridad: ocultar si el controlador hijo no lo hace en 8 s
            PauseTransition seguridad = new PauseTransition(Duration.seconds(8));
            seguridad.setOnFinished(ev -> ocultarCargando());
            seguridad.play();
        });
    }

    // ==================== Utilidades ====================

    public void mostrarEstadisticasJugador(Jugador jugador) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EstadisticasJugador.fxml"));
            Node vista = loader.load();
            EstadisticasJugadorController ctrl = loader.getController();
            ctrl.setDashboardController(this);
            ctrl.setJugador(jugador);
            mostrarEnCentral(vista);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void mostrarPartidosConJugador(Jugador jugador) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Partidos.fxml"));
            Node vista = loader.load();
            PartidosController ctrl = loader.getController();
            ctrl.setEquipoActivo(equipoActivo);
            mostrarEnCentral(vista);
            actualizarEstiloBotones(btnPartidos);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public Equipo getEquipoActivo() { return equipoActivo; }

    public void actualizarDatos() { cargarDatosAsync(); }

    private void cargarVista(String ruta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ruta));
            Node vista = loader.load();
            mostrarEnCentral(vista);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarEnCentral(Node vista) {
        contenedorCentral.getChildren().clear();
        contenedorCentral.getChildren().add(vista);
        // El overlay siempre encima del contenido
        if (overlayPane != null) contenedorCentral.getChildren().add(overlayPane);
        FadeTransition ft = new FadeTransition(Duration.millis(280), vista);
        ft.setFromValue(0.0); ft.setToValue(1.0); ft.play();
    }

    private void animarActualizacion(Node nodo) {
        ScaleTransition sc = new ScaleTransition(Duration.millis(200), nodo);
        sc.setFromX(1.0); sc.setFromY(1.0); sc.setToX(1.15); sc.setToY(1.15);
        sc.setCycleCount(2); sc.setAutoReverse(true); sc.play();
    }

    private void actualizarEstiloBotones(Button activo) {
        for (Button b : new Button[]{btnInfoClub, btnListadoJugadores, btnEquipos, btnRanking,
                btnEstadisticasJugador, btnPartidos, btnConvocatorias, btnHistorialMedico,
                btnEntrenamientos, btnObjetivos, btnInformes}) {
            if (b != null) b.setDisable(false);
        }
        activo.setDisable(true);
    }
}

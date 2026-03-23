package com.javafx.scouteo.controller;

import com.javafx.scouteo.dao.EquipoDAO;
import com.javafx.scouteo.dao.JugadorDAO;
import com.javafx.scouteo.dao.PartidoDAO;
import com.javafx.scouteo.model.Equipo;
import com.javafx.scouteo.model.Jugador;
import com.javafx.scouteo.util.ConexionBD;
import com.javafx.scouteo.util.SesionUsuario;
import com.javafx.scouteo.utils.StageUtils;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.chart.ChartData;
import javafx.animation.FadeTransition;
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

    @FXML
    public void initialize() {
        configurarUsuario();
        cargarEquipos();
        cargarDatos();
        mostrarInfoClub();
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

    private void cargarEquipos() {
        SesionUsuario sesion = SesionUsuario.getInstance();
        List<Equipo> equipos;
        if (sesion.esDirectiva()) {
            equipos = equipoDAO.obtenerPorClub(sesion.getUsuarioActual().getClubId());
        } else {
            equipos = equipoDAO.obtenerPorEntrenador(sesion.getUsuarioActual().getId());
        }

        cmbEquipoActivo.setItems(FXCollections.observableArrayList(equipos));

        if (!equipos.isEmpty()) {
            equipoActivo = equipos.get(0);
            cmbEquipoActivo.setValue(equipoActivo);
        }

        cmbEquipoActivo.setOnAction(e -> {
            Equipo sel = cmbEquipoActivo.getValue();
            if (sel != null) {
                equipoActivo = sel;
                cargarDatos();
                if (vistaActual != null) vistaActual.run();
            }
        });
    }

    private void cargarDatos() {
        if (!ConexionBD.isConexionValida()) {
            lblTotalJugadores.setText("0");
            lblTotalPartidos.setText("0");
            lblTotalGoles.setText("0");
            chartPosiciones.getChildren().clear();
            Label err = new Label("Sin conexion a la BD");
            err.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 12px;");
            chartPosiciones.getChildren().add(err);
            return;
        }

        int idEquipo = equipoActivo != null ? equipoActivo.getId() : 0;

        int clubId = SesionUsuario.getInstance().getUsuarioActual().getClubId();
        int totalJug = idEquipo > 0 ? jugadorDAO.contarPorEquipo(idEquipo) : jugadorDAO.contarPorClub(clubId);
        lblTotalJugadores.setText(String.valueOf(totalJug));
        animarActualizacion(lblTotalJugadores);

        int totalPar = idEquipo > 0 ? partidoDAO.contarPorEquipo(idEquipo) : partidoDAO.contarPorClub(clubId);
        lblTotalPartidos.setText(String.valueOf(totalPar));
        animarActualizacion(lblTotalPartidos);

        int totalGoles = idEquipo > 0 ? partidoDAO.contarGolesEquipo(idEquipo) : 0;
        lblTotalGoles.setText(String.valueOf(totalGoles));
        animarActualizacion(lblTotalGoles);

        cargarGraficoPosiciones(idEquipo);
    }

    private void cargarGraficoPosiciones(int equipoId) {
        chartPosiciones.getChildren().clear();

        Map<String, Integer> dist = equipoId > 0
            ? jugadorDAO.obtenerDistribucionPorPosicionEquipo(equipoId)
            : jugadorDAO.obtenerDistribucionPorPosicionClub(SesionUsuario.getInstance().getUsuarioActual().getClubId());

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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/InfoClub.fxml"));
            Node vista = loader.load();
            InfoClubController ctrl = loader.getController();
            ctrl.setEquipoActivo(equipoActivo);
            mostrarEnCentral(vista);
            actualizarEstiloBotones(btnInfoClub);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML public void mostrarListadoJugadores() {
        vistaActual = this::mostrarListadoJugadores;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ListadoJugadores.fxml"));
            Node vista = loader.load();
            ListadoJugadoresController ctrl = loader.getController();
            ctrl.setDashboardController(this);
            ctrl.setEquipoActivo(equipoActivo);
            mostrarEnCentral(vista);
            actualizarEstiloBotones(btnListadoJugadores);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void mostrarEquipos() {
        vistaActual = this::mostrarEquipos;
        cargarVista("/views/Equipos.fxml");
        actualizarEstiloBotones(btnEquipos);
    }

    @FXML private void mostrarRanking() {
        vistaActual = this::mostrarRanking;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Ranking.fxml"));
            Node vista = loader.load();
            RankingController ctrl = loader.getController();
            ctrl.setEquipoActivo(equipoActivo);
            mostrarEnCentral(vista);
            actualizarEstiloBotones(btnRanking);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void mostrarEstadisticasJugadorDirect() {
        vistaActual = this::mostrarEstadisticasJugadorDirect;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EstadisticasJugador.fxml"));
            Node vista = loader.load();
            EstadisticasJugadorController ctrl = loader.getController();
            ctrl.setDashboardController(this);
            ctrl.setEquipoActivo(equipoActivo);
            mostrarEnCentral(vista);
            actualizarEstiloBotones(btnEstadisticasJugador);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void mostrarPartidos() {
        vistaActual = this::mostrarPartidos;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Partidos.fxml"));
            Node vista = loader.load();
            PartidosController ctrl = loader.getController();
            ctrl.setEquipoActivo(equipoActivo);
            mostrarEnCentral(vista);
            actualizarEstiloBotones(btnPartidos);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void mostrarConvocatorias() {
        vistaActual = this::mostrarConvocatorias;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Convocatorias.fxml"));
            Node vista = loader.load();
            ConvocatoriasController ctrl = loader.getController();
            ctrl.setEquipoActivo(equipoActivo);
            mostrarEnCentral(vista);
            actualizarEstiloBotones(btnConvocatorias);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML public void mostrarHistorialMedico() {
        vistaActual = this::mostrarHistorialMedico;
        cargarVista("/views/HistorialMedico.fxml");
        actualizarEstiloBotones(btnHistorialMedico);
    }

    @FXML private void mostrarEntrenamientos() {
        vistaActual = this::mostrarEntrenamientos;
        cargarVista("/views/Entrenamientos.fxml");
        actualizarEstiloBotones(btnEntrenamientos);
    }

    @FXML private void mostrarObjetivos() {
        vistaActual = this::mostrarObjetivos;
        cargarVista("/views/Objetivos.fxml");
        actualizarEstiloBotones(btnObjetivos);
    }

    @FXML private void mostrarInformes() {
        vistaActual = this::mostrarInformes;
        cargarVista("/views/Informes.fxml");
        actualizarEstiloBotones(btnInformes);
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

    public void actualizarDatos() { cargarDatos(); }

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

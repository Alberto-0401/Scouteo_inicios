package com.javafx.scouteo.controller;

import com.javafx.scouteo.model.Jugador;
import com.javafx.scouteo.dao.JugadorDAO;
import com.javafx.scouteo.dao.PartidoDAO;
import com.javafx.scouteo.dao.EstadisticaPartidoDAO;
import com.javafx.scouteo.util.ConexionBD;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.paint.Color;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.RotateTransition;
import javafx.util.Duration;
import java.io.IOException;
import java.util.Map;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.chart.ChartData;

public class DashboardController {

    @FXML
    private Label lblTotalJugadores;

    @FXML
    private Label lblTotalPartidos;

    @FXML
    private Label lblTotalGoles;

    @FXML
    private Pane chartPosiciones;

    @FXML
    private StackPane contenedorCentral;

    @FXML
    private Button btnInfoClub;

    @FXML
    private Button btnListadoJugadores;

    @FXML
    private Button btnRanking;

    @FXML
    private Button btnPartidos;

    @FXML
    private Button btnConvocatorias;

    private Jugador jugadorSeleccionado;
    private JugadorDAO jugadorDAO;
    private PartidoDAO partidoDAO;
    private EstadisticaPartidoDAO estadisticaPartidoDAO;

    @FXML
    public void initialize() {
        jugadorDAO = new JugadorDAO();
        partidoDAO = new PartidoDAO();
        estadisticaPartidoDAO = new EstadisticaPartidoDAO();
        cargarDatos();
        // Cargar vista inicial (Info Club)
        mostrarInfoClub();
    }

    private void cargarDatos() {
        // Verificar conexión a la base de datos
        if (!ConexionBD.isConexionValida()) {
            lblTotalJugadores.setText("0");
            lblTotalPartidos.setText("0");
            lblTotalGoles.setText("0");
            // Limpiar el gráfico
            chartPosiciones.getChildren().clear();
            Label errorLabel = new Label("❌ Error de conexión a la base de datos");
            errorLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 14px; -fx-font-weight: bold;");
            chartPosiciones.getChildren().add(errorLabel);
            return;
        }

        int totalJugadores = jugadorDAO.contarTotal();
        lblTotalJugadores.setText(String.valueOf(totalJugadores));
        animarActualizacion(lblTotalJugadores);

        int totalPartidos = partidoDAO.contarTotal();
        lblTotalPartidos.setText(String.valueOf(totalPartidos));
        animarActualizacion(lblTotalPartidos);

        int totalGoles = estadisticaPartidoDAO.contarTotalGoles();
        lblTotalGoles.setText(String.valueOf(totalGoles));
        animarActualizacion(lblTotalGoles);

        // Cargar distribución por posición
        cargarGraficoPosiciones();
    }

    /**
     * Anima un nodo con efecto pulse cuando se actualiza
     */
    private void animarActualizacion(Node nodo) {
        // Animación de escala (pulse)
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), nodo);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.15);
        scale.setToY(1.15);
        scale.setCycleCount(2);
        scale.setAutoReverse(true);

        // Animación de fade para que sea más suave
        FadeTransition fade = new FadeTransition(Duration.millis(100), nodo);
        fade.setFromValue(1.0);
        fade.setToValue(0.7);
        fade.setCycleCount(2);
        fade.setAutoReverse(true);

        // Ejecutar ambas animaciones al mismo tiempo
        scale.play();
        fade.play();
    }

    private void cargarGraficoPosiciones() {
        chartPosiciones.getChildren().clear();

        // Obtener distribución de jugadores por posición
        Map<String, Integer> distribucion = jugadorDAO.obtenerDistribucionPorPosicion();

        // Obtener cantidades por posición
        int porteros = distribucion.getOrDefault("POR", 0);
        int defensas = distribucion.getOrDefault("DEF", 0);
        int mediocampistas = distribucion.getOrDefault("MED", 0);
        int delanteros = distribucion.getOrDefault("DEL", 0);

        // Crear datos para el gráfico con TilesFX
        ChartData dataPorteros = new ChartData("Porteros", porteros, Color.web("#ff7802ff"));
        ChartData dataDefensas = new ChartData("Defensas", defensas, Color.web("#f3c221ff"));
        ChartData dataMediocampistas = new ChartData("Mediocampistas", mediocampistas, Color.web("#00fd15ff"));
        ChartData dataDelanteros = new ChartData("Delanteros", delanteros, Color.web("#1201ffff"));

        // Crear el Grafico
        Tile pieChartTile = TileBuilder.create()
            .skinType(Tile.SkinType.DONUT_CHART)
            .prefSize(250, 150)
            .title("")
            .textVisible(true)
            .chartData(dataPorteros, dataDefensas, dataMediocampistas, dataDelanteros)
            .animated(true)
            .backgroundColor(Color.web("#ffffffff"))
            .foregroundColor(Color.web("#000000ff"))
            .textColor(Color.web("#000000ff"))
            .build();

        chartPosiciones.getChildren().add(pieChartTile);
    }

    @FXML
    private void mostrarInfoClub() {
        cargarVista("/views/InfoClub.fxml");
        actualizarEstiloBotones(btnInfoClub);
    }

    @FXML
    public void mostrarListadoJugadores() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ListadoJugadores.fxml"));
            Node vista = loader.load();

            // Obtener el controlador y pasarle la referencia al Dashboard
            ListadoJugadoresController controller = loader.getController();
            controller.setDashboardController(this);

            contenedorCentral.getChildren().clear();
            contenedorCentral.getChildren().add(vista);

            // Animación FadeIn
            aplicarAnimacionFadeIn(vista);

            actualizarEstiloBotones(btnListadoJugadores);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar ListadoJugadores.fxml");
        }
    }

    @FXML
    private void mostrarRanking() {
        cargarVista("/views/Ranking.fxml");
        actualizarEstiloBotones(btnRanking);
    }

    @FXML
    private void mostrarPartidos() {
        cargarVista("/views/Partidos.fxml");
        actualizarEstiloBotones(btnPartidos);
    }

    @FXML
    private void mostrarConvocatorias() {
        cargarVista("/views/Convocatorias.fxml");
        actualizarEstiloBotones(btnConvocatorias);
    }

    public void mostrarPartidosConJugador(Jugador jugador) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Partidos.fxml"));
            Node vista = loader.load();

            // Obtener el controlador y pasarle el jugador seleccionado
            PartidosController controller = loader.getController();
            controller.cargarEstadisticasJugador(jugador);

            contenedorCentral.getChildren().clear();
            contenedorCentral.getChildren().add(vista);

            // Animación FadeIn
            aplicarAnimacionFadeIn(vista);

            actualizarEstiloBotones(btnPartidos);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar Partidos.fxml");
        }
    }

    public void mostrarEstadisticasJugador(Jugador jugador) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EstadisticasJugador.fxml"));
            Node vista = loader.load();

            // Obtener el controlador y pasarle el jugador y dashboard
            EstadisticasJugadorController controller = loader.getController();
            controller.setDashboardController(this);
            controller.setJugador(jugador);

            contenedorCentral.getChildren().clear();
            contenedorCentral.getChildren().add(vista);

            // Animación FadeIn
            aplicarAnimacionFadeIn(vista);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar EstadisticasJugador.fxml");
        }
    }

    private void cargarVista(String rutaFXML) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFXML));
            Node vista = loader.load();
            contenedorCentral.getChildren().clear();
            contenedorCentral.getChildren().add(vista);

            // Animación FadeIn
            aplicarAnimacionFadeIn(vista);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar la vista: " + rutaFXML);
        }
    }

    /**
     * Aplica una animación de desvanecimiento (fade-in) a un nodo
     */
    private void aplicarAnimacionFadeIn(Node nodo) {
        FadeTransition fade = new FadeTransition(Duration.millis(300), nodo);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    private void actualizarEstiloBotones(Button botonActivo) {
        btnInfoClub.setDisable(false);
        btnListadoJugadores.setDisable(false);
        btnRanking.setDisable(false);
        btnPartidos.setDisable(false);
        btnConvocatorias.setDisable(false);

        botonActivo.setDisable(true);
    }

    /**
     * Método público para actualizar los datos del dashboard
     * Se debe llamar desde otros controladores cuando se modifiquen datos
     */
    public void actualizarDatos() {
        cargarDatos();
    }
}
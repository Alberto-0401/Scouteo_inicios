package com.javafx.scouteo.controller;

import com.javafx.scouteo.model.Jugador;
import com.javafx.scouteo.dao.JugadorDAO;
import com.javafx.scouteo.dao.PartidoDAO;
import com.javafx.scouteo.dao.EstadisticaPartidoDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import java.io.IOException;

public class DashboardController {

    @FXML
    private Label lblTotalJugadores;

    @FXML
    private Label lblTotalPartidos;

    @FXML
    private Label lblTotalGoles;

    @FXML
    private PieChart chartPosiciones;

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
        int totalJugadores = jugadorDAO.contarTotal();
        lblTotalJugadores.setText(String.valueOf(totalJugadores));

        int totalPartidos = partidoDAO.contarTotal();
        lblTotalPartidos.setText(String.valueOf(totalPartidos));

        int totalGoles = estadisticaPartidoDAO.contarTotalGoles();
        lblTotalGoles.setText(String.valueOf(totalGoles));

        // TODO: Cargar distribución por posición
        chartPosiciones.getData().clear();
    }

    @FXML
    private void mostrarInfoClub() {
        cargarVista("/views/InfoClub.fxml");
        actualizarEstiloBotones(btnInfoClub);
    }

    @FXML
    private void mostrarListadoJugadores() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ListadoJugadores.fxml"));
            Node vista = loader.load();

            // Obtener el controlador y pasarle la referencia al Dashboard
            ListadoJugadoresController controller = loader.getController();
            controller.setDashboardController(this);

            contenedorCentral.getChildren().clear();
            contenedorCentral.getChildren().add(vista);
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

    public void mostrarPartidosConJugador(Jugador jugador) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Partidos.fxml"));
            Node vista = loader.load();

            // Obtener el controlador y pasarle el jugador seleccionado
            PartidosController controller = loader.getController();
            controller.cargarEstadisticasJugador(jugador);

            contenedorCentral.getChildren().clear();
            contenedorCentral.getChildren().add(vista);
            actualizarEstiloBotones(btnPartidos);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar Partidos.fxml");
        }
    }

    private void cargarVista(String rutaFXML) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFXML));
            Node vista = loader.load();
            contenedorCentral.getChildren().clear();
            contenedorCentral.getChildren().add(vista);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar la vista: " + rutaFXML);
        }
    }

    private void actualizarEstiloBotones(Button botonActivo) {
        btnInfoClub.setDisable(false);
        btnListadoJugadores.setDisable(false);
        btnRanking.setDisable(false);
        btnPartidos.setDisable(false);

        botonActivo.setDisable(true);
    }
}
package com.javafx.ejercicios;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.chart.PieChart;

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
    public void initialize() {
        cargarDatos();
    }

    private void cargarDatos() {
        // TODO: Cargar desde base de datos
    }

    @FXML
    private void irAJugadores() {
        // TODO: Implementar navegación
    }

    @FXML
    private void irARanking() {
        // TODO: Implementar navegación
    }

    @FXML
    private void irAEstadisticas() {
        // TODO: Implementar navegación
    }
}
package com.javafx.scouteo.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class EstadisticasJugadorController {

    @FXML
    private ComboBox<String> cmbJugador;
    
    @FXML
    private Label lblNombreJugador;
    
    @FXML
    private Label lblPosicion;
    
    @FXML
    private Label lblDorsal;
    
    @FXML
    private Label lblTotalPartidos;
    
    @FXML
    private TableView<EstadisticaPartido> tablaEstadisticas;
    
    @FXML
    private TableColumn<EstadisticaPartido, String> colFecha;
    
    @FXML
    private TableColumn<EstadisticaPartido, String> colRival;
    
    @FXML
    private TableColumn<EstadisticaPartido, String> colResultado;
    
    @FXML
    private TableColumn<EstadisticaPartido, Integer> colMinutos;
    
    @FXML
    private TableColumn<EstadisticaPartido, Integer> colGoles;
    
    @FXML
    private TableColumn<EstadisticaPartido, Integer> colAsistencias;
    
    @FXML
    private TableColumn<EstadisticaPartido, Integer> colTarjetasA;
    
    @FXML
    private TableColumn<EstadisticaPartido, Integer> colTarjetasR;
    
    @FXML
    private Label lblTotalGoles;
    
    @FXML
    private Label lblTotalAsistencias;
    
    @FXML
    private Label lblPromedioGoles;

    @FXML
    public void initialize() {
        // Configurar columnas
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colRival.setCellValueFactory(new PropertyValueFactory<>("rival"));
        colResultado.setCellValueFactory(new PropertyValueFactory<>("resultado"));
        colMinutos.setCellValueFactory(new PropertyValueFactory<>("minutos"));
        colGoles.setCellValueFactory(new PropertyValueFactory<>("goles"));
        colAsistencias.setCellValueFactory(new PropertyValueFactory<>("asistencias"));
        colTarjetasA.setCellValueFactory(new PropertyValueFactory<>("tarjetasAmarillas"));
        colTarjetasR.setCellValueFactory(new PropertyValueFactory<>("tarjetasRojas"));
        
        cargarJugadores();
    }

    private void cargarJugadores() {
        
    }

    @FXML
    private void cargarEstadisticas() {
        String jugadorSeleccionado = cmbJugador.getValue();
        
        if (jugadorSeleccionado == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aviso");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, selecciona un jugador");
            alert.showAndWait();
            return;
        }
        
        ObservableList<EstadisticaPartido> datos = FXCollections.observableArrayList();
        
        // Calcular totales
        int totalPartidos = datos.size();
        int totalGoles = datos.stream().mapToInt(EstadisticaPartido::getGoles).sum();
        int totalAsistencias = datos.stream().mapToInt(EstadisticaPartido::getAsistencias).sum();
        double promedio = totalPartidos > 0 ? (double) totalGoles / totalPartidos : 0.0;
        
        lblTotalPartidos.setText(String.valueOf(totalPartidos));
        lblTotalGoles.setText(String.valueOf(totalGoles));
        lblTotalAsistencias.setText(String.valueOf(totalAsistencias));
        lblPromedioGoles.setText(String.format("%.2f", promedio));
    }

    @FXML
    private void volverDashboard() {
    }
    public static class EstadisticaPartido {
        private String fecha;
        private String rival;
        private String resultado;
        private Integer minutos;
        private Integer goles;
        private Integer asistencias;
        private Integer tarjetasAmarillas;
        private Integer tarjetasRojas;
        
        public EstadisticaPartido(String fecha, String rival, String resultado, 
                                 Integer minutos, Integer goles, Integer asistencias,
                                 Integer tarjetasAmarillas, Integer tarjetasRojas) {
            this.fecha = fecha;
            this.rival = rival;
            this.resultado = resultado;
            this.minutos = minutos;
            this.goles = goles;
            this.asistencias = asistencias;
            this.tarjetasAmarillas = tarjetasAmarillas;
            this.tarjetasRojas = tarjetasRojas;
        }
        
        public String getFecha() { return fecha; }
        public String getRival() { return rival; }
        public String getResultado() { return resultado; }
        public Integer getMinutos() { return minutos; }
        public Integer getGoles() { return goles; }
        public Integer getAsistencias() { return asistencias; }
        public Integer getTarjetasAmarillas() { return tarjetasAmarillas; }
        public Integer getTarjetasRojas() { return tarjetasRojas; }
    }
}
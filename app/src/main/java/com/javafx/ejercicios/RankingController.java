package com.javafx.ejercicios;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class RankingController {

    @FXML
    private ComboBox<String> cmbOrdenar;
    
    @FXML
    private ComboBox<String> cmbPosicion;
    
    @FXML
    private TableView<JugadorRanking> tablaRanking;
    
    @FXML
    private TableColumn<JugadorRanking, Integer> colPosicion;
    
    @FXML
    private TableColumn<JugadorRanking, String> colNombre;
    
    @FXML
    private TableColumn<JugadorRanking, Integer> colDorsal;
    
    @FXML
    private TableColumn<JugadorRanking, String> colPosicionJuego;
    
    @FXML
    private TableColumn<JugadorRanking, Integer> colPartidos;
    
    @FXML
    private TableColumn<JugadorRanking, Integer> colGoles;
    
    @FXML
    private TableColumn<JugadorRanking, Integer> colAsistencias;
    
    @FXML
    private TableColumn<JugadorRanking, Double> colPromedio;
    
    @FXML
    private Label lblTotal;

    @FXML
    public void initialize() {
        // Configurar columnas
        colPosicion.setCellValueFactory(new PropertyValueFactory<>("posicion"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDorsal.setCellValueFactory(new PropertyValueFactory<>("dorsal"));
        colPosicionJuego.setCellValueFactory(new PropertyValueFactory<>("posicionJuego"));
        colPartidos.setCellValueFactory(new PropertyValueFactory<>("partidos"));
        colGoles.setCellValueFactory(new PropertyValueFactory<>("goles"));
        colAsistencias.setCellValueFactory(new PropertyValueFactory<>("asistencias"));
        colPromedio.setCellValueFactory(new PropertyValueFactory<>("promedio"));
        
        // Valores por defecto
        cmbOrdenar.setValue("Goles");
        cmbPosicion.setValue("Todas");
        
        cargarDatos();
    }

    private void cargarDatos() {
        ObservableList<JugadorRanking> datos = FXCollections.observableArrayList();

        tablaRanking.setItems(datos);
        lblTotal.setText("Mostrando " + datos.size() + " jugadores");
    }

    @FXML
    private void aplicarFiltros() {

        cargarDatos();
    }

    @FXML
    private void volverDashboard() {
        // TODO: Implementar navegación
    }

    public static class JugadorRanking {
        private Integer posicion;
        private String nombre;
        private Integer dorsal;
        private String posicionJuego;
        private Integer partidos;
        private Integer goles;
        private Integer asistencias;
        private Double promedio;
        
        public JugadorRanking(Integer posicion, String nombre, Integer dorsal, 
                             String posicionJuego, Integer partidos, Integer goles, 
                             Integer asistencias, Double promedio) {
            this.posicion = posicion;
            this.nombre = nombre;
            this.dorsal = dorsal;
            this.posicionJuego = posicionJuego;
            this.partidos = partidos;
            this.goles = goles;
            this.asistencias = asistencias;
            this.promedio = promedio;
        }
        
        // Getters
        public Integer getPosicion() { return posicion; }
        public String getNombre() { return nombre; }
        public Integer getDorsal() { return dorsal; }
        public String getPosicionJuego() { return posicionJuego; }
        public Integer getPartidos() { return partidos; }
        public Integer getGoles() { return goles; }
        public Integer getAsistencias() { return asistencias; }
        public Double getPromedio() { return promedio; }
    }
}
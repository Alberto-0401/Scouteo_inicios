package com.javafx.scouteo.controller;

import com.javafx.scouteo.dao.RankingDAO;
import com.javafx.scouteo.util.ConexionBD;
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

    private RankingDAO rankingDAO;

    @FXML
    public void initialize() {
        rankingDAO = new RankingDAO();

        colPosicion.setCellValueFactory(new PropertyValueFactory<>("posicion"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDorsal.setCellValueFactory(new PropertyValueFactory<>("dorsal"));
        colPosicionJuego.setCellValueFactory(new PropertyValueFactory<>("posicionJuego"));
        colPartidos.setCellValueFactory(new PropertyValueFactory<>("partidos"));
        colGoles.setCellValueFactory(new PropertyValueFactory<>("goles"));
        colAsistencias.setCellValueFactory(new PropertyValueFactory<>("asistencias"));
        colPromedio.setCellValueFactory(new PropertyValueFactory<>("promedio"));

        cmbOrdenar.setValue("Goles");
        cmbPosicion.setValue("Todas");

        cargarDatos();
    }

    private void cargarDatos() {
        // Verificar conexión a la base de datos
        if (!ConexionBD.isConexionValida()) {
            Label errorLabel = new Label("❌ No es posible conectar con la base de datos");
            errorLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 14px; -fx-font-weight: bold;");
            tablaRanking.setPlaceholder(errorLabel);
            tablaRanking.setItems(FXCollections.observableArrayList());
            lblTotal.setText("Mostrando 0 jugadores");
            return;
        }

        String ordenarPor = cmbOrdenar.getValue() != null ? cmbOrdenar.getValue() : "Goles";
        String posicion = cmbPosicion.getValue() != null ? cmbPosicion.getValue() : "Todas";

        ObservableList<JugadorRanking> datos = FXCollections.observableArrayList(
            rankingDAO.obtenerRanking(ordenarPor, posicion)
        );

        tablaRanking.setItems(datos);

        // Placeholder para cuando no hay datos pero la conexión es válida
        if (datos.isEmpty()) {
            Label emptyLabel = new Label("No hay jugadores con estadísticas");
            emptyLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");
            tablaRanking.setPlaceholder(emptyLabel);
        }

        lblTotal.setText("Mostrando " + datos.size() + " jugadores");
    }

    @FXML
    private void aplicarFiltros() {
        cargarDatos();
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
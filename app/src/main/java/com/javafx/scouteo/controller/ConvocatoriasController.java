package com.javafx.scouteo.controller;

import com.javafx.scouteo.util.ConexionBD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;
import java.time.LocalDate;

public class ConvocatoriasController {

    @FXML
    private TableView<ConvocatoriaItem> tablaConvocatorias;

    @FXML
    private TableColumn<ConvocatoriaItem, String> colPartido;

    @FXML
    private TableColumn<ConvocatoriaItem, LocalDate> colFecha;

    @FXML
    private TableColumn<ConvocatoriaItem, String> colJugador;

    @FXML
    private TableColumn<ConvocatoriaItem, Integer> colDorsal;

    @FXML
    private TableColumn<ConvocatoriaItem, String> colPosicion;

    @FXML
    private TableColumn<ConvocatoriaItem, String> colTitular;

    @FXML
    private TableColumn<ConvocatoriaItem, String> colConvocado;

    @FXML
    private Label lblTotal;

    private ObservableList<ConvocatoriaItem> listaConvocatorias;

    @FXML
    public void initialize() {
        configurarTabla();
        cargarConvocatorias();
    }

    private void configurarTabla() {
        colPartido.setCellValueFactory(new PropertyValueFactory<>("partido"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colJugador.setCellValueFactory(new PropertyValueFactory<>("jugador"));
        colDorsal.setCellValueFactory(new PropertyValueFactory<>("dorsal"));
        colPosicion.setCellValueFactory(new PropertyValueFactory<>("posicion"));
        colTitular.setCellValueFactory(new PropertyValueFactory<>("titular"));
        colConvocado.setCellValueFactory(new PropertyValueFactory<>("convocado"));
    }

    private void cargarConvocatorias() {
        listaConvocatorias = FXCollections.observableArrayList();

        String sql = "SELECT p.rival, p.fecha_partido, " +
                     "CONCAT(j.nombre, ' ', j.apellidos) as jugador, " +
                     "j.dorsal, j.posicion, " +
                     "jp.titular, jp.convocado " +
                     "FROM jugadores_partidos jp " +
                     "INNER JOIN partidos p ON jp.id_partido = p.id_partido " +
                     "INNER JOIN jugadores j ON jp.id_jugador = j.id_jugador " +
                     "ORDER BY p.fecha_partido DESC, j.dorsal";

        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ConvocatoriaItem item = new ConvocatoriaItem(
                    rs.getString("rival"),
                    rs.getDate("fecha_partido").toLocalDate(),
                    rs.getString("jugador"),
                    rs.getInt("dorsal"),
                    rs.getString("posicion"),
                    rs.getBoolean("titular") ? "Sí" : "No",
                    rs.getBoolean("convocado") ? "Sí" : "No"
                );
                listaConvocatorias.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        tablaConvocatorias.setItems(listaConvocatorias);
        lblTotal.setText("Total: " + listaConvocatorias.size() + " convocatorias");
    }

    // Clase interna para representar items de convocatorias
    public static class ConvocatoriaItem {
        private String partido;
        private LocalDate fecha;
        private String jugador;
        private Integer dorsal;
        private String posicion;
        private String titular;
        private String convocado;

        public ConvocatoriaItem(String partido, LocalDate fecha, String jugador,
                               Integer dorsal, String posicion, String titular, String convocado) {
            this.partido = partido;
            this.fecha = fecha;
            this.jugador = jugador;
            this.dorsal = dorsal;
            this.posicion = posicion;
            this.titular = titular;
            this.convocado = convocado;
        }

        public String getPartido() { return partido; }
        public LocalDate getFecha() { return fecha; }
        public String getJugador() { return jugador; }
        public Integer getDorsal() { return dorsal; }
        public String getPosicion() { return posicion; }
        public String getTitular() { return titular; }
        public String getConvocado() { return convocado; }
    }
}

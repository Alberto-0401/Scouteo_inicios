package com.javafx.scouteo.controller;

import com.javafx.scouteo.model.Jugador;
import com.javafx.scouteo.model.Partido;
import com.javafx.scouteo.dao.PartidoDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;

public class PartidosController {

    @FXML
    private TableView<Partido> tablaPartidos;

    @FXML
    private TableColumn<Partido, Integer> colId;

    @FXML
    private TableColumn<Partido, LocalDate> colFecha;

    @FXML
    private TableColumn<Partido, String> colRival;

    @FXML
    private TableColumn<Partido, String> colResultado;

    @FXML
    private TableColumn<Partido, String> colLocalVisitante;

    @FXML
    private TableColumn<Partido, Void> colAcciones;

    @FXML
    private Label lblTotal;

    private ObservableList<Partido> listaPartidos;
    private PartidoDAO partidoDAO;
    private Jugador jugadorActual;

    @FXML
    public void initialize() {
        partidoDAO = new PartidoDAO();
        configurarTabla();
        cargarPartidos();
    }

    public void cargarEstadisticasJugador(Jugador jugador) {
        this.jugadorActual = jugador;
        // Aquí puedes implementar lógica adicional para filtrar partidos por jugador
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idPartido"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaPartido"));
        colRival.setCellValueFactory(new PropertyValueFactory<>("rival"));
        colResultado.setCellValueFactory(new PropertyValueFactory<>("resultado"));
        colLocalVisitante.setCellValueFactory(new PropertyValueFactory<>("localVisitante"));

        // Columna de acciones con botón
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnAnadirEstadisticas = new Button("+ Estadísticas");

            {
                btnAnadirEstadisticas.setOnAction(event -> {
                    Partido partido = getTableView().getItems().get(getIndex());
                    abrirFormEstadisticas(partido);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnAnadirEstadisticas);
                }
            }
        });
    }

    private void cargarPartidos() {
        listaPartidos = FXCollections.observableArrayList(partidoDAO.obtenerTodos());
        tablaPartidos.setItems(listaPartidos);
        actualizarTotal();
    }

    private void actualizarTotal() {
        lblTotal.setText("Total: " + listaPartidos.size() + " partidos");
    }

    private void abrirFormEstadisticas(Partido partido) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FormEstadistica.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Añadir Estadísticas - " + partido.getRival());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Clase interna para representar estadísticas de un partido
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

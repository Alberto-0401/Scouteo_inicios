package com.javafx.scouteo.controller;

import com.javafx.scouteo.model.Jugador;
import com.javafx.scouteo.model.Partido;
import com.javafx.scouteo.model.JugadorPartido;
import com.javafx.scouteo.dao.JugadorDAO;
import com.javafx.scouteo.dao.PartidoDAO;
import com.javafx.scouteo.dao.JugadorPartidoDAO;
import com.javafx.scouteo.dao.EstadisticaPartidoDAO;
import com.javafx.scouteo.utils.StageUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.List;

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
    private TableView<EstadisticaPartidoVista> tablaEstadisticas;

    @FXML
    private TableColumn<EstadisticaPartidoVista, String> colFecha;

    @FXML
    private TableColumn<EstadisticaPartidoVista, String> colRival;

    @FXML
    private TableColumn<EstadisticaPartidoVista, String> colResultado;

    @FXML
    private TableColumn<EstadisticaPartidoVista, Integer> colMinutos;

    @FXML
    private TableColumn<EstadisticaPartidoVista, Integer> colGoles;

    @FXML
    private TableColumn<EstadisticaPartidoVista, Integer> colAsistencias;

    @FXML
    private TableColumn<EstadisticaPartidoVista, Integer> colTarjetasA;

    @FXML
    private TableColumn<EstadisticaPartidoVista, Integer> colTarjetasR;

    @FXML
    private TableColumn<EstadisticaPartidoVista, Void> colAcciones;

    @FXML
    private Label lblTotalGoles;

    @FXML
    private Label lblTotalAsistencias;

    @FXML
    private Label lblPromedioGoles;

    private JugadorDAO jugadorDAO;
    private PartidoDAO partidoDAO;
    private JugadorPartidoDAO jugadorPartidoDAO;
    private EstadisticaPartidoDAO estadisticaPartidoDAO;
    private List<Jugador> listaJugadores;
    private Jugador jugadorActual;

    @FXML
    public void initialize() {
        jugadorDAO = new JugadorDAO();
        partidoDAO = new PartidoDAO();
        jugadorPartidoDAO = new JugadorPartidoDAO();
        estadisticaPartidoDAO = new EstadisticaPartidoDAO();

        // Configurar columnas
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colRival.setCellValueFactory(new PropertyValueFactory<>("rival"));
        colResultado.setCellValueFactory(new PropertyValueFactory<>("resultado"));
        colMinutos.setCellValueFactory(new PropertyValueFactory<>("minutos"));
        colGoles.setCellValueFactory(new PropertyValueFactory<>("goles"));
        colAsistencias.setCellValueFactory(new PropertyValueFactory<>("asistencias"));
        colTarjetasA.setCellValueFactory(new PropertyValueFactory<>("tarjetasAmarillas"));
        colTarjetasR.setCellValueFactory(new PropertyValueFactory<>("tarjetasRojas"));

        // Columna de acciones
        configurarColumnaAcciones();

        cargarJugadores();
    }

    private void configurarColumnaAcciones() {
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");

            {
                btnEditar.setStyle("-fx-font-size: 10px; -fx-padding: 2 8;");
                btnEditar.setOnAction(event -> {
                    EstadisticaPartidoVista vista = getTableView().getItems().get(getIndex());
                    abrirFormularioEdicion(vista);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnEditar);
                }
            }
        });
    }

    private void abrirFormularioEdicion(EstadisticaPartidoVista vista) {
        if (jugadorActual == null || vista == null) return;

        // Buscar el partido y la estadistica
        List<JugadorPartido> participaciones = jugadorPartidoDAO.obtenerPorJugador(jugadorActual.getId());

        for (JugadorPartido jp : participaciones) {
            Partido partido = partidoDAO.obtenerPorId(jp.getIdPartido());
            if (partido != null && partido.getFechaPartido().toString().equals(vista.getFecha())
                    && partido.getRival().equals(vista.getRival())) {

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FormEstadistica.fxml"));
                    Stage stage = new Stage();
                    StageUtils.setAppIcon(stage);
                    stage.setScene(new Scene(loader.load()));
                    stage.setTitle("Editar Estadisticas");
                    stage.initModality(Modality.APPLICATION_MODAL);

                    FormEstadisticaController controller = loader.getController();
                    controller.setEstadisticaEditar(jp.getIdEstadistica(), jugadorActual, partido);
                    controller.setOnGuardado(() -> cargarEstadisticasJugador(jugadorActual));

                    stage.showAndWait();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private void cargarJugadores() {
        listaJugadores = jugadorDAO.obtenerTodos();
        ObservableList<String> nombres = FXCollections.observableArrayList();

        for (Jugador j : listaJugadores) {
            nombres.add(j.getDorsal() + " - " + j.getNombre() + " " + j.getApellidos());
        }

        cmbJugador.setItems(nombres);
    }

    public void setJugador(Jugador jugador) {
        this.jugadorActual = jugador;

        if (jugador != null) {
            // Seleccionar en el combo
            String nombreCompleto = jugador.getDorsal() + " - " + jugador.getNombre() + " " + jugador.getApellidos();
            cmbJugador.setValue(nombreCompleto);

            // Actualizar info
            if (lblNombreJugador != null) {
                lblNombreJugador.setText(jugador.getNombre() + " " + jugador.getApellidos());
            }
            if (lblPosicion != null) {
                lblPosicion.setText(jugador.getPosicion());
            }
            if (lblDorsal != null) {
                lblDorsal.setText(String.valueOf(jugador.getDorsal()));
            }

            // Cargar estadisticas
            cargarEstadisticasJugador(jugador);
        }
    }

    @FXML
    private void cargarEstadisticas() {
        String jugadorSeleccionado = cmbJugador.getValue();

        if (jugadorSeleccionado == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aviso");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, selecciona un jugador");

            // Agregar icono a la ventana de alerta
            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
            StageUtils.setAppIcon(alertStage);

            alert.showAndWait();
            return;
        }

        // Buscar jugador por el texto seleccionado
        for (Jugador j : listaJugadores) {
            String nombreCompleto = j.getDorsal() + " - " + j.getNombre() + " " + j.getApellidos();
            if (nombreCompleto.equals(jugadorSeleccionado)) {
                jugadorActual = j;
                break;
            }
        }

        if (jugadorActual != null) {
            // Actualizar labels
            if (lblNombreJugador != null) {
                lblNombreJugador.setText(jugadorActual.getNombre() + " " + jugadorActual.getApellidos());
            }
            if (lblPosicion != null) {
                lblPosicion.setText(jugadorActual.getPosicion());
            }
            if (lblDorsal != null) {
                lblDorsal.setText(String.valueOf(jugadorActual.getDorsal()));
            }

            cargarEstadisticasJugador(jugadorActual);
        }
    }

    private void cargarEstadisticasJugador(Jugador jugador) {
        ObservableList<EstadisticaPartidoVista> datos = FXCollections.observableArrayList();

        // Obtener participaciones del jugador
        List<JugadorPartido> participaciones = jugadorPartidoDAO.obtenerPorJugador(jugador.getId());

        int totalGoles = 0;
        int totalAsistencias = 0;

        for (JugadorPartido jp : participaciones) {
            Partido partido = partidoDAO.obtenerPorId(jp.getIdPartido());
            com.javafx.scouteo.model.EstadisticaPartido est = estadisticaPartidoDAO.obtenerPorId(jp.getIdEstadistica());

            if (partido != null && est != null) {
                EstadisticaPartidoVista vista = new EstadisticaPartidoVista(
                    partido.getFechaPartido().toString(),
                    partido.getRival(),
                    partido.getResultado(),
                    est.getMinutosJugados(),
                    est.getGoles() != null ? est.getGoles() : 0,
                    est.getAsistencias() != null ? est.getAsistencias() : 0,
                    est.getTarjetasAmarillas() != null ? est.getTarjetasAmarillas() : 0,
                    est.getTarjetasRojas() != null ? est.getTarjetasRojas() : 0
                );
                datos.add(vista);

                totalGoles += est.getGoles() != null ? est.getGoles() : 0;
                totalAsistencias += est.getAsistencias() != null ? est.getAsistencias() : 0;
            }
        }

        tablaEstadisticas.setItems(datos);

        // Actualizar totales
        int totalPartidos = datos.size();
        double promedio = totalPartidos > 0 ? (double) totalGoles / totalPartidos : 0.0;

        if (lblTotalPartidos != null) lblTotalPartidos.setText(String.valueOf(totalPartidos));
        if (lblTotalGoles != null) lblTotalGoles.setText(String.valueOf(totalGoles));
        if (lblTotalAsistencias != null) lblTotalAsistencias.setText(String.valueOf(totalAsistencias));
        if (lblPromedioGoles != null) lblPromedioGoles.setText(String.format("%.2f", promedio));
    }

    private DashboardController dashboardController;

    public void setDashboardController(DashboardController controller) {
        this.dashboardController = controller;
    }

    @FXML
    private void volverDashboard() {
        if (dashboardController != null) {
            dashboardController.mostrarListadoJugadores();
        }
    }

    // Clase interna para la vista de estadisticas
    public static class EstadisticaPartidoVista {
        private String fecha;
        private String rival;
        private String resultado;
        private Integer minutos;
        private Integer goles;
        private Integer asistencias;
        private Integer tarjetasAmarillas;
        private Integer tarjetasRojas;

        public EstadisticaPartidoVista(String fecha, String rival, String resultado,
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

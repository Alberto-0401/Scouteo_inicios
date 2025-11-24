package com.javafx.scouteo.controller;

import com.javafx.scouteo.model.Jugador;
import com.javafx.scouteo.dao.JugadorDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import java.io.IOException;

public class ListadoJugadoresController {

    private DashboardController dashboardController;
    private JugadorDAO jugadorDAO;

    @FXML
    private ComboBox<String> cmbPosicion;

    @FXML
    private TableColumn<Jugador, Void> colAcciones;

    @FXML
    private TableColumn<Jugador, String> colApellidos;

    @FXML
    private TableColumn<Jugador, String> colCategoria;

    @FXML
    private TableColumn<Jugador, Integer> colDorsal;

    @FXML
    private TableColumn<Jugador, Integer> colEdad;

    @FXML
    private TableColumn<Jugador, Integer> colId;

    @FXML
    private TableColumn<Jugador, String> colNombre;

    @FXML
    private TableColumn<Jugador, String> colPosicion;

    @FXML
    private Label lblTotal;

    @FXML
    private TableView<Jugador> tablaJugadores;

    @FXML
    private TextField txtBuscar;

    private ObservableList<Jugador> listaJugadores;
    private FilteredList<Jugador> listaFiltrada;

    @FXML
    public void initialize() {
        jugadorDAO = new JugadorDAO();
        configurarTabla();
        cargarJugadores();
        configurarFiltros();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colDorsal.setCellValueFactory(new PropertyValueFactory<>("dorsal"));
        colPosicion.setCellValueFactory(new PropertyValueFactory<>("posicion"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colEdad.setCellValueFactory(new PropertyValueFactory<>("edad"));

        // Configurar columna de acciones con botones
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEstadisticas = new Button("\ud83d\udcca");
            private final Button btnEditar = new Button("\u270f\ufe0f");
            private final Button btnEliminar = new Button("\ud83d\uddd1\ufe0f");
            private final HBox contenedor = new HBox(5, btnEstadisticas, btnEditar, btnEliminar);

            {

                btnEstadisticas.setOnAction(event -> {
                    Jugador jugador = getTableView().getItems().get(getIndex());
                    abrirEstadisticas(jugador);
                });

                btnEditar.setOnAction(event -> {
                    Jugador jugador = getTableView().getItems().get(getIndex());
                    abrirFormularioJugador(jugador);
                });

                btnEliminar.setOnAction(event -> {
                    Jugador jugador = getTableView().getItems().get(getIndex());
                    eliminarJugador(jugador);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(contenedor);
                }
            }
        });
    }

    private void configurarFiltros() {
        // Listener para busqueda por texto
        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            aplicarFiltros();
        });

        // Listener para filtro por posicion
        cmbPosicion.valueProperty().addListener((observable, oldValue, newValue) -> {
            aplicarFiltros();
        });

        // Valor inicial del ComboBox
        cmbPosicion.setValue("Todas");
    }

    @FXML
    private void aplicarFiltros() {
        if (listaFiltrada == null) return;

        String textoBusqueda = txtBuscar.getText() != null ? txtBuscar.getText().toLowerCase().trim() : "";
        String posicionSeleccionada = cmbPosicion.getValue();

        listaFiltrada.setPredicate(jugador -> {
            // Filtro por texto (nombre o apellidos)
            boolean coincideTexto = textoBusqueda.isEmpty() ||
                    jugador.getNombre().toLowerCase().contains(textoBusqueda) ||
                    jugador.getApellidos().toLowerCase().contains(textoBusqueda) ||
                    String.valueOf(jugador.getDorsal()).contains(textoBusqueda);

            // Filtro por posicion
            boolean coincidePosicion = posicionSeleccionada == null ||
                    posicionSeleccionada.equals("Todas") ||
                    jugador.getPosicion().equals(posicionSeleccionada);

            return coincideTexto && coincidePosicion;
        });

        actualizarTotal();
    }

    @FXML
    private void limpiarFiltros() {
        txtBuscar.clear();
        cmbPosicion.setValue("Todas");
        aplicarFiltros();
    }

    public void cargarJugadores() {
        listaJugadores = FXCollections.observableArrayList(jugadorDAO.obtenerTodos());
        listaFiltrada = new FilteredList<>(listaJugadores, p -> true);
        tablaJugadores.setItems(listaFiltrada);
        aplicarFiltros();
        actualizarTotal();
    }

    private void actualizarTotal() {
        int total = listaFiltrada != null ? listaFiltrada.size() : 0;
        lblTotal.setText("Total: " + total + " jugadores");
    }

    @FXML
    void abrirFormularioNuevo(ActionEvent event) {
        abrirFormularioJugador(null);
    }

    private void abrirFormularioJugador(Jugador jugador) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FormJugador.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle(jugador == null ? "Nuevo Jugador" : "Editar Jugador");
            stage.initModality(Modality.APPLICATION_MODAL);

            FormJugadorController controller = loader.getController();
            controller.setListadoController(this);
            if (jugador != null) {
                controller.setJugadorEditar(jugador);
            }

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void eliminarJugador(Jugador jugador) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminacion");
        alert.setHeaderText("Eliminar jugador");
        alert.setContentText("¿Esta seguro de eliminar a " + jugador.getNombre() + " " + jugador.getApellidos() + "?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (jugadorDAO.eliminar(jugador.getId())) {
                cargarJugadores();
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Error");
                error.setContentText("No se pudo eliminar el jugador");
                error.showAndWait();
            }
        }
    }

    private void abrirEstadisticas(Jugador jugador) {
        if (dashboardController != null) {
            dashboardController.mostrarEstadisticasJugador(jugador);
        } else {
            // Abrir en ventana modal si no hay dashboard
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EstadisticasJugador.fxml"));
                Stage stage = new Stage();
                stage.setScene(new Scene(loader.load()));
                stage.setTitle("Estadisticas de " + jugador.getNombre() + " " + jugador.getApellidos());
                stage.initModality(Modality.APPLICATION_MODAL);

                EstadisticasJugadorController controller = loader.getController();
                controller.setJugador(jugador);

                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setDashboardController(DashboardController controller) {
        this.dashboardController = controller;
    }
}

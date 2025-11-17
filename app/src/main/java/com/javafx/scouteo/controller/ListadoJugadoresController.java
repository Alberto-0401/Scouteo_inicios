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

    @FXML
    public void initialize() {
        jugadorDAO = new JugadorDAO();
        configurarTabla();
        cargarJugadores();
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
            private final Button btnEstadisticas = new Button("📊");
            private final Button btnEditar = new Button("✏️");
            private final Button btnEliminar = new Button("🗑️");
            private final HBox contenedor = new HBox(5, btnEstadisticas, btnEditar, btnEliminar);

            {
                btnEstadisticas.setOnAction(event -> {
                    System.out.println("Ver estadísticas (no implementado)");
                });

                btnEditar.setOnAction(event -> {
                    System.out.println("Editar (no implementado)");
                });

                btnEliminar.setOnAction(event -> {
                    System.out.println("Eliminar (no implementado)");
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

    private void cargarJugadores() {
        listaJugadores = FXCollections.observableArrayList(jugadorDAO.obtenerTodos());
        tablaJugadores.setItems(listaJugadores);
        actualizarTotal();
    }

    private void actualizarTotal() {
        lblTotal.setText("Total: " + listaJugadores.size() + " jugadores");
    }

    @FXML
    void abrirFormularioNuevo(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FormJugador.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Nuevo Jugador");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void abrirEstadisticas(Jugador jugador) {
        if (dashboardController != null) {
            dashboardController.mostrarPartidosConJugador(jugador);
        } else {
            System.out.println("No se pudo acceder al Dashboard");
        }
    }

    public void setDashboardController(DashboardController controller) {
        this.dashboardController = controller;
    }
}

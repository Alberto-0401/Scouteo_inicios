package com.javafx.scouteo.controller;

import com.javafx.scouteo.model.Jugador;
import com.javafx.scouteo.dao.JugadorDAO;
import com.javafx.scouteo.utils.StageUtils;
import com.javafx.scouteo.util.ConexionBD;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import java.io.ByteArrayInputStream;
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
    private TableColumn<Jugador, Void> colFoto;

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
        // Configurar columna de foto
        colFoto.setCellFactory(param -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitHeight(50);
                imageView.setFitWidth(50);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Jugador jugador = getTableRow().getItem();
                    if (jugador.getFoto() != null && jugador.getFoto().length > 0) {
                        try {
                            Image image = new Image(new ByteArrayInputStream(jugador.getFoto()));
                            imageView.setImage(image);
                            setGraphic(imageView);
                        } catch (Exception e) {
                            setGraphic(null);
                        }
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colDorsal.setCellValueFactory(new PropertyValueFactory<>("dorsal"));
        colPosicion.setCellValueFactory(new PropertyValueFactory<>("posicion"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colEdad.setCellValueFactory(new PropertyValueFactory<>("edad"));

        // Configurar columna de acciones con botones
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEstadisticas = new Button("📊");  // 📊 Estadísticas
            private final Button btnEditar = new Button("\u270E");        // ✎ Editar
            private final Button btnEliminar = new Button("\u2716");      // ✖ Eliminar
            private final HBox contenedor = new HBox(5, btnEstadisticas, btnEditar, btnEliminar);

            {
                // Agregar tooltips a los botones
                Tooltip.install(btnEstadisticas, new Tooltip("Ver estadísticas del jugador"));
                Tooltip.install(btnEditar, new Tooltip("Editar información del jugador"));
                Tooltip.install(btnEliminar, new Tooltip("Eliminar jugador"));

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
        // Verificar conexión a la base de datos
        if (!ConexionBD.isConexionValida()) {
            Label errorLabel = new Label("❌ No es posible conectar con la base de datos");
            errorLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 14px; -fx-font-weight: bold;");
            tablaJugadores.setPlaceholder(errorLabel);
            tablaJugadores.setItems(FXCollections.observableArrayList());
            lblTotal.setText("Total: 0 jugadores");
            return;
        }

        listaJugadores = FXCollections.observableArrayList(jugadorDAO.obtenerTodos());
        listaFiltrada = new FilteredList<>(listaJugadores, p -> true);
        tablaJugadores.setItems(listaFiltrada);

        // Placeholder para cuando no hay jugadores pero la conexión es válida
        if (listaJugadores.isEmpty()) {
            Label emptyLabel = new Label("No hay jugadores registrados");
            emptyLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");
            tablaJugadores.setPlaceholder(emptyLabel);
        }

        aplicarFiltros();
        actualizarTotal();

        // Actualizar el dashboard si existe
        if (dashboardController != null) {
            dashboardController.actualizarDatos();
        }
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
            StageUtils.setAppIcon(stage);
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

        // Agregar icono a la ventana de alerta
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        StageUtils.setAppIcon(alertStage);

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (jugadorDAO.eliminar(jugador.getId())) {
                cargarJugadores();
                // Actualizar el dashboard si existe
                if (dashboardController != null) {
                    dashboardController.actualizarDatos();
                }
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Error");
                error.setContentText("No se pudo eliminar el jugador");

                // Agregar icono a la ventana de error
                Stage errorStage = (Stage) error.getDialogPane().getScene().getWindow();
                StageUtils.setAppIcon(errorStage);

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
                StageUtils.setAppIcon(stage);
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

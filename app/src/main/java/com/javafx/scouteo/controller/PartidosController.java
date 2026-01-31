package com.javafx.scouteo.controller;

import com.javafx.scouteo.model.Jugador;
import com.javafx.scouteo.model.Partido;
import com.javafx.scouteo.dao.PartidoDAO;
import com.javafx.scouteo.utils.StageUtils;
import com.javafx.scouteo.utils.TooltipUtils;
import com.javafx.scouteo.util.ConexionBD;
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

        // Columna de acciones con botones
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEstadisticas = new Button("📊");  // 📊 Estadísticas
            private final Button btnEditar = new Button("\u270E");        // ✎ Editar
            private final Button btnEliminar = new Button("\u2716");      // ✖ Eliminar
            private final HBox contenedor = new HBox(5, btnEstadisticas, btnEditar, btnEliminar);

            {
                // Agregar tooltips a los botones
                TooltipUtils.instalarTooltip(btnEstadisticas, "Ver y añadir estadísticas del partido");
                TooltipUtils.instalarTooltip(btnEditar, "Editar información del partido");
                TooltipUtils.instalarTooltip(btnEliminar, "Eliminar partido");

                btnEstadisticas.setOnAction(event -> {
                    Partido partido = getTableView().getItems().get(getIndex());
                    abrirFormEstadisticas(partido);
                });

                btnEditar.setOnAction(event -> {
                    Partido partido = getTableView().getItems().get(getIndex());
                    abrirFormularioEditar(partido);
                });

                btnEliminar.setOnAction(event -> {
                    Partido partido = getTableView().getItems().get(getIndex());
                    eliminarPartido(partido);
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

    public void cargarPartidos() {
        // Verificar conexión a la base de datos
        if (!ConexionBD.isConexionValida()) {
            Label errorLabel = new Label("❌ No es posible conectar con la base de datos");
            errorLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 14px; -fx-font-weight: bold;");
            tablaPartidos.setPlaceholder(errorLabel);
            listaPartidos = FXCollections.observableArrayList();
            tablaPartidos.setItems(listaPartidos);
            lblTotal.setText("Total: 0 partidos");
            return;
        }

        listaPartidos = FXCollections.observableArrayList(partidoDAO.obtenerTodos());
        tablaPartidos.setItems(listaPartidos);

        // Placeholder para cuando no hay partidos pero la conexión es válida
        if (listaPartidos.isEmpty()) {
            Label emptyLabel = new Label("No hay partidos registrados");
            emptyLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");
            tablaPartidos.setPlaceholder(emptyLabel);
        }

        actualizarTotal();
    }

    private void actualizarTotal() {
        lblTotal.setText("Total: " + listaPartidos.size() + " partidos");
    }

    @FXML
    private void abrirFormularioNuevo() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FormPartido.fxml"));
            Stage stage = new Stage();
            StageUtils.setAppIcon(stage);
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Nuevo Partido");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            FormPartidoController controller = loader.getController();
            controller.setPartidosController(this);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void abrirFormularioEditar(Partido partido) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FormPartido.fxml"));
            Stage stage = new Stage();
            StageUtils.setAppIcon(stage);
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Editar Partido");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            FormPartidoController controller = loader.getController();
            controller.setPartidosController(this);
            controller.setPartidoEditar(partido);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void eliminarPartido(Partido partido) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("Eliminar partido");
        alert.setContentText("¿Eliminar el partido contra " + partido.getRival() + "?\nEsto también eliminará las convocatorias y estadísticas asociadas.");

        // Agregar icono a la ventana de alerta
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        StageUtils.setAppIcon(alertStage);

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (partidoDAO.eliminar(partido.getIdPartido())) {
                cargarPartidos();
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Error");
                error.setContentText("Error al eliminar el partido");

                // Agregar icono a la ventana de error
                Stage errorStage = (Stage) error.getDialogPane().getScene().getWindow();
                StageUtils.setAppIcon(errorStage);

                error.showAndWait();
            }
        }
    }

    private void abrirFormEstadisticas(Partido partido) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FormEstadisticasPartido.fxml"));
            Stage stage = new Stage();
            StageUtils.setAppIcon(stage);
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Añadir Estadísticas - " + partido.getRival());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            FormEstadisticasPartidoController controller = loader.getController();
            controller.setPartido(partido);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

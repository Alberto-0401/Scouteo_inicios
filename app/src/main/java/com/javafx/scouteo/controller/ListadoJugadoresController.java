package com.javafx.scouteo.controller;

import com.javafx.scouteo.model.Equipo;
import com.javafx.scouteo.model.Jugador;
import com.javafx.scouteo.model.Usuario;
import com.javafx.scouteo.dao.JugadorDAO;
import com.javafx.scouteo.dao.UsuarioDAO;
import com.javafx.scouteo.utils.StageUtils;
import com.javafx.scouteo.utils.TooltipUtils;
import com.javafx.scouteo.util.ApiClient;
import com.javafx.scouteo.util.SesionUsuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
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
import java.io.IOException;
import java.util.List;

public class ListadoJugadoresController {

    private DashboardController dashboardController;
    private JugadorDAO jugadorDAO;
    private Equipo equipoActivo;

    @FXML
    private ComboBox<String> cmbPosicion;

    @FXML private TableColumn<Jugador, Void>    colAcciones;
    @FXML private TableColumn<Jugador, String>  colApellidos;
    @FXML private TableColumn<Jugador, String>  colCategoria;
    @FXML private TableColumn<Jugador, Integer> colDorsal;
    @FXML private TableColumn<Jugador, Integer> colEdad;
    @FXML private TableColumn<Jugador, Void>    colFoto;
    @FXML private TableColumn<Jugador, String>  colNombre;
    @FXML private TableColumn<Jugador, String>  colPosicion;
    @FXML private TableColumn<Jugador, String>  colEstado;

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
            private final Label placeholderLabel = new Label("👤");

            {
                imageView.setFitHeight(50);
                imageView.setFitWidth(50);
                imageView.setPreserveRatio(true);

                placeholderLabel.setStyle("-fx-font-size: 30px; -fx-text-fill: #BDBDBD;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Jugador jugador = getTableRow().getItem();
                    String fotoUrl = jugador.getFotoUrl();
                    if (fotoUrl != null && !fotoUrl.isEmpty()) {
                        try {
                            String uri = fotoUrl.startsWith("http") ? fotoUrl : new java.io.File(fotoUrl).toURI().toString();
                            Image image = new Image(uri, true);
                            imageView.setImage(image);
                            setGraphic(imageView);
                        } catch (Exception e) {
                            setGraphic(placeholderLabel);
                        }
                    } else {
                        // Si no hay foto, mostrar icono predeterminado
                        setGraphic(placeholderLabel);
                    }
                }
            }
        });

        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colDorsal.setCellValueFactory(new PropertyValueFactory<>("dorsal"));
        colPosicion.setCellValueFactory(new PropertyValueFactory<>("posicion"));
        colPosicion.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String pos, boolean empty) {
                super.updateItem(pos, empty);
                setText(empty || pos == null ? null : posicionDisplay(pos));
            }
        });
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("nombreEquipo"));
        colEdad.setCellValueFactory(new PropertyValueFactory<>("edad"));

        // Columna Estado - badge de color
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) { setText(null); setStyle(""); return; }
                setText(estadoDisplay(estado));
                switch (estado) {
                    case "activo":
                        setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold; -fx-background-color: #E8F5E9; -fx-background-radius: 20; -fx-padding: 2 8;");
                        break;
                    case "lesionado":
                        setStyle("-fx-text-fill: #C62828; -fx-font-weight: bold; -fx-background-color: #FFEBEE; -fx-background-radius: 20; -fx-padding: 2 8;");
                        break;
                    case "sancionado":
                        setStyle("-fx-text-fill: #E65100; -fx-font-weight: bold; -fx-background-color: #FFF3E0; -fx-background-radius: 20; -fx-padding: 2 8;");
                        break;
                    default:
                        setStyle("-fx-text-fill: #546E7A; -fx-font-weight: bold; -fx-background-color: #ECEFF1; -fx-background-radius: 20; -fx-padding: 2 8;");
                }
            }
        });

        // Configurar columna de acciones con botones
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEstadisticas = new Button("\uD83D\uDCCA");
            private final Button btnEditar       = new Button("\u270E");
            private final Button btnEstado       = new Button("\u21C4");
            private final Button btnCuenta       = new Button("\uD83D\uDC64");
            private final Button btnEliminar     = new Button("\u2716");
            private final HBox contenedor = new HBox(4, btnEstadisticas, btnEditar, btnEstado, btnCuenta, btnEliminar);

            {
                btnEliminar.getStyleClass().setAll("btn-peligro");

                TooltipUtils.instalarTooltip(btnEstadisticas, "Ver estadisticas del jugador");
                TooltipUtils.instalarTooltip(btnEditar,       "Editar informacion del jugador");
                TooltipUtils.instalarTooltip(btnEstado,       "Cambiar estado (activo/lesionado/sancionado/baja)");
                TooltipUtils.instalarTooltip(btnCuenta,       "Crear/ver cuenta de acceso movil");
                TooltipUtils.instalarTooltip(btnEliminar,     "Eliminar jugador");

                btnEstadisticas.setOnAction(e -> abrirEstadisticas(getTableView().getItems().get(getIndex())));
                btnEditar.setOnAction(e -> abrirFormularioJugador(getTableView().getItems().get(getIndex())));
                btnEstado.setOnAction(e -> cambiarEstado(getTableView().getItems().get(getIndex())));
                btnCuenta.setOnAction(e -> gestionarCuentaJugador(getTableView().getItems().get(getIndex())));
                btnEliminar.setOnAction(e -> eliminarJugador(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Jugador j = getTableView().getItems().get(getIndex());
                btnCuenta.setStyle(j.getUsuarioId() != null
                    ? "-fx-background-color: #2E7D32; -fx-text-fill: white;"
                    : "-fx-background-color: #C9A84C; -fx-text-fill: #0D1B2A;");
                setGraphic(contenedor);
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
                    posicionSeleccionada.equals(posicionAGrupo(jugador.getPosicion()));

            boolean noEsBaja = !com.javafx.scouteo.util.Preferencias.getOcultarJugadoresBaja()
                    || !"baja".equals(jugador.getEstado());

            return coincideTexto && coincidePosicion && noEsBaja;
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
        if (!ApiClient.getInstance().isDisponible()) {
            Label errorLabel = new Label("❌ No es posible conectar con el servidor");
            errorLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 14px; -fx-font-weight: bold;");
            tablaJugadores.setPlaceholder(errorLabel);
            tablaJugadores.setItems(FXCollections.observableArrayList());
            lblTotal.setText("Total: 0 jugadores");
            return;
        }

        List<Jugador> jugadores = equipoActivo != null
            ? jugadorDAO.obtenerPorEquipo(equipoActivo.getId())
            : jugadorDAO.obtenerPorClub(com.javafx.scouteo.util.SesionUsuario.getInstance().getUsuarioActual().getClubId());
        listaJugadores = FXCollections.observableArrayList(jugadores);
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
            controller.setEquipoActivo(equipoActivo);
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

    private void cambiarEstado(Jugador jugador) {
        ComboBox<String> cmbEstado = new ComboBox<>();
        cmbEstado.getItems().addAll("activo", "lesionado", "sancionado", "baja");
        cmbEstado.setValue(jugador.getEstado() != null ? jugador.getEstado() : "activo");
        cmbEstado.setPrefWidth(200);

        javafx.scene.layout.VBox contenido = new javafx.scene.layout.VBox(10,
                new Label("Jugador: " + jugador.getNombreCompleto()),
                new Label("Estado actual: " + estadoDisplay(jugador.getEstado())),
                new Label("Nuevo estado:"), cmbEstado);
        contenido.setStyle("-fx-padding: 20;");

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Cambiar estado");
        dialog.setHeaderText(jugador.getNombreCompleto());
        dialog.getDialogPane().setContent(contenido);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getClassLoader().getResource("scouteo.css").toExternalForm());
        dialog.setOnShowing(e ->
                com.javafx.scouteo.utils.StageUtils.setAppIcon(
                        (Stage) dialog.getDialogPane().getScene().getWindow()));

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String nuevoEstado = cmbEstado.getValue();
            if (jugadorDAO.cambiarEstado(jugador.getId(), nuevoEstado)) {
                jugador.setEstado(nuevoEstado);
                tablaJugadores.refresh();

                if ("lesionado".equals(nuevoEstado) && dashboardController != null) {
                    Alert a = new Alert(Alert.AlertType.CONFIRMATION);
                    a.setTitle("Registrar lesion");
                    a.setHeaderText(null);
                    a.setContentText("Jugador marcado como lesionado.\n¿Quieres ir al Historial Medico para registrar los detalles?");
                    com.javafx.scouteo.utils.StageUtils.setAppIcon(
                            (Stage) a.getDialogPane().getScene().getWindow());
                    if (a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                        dashboardController.mostrarHistorialMedico();
                    }
                }
            }
        }
    }

    private void gestionarCuentaJugador(Jugador jugador) {
        if (jugador.getUsuarioId() != null) {
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Cuenta existente");
            info.setHeaderText(jugador.getNombreCompleto());
            info.setContentText("Este jugador ya tiene cuenta de acceso movil.\nID de usuario: " + jugador.getUsuarioId());
            StageUtils.setAppIcon((Stage) info.getDialogPane().getScene().getWindow());
            info.showAndWait();
            return;
        }

        // Campos del formulario
        TextField txtEmail    = new TextField(jugador.getNombre().toLowerCase().replace(" ", ".") + "." + jugador.getApellidos().split(" ")[0].toLowerCase() + "@fcbarcelona.cat");
        PasswordField txtPass = new PasswordField();
        txtPass.setText("Scouteo2024!");
        txtPass.setPromptText("Contraseña");

        javafx.scene.layout.VBox contenido = new javafx.scene.layout.VBox(8,
            new Label("Jugador: " + jugador.getNombreCompleto()),
            new Label("Email:"), txtEmail,
            new Label("Contraseña:"), txtPass);
        contenido.setStyle("-fx-padding: 20;");

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Crear cuenta movil");
        dialog.setHeaderText("Nueva cuenta para " + jugador.getNombreCompleto());
        dialog.getDialogPane().setContent(contenido);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(
            getClass().getClassLoader().getResource("scouteo.css").toExternalForm());
        dialog.setOnShowing(e ->
            StageUtils.setAppIcon((Stage) dialog.getDialogPane().getScene().getWindow()));

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        String email    = txtEmail.getText().trim().toLowerCase();
        String password = txtPass.getText();

        if (email.isEmpty() || password.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Email y contraseña son obligatorios.").showAndWait();
            return;
        }

        UsuarioDAO usuarioDAO = new UsuarioDAO();
        if (usuarioDAO.existeEmail(email)) {
            new Alert(Alert.AlertType.ERROR, "Ya existe un usuario con ese email.").showAndWait();
            return;
        }

        Usuario nuevo = new Usuario();
        nuevo.setClubId(SesionUsuario.getInstance().getUsuarioActual().getClubId());
        nuevo.setEmail(email);
        nuevo.setRol("jugador");
        nuevo.setNombre(jugador.getNombre());
        nuevo.setApellidos(jugador.getApellidos());

        int nuevoId = usuarioDAO.insertar(nuevo, password);
        if (nuevoId < 0 || !jugadorDAO.vincularUsuario(jugador.getId(), nuevoId)) {
            new Alert(Alert.AlertType.ERROR, "Error al crear la cuenta. Intentalo de nuevo.").showAndWait();
            return;
        }

        jugador.setUsuarioId(nuevoId);
        tablaJugadores.refresh();

        Alert ok = new Alert(Alert.AlertType.INFORMATION);
        ok.setTitle("Cuenta creada");
        ok.setHeaderText("Cuenta creada correctamente");
        ok.setContentText("Email: " + email + "\nContraseña: " + password + "\n\nComparte estos datos con el jugador para que acceda a la app movil.");
        StageUtils.setAppIcon((Stage) ok.getDialogPane().getScene().getWindow());
        ok.showAndWait();
    }

    private String posicionDisplay(String posicion) {
        if (posicion == null) return "";
        switch (posicion) {
            case "portero":           return "Portero";
            case "defensa_central":   return "Defensa Central";
            case "lateral_derecho":   return "Lateral Der.";
            case "lateral_izquierdo": return "Lateral Izq.";
            case "mediocentro":       return "Mediocentro";
            case "medio_derecho":     return "Medio Der.";
            case "medio_izquierdo":   return "Medio Izq.";
            case "mediapunta":        return "Mediapunta";
            case "extremo_derecho":   return "Extremo Der.";
            case "extremo_izquierdo": return "Extremo Izq.";
            case "delantero_centro":  return "Delantero";
            default: return posicion;
        }
    }

    private String posicionAGrupo(String posicion) {
        if (posicion == null) return "";
        switch (posicion) {
            case "portero":
                return "POR";
            case "defensa_central": case "lateral_derecho": case "lateral_izquierdo":
                return "DEF";
            case "mediocentro": case "medio_derecho": case "medio_izquierdo": case "mediapunta":
                return "MED";
            case "extremo_derecho": case "extremo_izquierdo": case "delantero_centro":
                return "DEL";
            default:
                return posicion;
        }
    }

    private String estadoDisplay(String estado) {
        if (estado == null) return "Activo";
        switch (estado) {
            case "activo":    return "Activo";
            case "lesionado": return "Lesionado";
            case "sancionado":return "Sancionado";
            case "baja":      return "Baja";
            default:          return estado;
        }
    }

    public void setDashboardController(DashboardController controller) {
        this.dashboardController = controller;
    }

    public void setEquipoActivo(Equipo equipo) {
        this.equipoActivo = equipo;
        if (equipo != null) {
            listaJugadores = javafx.collections.FXCollections.observableArrayList(jugadorDAO.obtenerPorEquipo(equipo.getId()));
            listaFiltrada = new javafx.collections.transformation.FilteredList<>(listaJugadores, p -> true);
            tablaJugadores.setItems(listaFiltrada);
            aplicarFiltros();
            actualizarTotal();
        }
    }
}

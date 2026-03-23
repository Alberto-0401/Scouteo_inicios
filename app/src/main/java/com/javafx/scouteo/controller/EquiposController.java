package com.javafx.scouteo.controller;

import com.javafx.scouteo.dao.EquipoDAO;
import com.javafx.scouteo.dao.JugadorDAO;
import com.javafx.scouteo.dao.UsuarioDAO;
import com.javafx.scouteo.model.Equipo;
import com.javafx.scouteo.model.Usuario;
import com.javafx.scouteo.util.ConexionBD;
import com.javafx.scouteo.util.SesionUsuario;
import com.javafx.scouteo.utils.StageUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class EquiposController {

    @FXML private TableView<Equipo> tablaEquipos;
    @FXML private TableColumn<Equipo, String> colNombre;
    @FXML private TableColumn<Equipo, String> colCategoria;
    @FXML private TableColumn<Equipo, String> colTemporada;
    @FXML private TableColumn<Equipo, String> colCampo;
    @FXML private TableColumn<Equipo, Void>   colJugadores;
    @FXML private TableColumn<Equipo, Void>   colAcciones;
    @FXML private Label lblTotal;
    @FXML private Label lblTitulo;
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private ComboBox<String> cmbTemporada;

    private final EquipoDAO  equipoDAO  = new EquipoDAO();
    private final JugadorDAO jugadorDAO = new JugadorDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private ObservableList<Equipo> listaEquipos;
    private FilteredList<Equipo> listaFiltrada;

    @FXML
    public void initialize() {
        boolean esDirectiva = SesionUsuario.getInstance().esDirectiva();
        if (!esDirectiva) lblTitulo.setText("Mis Equipos");

        configurarTabla();
        cargarEquipos();
        configurarFiltros();
    }

    private void configurarFiltros() {
        cmbCategoria.getItems().add("Todas");
        cmbCategoria.getItems().addAll("benjamin","alevin","infantil","cadete","juvenil","senior","veteranos");
        cmbCategoria.setValue("Todas");

        txtBuscar.textProperty().addListener((obs, o, n) -> aplicarFiltros());
        cmbCategoria.valueProperty().addListener((obs, o, n) -> aplicarFiltros());
        cmbTemporada.valueProperty().addListener((obs, o, n) -> aplicarFiltros());
    }

    private void aplicarFiltros() {
        if (listaFiltrada == null) return;
        String texto = txtBuscar.getText() != null ? txtBuscar.getText().toLowerCase().trim() : "";
        String cat   = cmbCategoria.getValue();
        String temp  = cmbTemporada.getValue();

        listaFiltrada.setPredicate(eq -> {
            boolean matchTexto = texto.isEmpty() || eq.getNombre().toLowerCase().contains(texto);
            boolean matchCat   = cat == null || cat.equals("Todas") || cat.equals(eq.getCategoria());
            boolean matchTemp  = temp == null || temp.equals("Todas") || temp.equals(eq.getTemporada());
            return matchTexto && matchCat && matchTemp;
        });

        lblTotal.setText("Total: " + listaFiltrada.size() + " equipos");
    }

    @FXML
    private void limpiarFiltros() {
        txtBuscar.clear();
        cmbCategoria.setValue("Todas");
        cmbTemporada.setValue("Todas");
    }

    private void configurarTabla() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTemporada.setCellValueFactory(new PropertyValueFactory<>("temporada"));

        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoriaDisplay"));

        // Columna de jugadores (conteo)
        colJugadores.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Equipo eq = getTableRow().getItem();
                int count = jugadorDAO.contarPorEquipo(eq.getId());
                Label lbl = new Label(String.valueOf(count));
                lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #C9A84C;");
                setGraphic(lbl);
            }
        });

        // Columna de acciones
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnStaff   = new Button("\uD83D\uDC65");
            private final Button btnEditar   = new Button("\u270E");
            private final Button btnEliminar = new Button("\u2716");
            private final HBox   contenedor  = new HBox(4, btnStaff, btnEditar, btnEliminar);

            {
                btnStaff.setTooltip(new Tooltip("Gestionar entrenadores"));
                btnStaff.setOnAction(e -> {
                    Equipo eq = getTableView().getItems().get(getIndex());
                    abrirGestionStaff(eq);
                });
                btnEditar.setOnAction(e -> {
                    Equipo eq = getTableView().getItems().get(getIndex());
                    abrirFormularioEditar(eq);
                });
                btnEliminar.setOnAction(e -> {
                    Equipo eq = getTableView().getItems().get(getIndex());
                    eliminarEquipo(eq);
                });
                boolean puedeMod = SesionUsuario.getInstance().esDirectiva();
                btnEditar.setDisable(!puedeMod);
                btnEliminar.setDisable(!puedeMod);
                btnStaff.setDisable(!puedeMod);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : contenedor);
            }
        });

        colCampo.setCellValueFactory(new PropertyValueFactory<>("campo"));
    }

    public void cargarEquipos() {
        if (!ConexionBD.isConexionValida()) {
            tablaEquipos.setPlaceholder(new Label("Sin conexion a la base de datos"));
            return;
        }

        List<Equipo> equipos;
        SesionUsuario sesion = SesionUsuario.getInstance();
        if (sesion.esDirectiva()) {
            equipos = equipoDAO.obtenerPorClub(sesion.getUsuarioActual().getClubId());
        } else {
            equipos = equipoDAO.obtenerPorEntrenador(sesion.getUsuarioActual().getId());
        }

        listaEquipos = FXCollections.observableArrayList(equipos);
        listaFiltrada = new FilteredList<>(listaEquipos, p -> true);
        tablaEquipos.setItems(listaFiltrada);

        // Poblar combo de temporadas con los valores distintos que existen
        if (cmbTemporada != null) {
            String selTemp = cmbTemporada.getValue();
            cmbTemporada.getItems().clear();
            cmbTemporada.getItems().add("Todas");
            listaEquipos.stream()
                .map(Equipo::getTemporada)
                .filter(t -> t != null && !t.isBlank())
                .distinct().sorted()
                .forEach(cmbTemporada.getItems()::add);
            cmbTemporada.setValue(selTemp != null ? selTemp : "Todas");
        }

        lblTotal.setText("Total: " + listaFiltrada.size() + " equipos");
    }

    @FXML
    private void abrirFormularioNuevo() {
        mostrarDialogoEquipo(null);
    }

    private void abrirFormularioEditar(Equipo equipo) {
        mostrarDialogoEquipo(equipo);
    }

    private void mostrarDialogoEquipo(Equipo equipoEditar) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(equipoEditar == null ? "Nuevo Equipo" : "Editar Equipo");
        dialog.setHeaderText(equipoEditar == null ? "Crear nuevo equipo" : "Editar: " + equipoEditar.getNombre());

        TextField txtNombre    = new TextField(equipoEditar != null ? equipoEditar.getNombre() : "");
        ComboBox<String> cmbCategoria = new ComboBox<>();
        cmbCategoria.getItems().addAll("benjamin","alevin","infantil","cadete","juvenil","senior","veteranos");
        if (equipoEditar != null) cmbCategoria.setValue(equipoEditar.getCategoria());
        else cmbCategoria.setValue("senior");
        TextField txtTemporada = new TextField(equipoEditar != null ? equipoEditar.getTemporada() : "2024-25");
        TextField txtCampo     = new TextField(equipoEditar != null ? (equipoEditar.getCampo() != null ? equipoEditar.getCampo() : "") : "");

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setStyle("-fx-padding: 20;");
        grid.add(new Label("Nombre:"),    0, 0); grid.add(txtNombre, 1, 0);
        grid.add(new Label("Categoria:"), 0, 1); grid.add(cmbCategoria, 1, 1);
        grid.add(new Label("Temporada:"), 0, 2); grid.add(txtTemporada, 1, 2);
        grid.add(new Label("Campo:"),     0, 3); grid.add(txtCampo, 1, 3);
        txtNombre.setPrefWidth(250);
        cmbCategoria.setPrefWidth(250);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/scouteo.css").toExternalForm());
        dialog.setOnShowing(e -> StageUtils.setAppIcon((Stage) dialog.getDialogPane().getScene().getWindow()));

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String nombre = txtNombre.getText().trim();
            if (nombre.isEmpty()) { mostrarError("El nombre es obligatorio"); return; }

            Equipo eq = equipoEditar != null ? equipoEditar : new Equipo();
            eq.setNombre(nombre);
            eq.setCategoria(cmbCategoria.getValue());
            eq.setTemporada(txtTemporada.getText().trim());
            eq.setCampo(txtCampo.getText().trim().isEmpty() ? null : txtCampo.getText().trim());
            if (equipoEditar == null) {
                eq.setClubId(SesionUsuario.getInstance().getUsuarioActual().getClubId());
            }

            boolean ok = equipoEditar == null ? equipoDAO.insertar(eq) > 0 : equipoDAO.actualizar(eq);
            if (ok) cargarEquipos();
            else mostrarError("Error al guardar el equipo");
        }
    }

    private void eliminarEquipo(Equipo equipo) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar");
        alert.setHeaderText("Eliminar equipo");
        alert.setContentText("Desactivar el equipo \"" + equipo.getNombre() + "\"?");
        alert.setOnShowing(e -> StageUtils.setAppIcon((Stage) alert.getDialogPane().getScene().getWindow()));
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (equipoDAO.eliminar(equipo.getId())) cargarEquipos();
            else mostrarError("No se pudo eliminar el equipo");
        }
    }

    private void abrirGestionStaff(Equipo equipo) {
        ObservableList<Usuario> staff = FXCollections.observableArrayList(
                equipoDAO.obtenerEntrenadoresPorEquipo(equipo.getId()));

        // Tabla de entrenadores actuales
        TableView<Usuario> tablaStaff = new TableView<>(staff);
        tablaStaff.setPrefHeight(180);
        tablaStaff.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Usuario, String> colNombreS = new TableColumn<>("Nombre");
        colNombreS.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNombreCompleto()));
        TableColumn<Usuario, String> colEmailS = new TableColumn<>("Email");
        colEmailS.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEmail()));
        TableColumn<Usuario, Void> colQuitar = new TableColumn<>("Quitar");
        colQuitar.setPrefWidth(70);
        colQuitar.setCellFactory(p -> new TableCell<>() {
            private final Button btn = new Button("\u2716");
            { btn.setOnAction(e -> {
                Usuario u = getTableView().getItems().get(getIndex());
                equipoDAO.quitarEntrenador(equipo.getId(), u.getId());
                staff.remove(u);
            }); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
        tablaStaff.getColumns().addAll(colNombreS, colEmailS, colQuitar);

        // Formulario para añadir nuevo entrenador
        TextField txtNombreE    = new TextField(); txtNombreE.setPromptText("Nombre");
        TextField txtApellidosE = new TextField(); txtApellidosE.setPromptText("Apellidos");
        TextField txtEmailE     = new TextField(); txtEmailE.setPromptText("correo@club.es");
        PasswordField txtPassE  = new PasswordField(); txtPassE.setPromptText("Contraseña temporal (min. 6 car.)");
        Label lblErrorStaff     = new Label();
        lblErrorStaff.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 11px;");

        javafx.scene.layout.GridPane formGrid = new javafx.scene.layout.GridPane();
        formGrid.setHgap(10); formGrid.setVgap(8);
        formGrid.setStyle("-fx-padding: 12 0 0 0;");
        Label lblSeparador = new Label("── Añadir nuevo entrenador ──");
        lblSeparador.setStyle("-fx-text-fill: #78909C; -fx-font-size: 11px;");
        formGrid.add(lblSeparador,           0, 0, 2, 1);
        formGrid.add(new Label("Nombre:"),   0, 1); formGrid.add(txtNombreE,    1, 1);
        formGrid.add(new Label("Apellidos:"),0, 2); formGrid.add(txtApellidosE, 1, 2);
        formGrid.add(new Label("Email:"),    0, 3); formGrid.add(txtEmailE,     1, 3);
        formGrid.add(new Label("Contraseña:"),0,4); formGrid.add(txtPassE,      1, 4);
        formGrid.add(lblErrorStaff,          0, 5, 2, 1);
        txtNombreE.setPrefWidth(230); txtEmailE.setPrefWidth(230);

        VBox contenido = new VBox(8,
                new Label("Entrenadores asignados:"), tablaStaff, formGrid);
        contenido.setStyle("-fx-padding: 4;");

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Staff — " + equipo.getNombre());
        dialog.setHeaderText("Gestión de entrenadores del equipo");
        dialog.getDialogPane().setContent(contenido);
        dialog.getDialogPane().setPrefWidth(520);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/scouteo.css").toExternalForm());
        dialog.setOnShowing(e -> StageUtils.setAppIcon((Stage) dialog.getDialogPane().getScene().getWindow()));

        // Validar antes de cerrar
        Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            String nombre    = txtNombreE.getText().trim();
            String apellidos = txtApellidosE.getText().trim();
            String email     = txtEmailE.getText().trim();
            String pass      = txtPassE.getText();
            // Si hay algo escrito, todos los campos son obligatorios
            if (!nombre.isEmpty() || !apellidos.isEmpty() || !email.isEmpty() || !pass.isEmpty()) {
                if (nombre.isEmpty() || apellidos.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                    lblErrorStaff.setText("Rellena todos los campos o déjalos vacíos.");
                    e.consume(); return;
                }
                if (pass.length() < 6) {
                    lblErrorStaff.setText("La contraseña debe tener al menos 6 caracteres.");
                    e.consume(); return;
                }
                if (usuarioDAO.existeEmail(email)) {
                    lblErrorStaff.setText("Ese email ya está registrado.");
                    e.consume(); return;
                }
            }
        });

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            String nombre = txtNombreE.getText().trim();
            if (!nombre.isEmpty()) {
                Usuario nuevo = new Usuario();
                nuevo.setNombre(nombre);
                nuevo.setApellidos(txtApellidosE.getText().trim());
                nuevo.setEmail(txtEmailE.getText().trim());
                nuevo.setRol("entrenador");
                nuevo.setActivo(true);
                nuevo.setClubId(SesionUsuario.getInstance().getUsuarioActual().getClubId());
                int id = usuarioDAO.insertar(nuevo, txtPassE.getText());
                if (id > 0) {
                    equipoDAO.asignarEntrenador(equipo.getId(), id, "entrenador");
                } else {
                    mostrarError("Error al crear el entrenador.");
                }
            }
        }
    }

    private void mostrarError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error"); a.setContentText(msg);
        a.setOnShowing(e -> StageUtils.setAppIcon((Stage) a.getDialogPane().getScene().getWindow()));
        a.showAndWait();
    }
}

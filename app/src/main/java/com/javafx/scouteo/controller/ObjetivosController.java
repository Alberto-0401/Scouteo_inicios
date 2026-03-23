package com.javafx.scouteo.controller;

import com.javafx.scouteo.dao.EquipoDAO;
import com.javafx.scouteo.dao.JugadorDAO;
import com.javafx.scouteo.dao.ObjetivoDAO;
import com.javafx.scouteo.model.Equipo;
import com.javafx.scouteo.model.Jugador;
import com.javafx.scouteo.model.Objetivo;
import com.javafx.scouteo.util.SesionUsuario;
import com.javafx.scouteo.utils.StageUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ObjetivosController {

    @FXML private ComboBox<Equipo>    cmbEquipo;
    @FXML private ComboBox<String>    cmbEstado;
    @FXML private ComboBox<String>    cmbPrioridad;
    @FXML private ComboBox<String>    cmbTipo;
    @FXML private TableView<Objetivo> tablaObjetivos;
    @FXML private TableColumn<Objetivo, String>    colTipo;
    @FXML private TableColumn<Objetivo, String>    colObjetivo;
    @FXML private TableColumn<Objetivo, String>    colDescripcion;
    @FXML private TableColumn<Objetivo, String>    colPrioridad;
    @FXML private TableColumn<Objetivo, String>    colEstado;
    @FXML private TableColumn<Objetivo, Integer>   colProgreso;
    @FXML private TableColumn<Objetivo, LocalDate> colFechaLimite;
    @FXML private TableColumn<Objetivo, Void>      colAcciones;
    @FXML private Label lblTotal;

    private final ObjetivoDAO objetivoDAO = new ObjetivoDAO();
    private final EquipoDAO   equipoDAO   = new EquipoDAO();
    private final JugadorDAO  jugadorDAO  = new JugadorDAO();

    private ObservableList<Objetivo> listaCompleta;
    private FilteredList<Objetivo>   listaFiltrada;

    @FXML
    public void initialize() {
        cmbEstado.setItems(FXCollections.observableArrayList("Todos","pendiente","en_progreso","completado","cancelado"));
        cmbEstado.setValue("Todos");
        cmbPrioridad.setItems(FXCollections.observableArrayList("Todos","alta","media","baja"));
        cmbPrioridad.setValue("Todos");
        cmbTipo.setItems(FXCollections.observableArrayList("Todos","Equipo","Individual"));
        cmbTipo.setValue("Todos");

        cargarEquipos();
        configurarTabla();
        configurarFiltros();
    }

    private void cargarEquipos() {
        List<Equipo> equipos;
        SesionUsuario sesion = SesionUsuario.getInstance();
        if (sesion.esDirectiva()) equipos = equipoDAO.obtenerPorClub(sesion.getUsuarioActual().getClubId());
        else equipos = equipoDAO.obtenerPorEntrenador(sesion.getUsuarioActual().getId());

        cmbEquipo.setItems(FXCollections.observableArrayList(equipos));
        if (!equipos.isEmpty()) {
            cmbEquipo.setValue(equipos.get(0));
            cargarObjetivos(equipos.get(0).getId());
        }
        cmbEquipo.setOnAction(e -> { Equipo sel = cmbEquipo.getValue(); if (sel != null) cargarObjetivos(sel.getId()); });
    }

    private void configurarTabla() {
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colObjetivo.setCellValueFactory(new PropertyValueFactory<>("objetivoLabel"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colPrioridad.setCellValueFactory(new PropertyValueFactory<>("prioridadDisplay"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoDisplay"));
        colProgreso.setCellValueFactory(new PropertyValueFactory<>("progresoPct"));
        colFechaLimite.setCellValueFactory(new PropertyValueFactory<>("fechaLimite"));

        // Barra de progreso en columna progreso
        colProgreso.setCellFactory(col -> new TableCell<>() {
            private final ProgressBar pb = new ProgressBar();
            { pb.setPrefWidth(80); pb.setPrefHeight(12); }
            @Override protected void updateItem(Integer val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setGraphic(null); return; }
                pb.setProgress(val / 100.0);
                setGraphic(pb);
            }
        });

        // Color por prioridad
        colPrioridad.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); setStyle(""); return; }
                setText(val);
                setStyle("Alta".equals(val) ? "-fx-text-fill: #d32f2f; -fx-font-weight: bold;"
                    : "Media".equals(val) ? "-fx-text-fill: #FF9800; -fx-font-weight: bold;"
                    : "-fx-text-fill: #4CAF50;");
            }
        });

        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar   = new Button("\u270E");
            private final Button btnEliminar = new Button("\u2716");
            private final HBox   contenedor  = new HBox(4, btnEditar, btnEliminar);

            {
                btnEditar.setOnAction(e -> abrirFormularioEditar(getTableView().getItems().get(getIndex())));
                btnEliminar.setOnAction(e -> eliminar(getTableView().getItems().get(getIndex())));
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : contenedor);
            }
        });
    }

    private void configurarFiltros() {
        cmbEstado.valueProperty().addListener((o, v, n) -> aplicarFiltros());
        cmbPrioridad.valueProperty().addListener((o, v, n) -> aplicarFiltros());
        cmbTipo.valueProperty().addListener((o, v, n) -> aplicarFiltros());
    }

    private void cargarObjetivos(int equipoId) {
        listaCompleta = FXCollections.observableArrayList(objetivoDAO.obtenerTodosPorEquipoOJugadores(equipoId));
        listaFiltrada = new FilteredList<>(listaCompleta, p -> true);
        tablaObjetivos.setItems(listaFiltrada);
        aplicarFiltros();
    }

    @FXML private void limpiarFiltros() {
        cmbEstado.setValue("Todos");
        cmbPrioridad.setValue("Todos");
        cmbTipo.setValue("Todos");
    }

    private void aplicarFiltros() {
        if (listaFiltrada == null) return;
        String estado    = cmbEstado.getValue();
        String prioridad = cmbPrioridad.getValue();
        String tipo      = cmbTipo.getValue();
        listaFiltrada.setPredicate(o -> {
            boolean e = "Todos".equals(estado) || estado == null || estado.equals(o.getEstado());
            boolean p = "Todos".equals(prioridad) || prioridad == null || prioridad.equals(o.getPrioridad());
            boolean t = "Todos".equals(tipo) || tipo == null || tipo.equals(o.getTipo());
            return e && p && t;
        });
        lblTotal.setText("Total: " + listaFiltrada.size() + " objetivos");
    }

    @FXML
    private void abrirFormularioNuevo() {
        mostrarDialogo(null);
    }

    private void abrirFormularioEditar(Objetivo obj) {
        mostrarDialogo(obj);
    }

    private void mostrarDialogo(Objetivo editar) {
        Equipo equipo = cmbEquipo.getValue();
        if (equipo == null) { mostrarError("Selecciona un equipo primero"); return; }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(editar == null ? "Nuevo Objetivo" : "Editar Objetivo");

        // Tipo de objetivo
        ToggleGroup tgTipo = new ToggleGroup();
        RadioButton rbEquipo   = new RadioButton("De equipo");
        RadioButton rbJugador  = new RadioButton("Individual");
        rbEquipo.setToggleGroup(tgTipo);
        rbJugador.setToggleGroup(tgTipo);
        rbEquipo.setSelected(editar == null || editar.getEquipoId() != null);
        rbJugador.setSelected(editar != null && editar.getJugadorId() != null);

        List<Jugador> jugadores = jugadorDAO.obtenerActivosPorEquipo(equipo.getId());
        ComboBox<Jugador> cmbJug = new ComboBox<>(FXCollections.observableArrayList(jugadores));
        cmbJug.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Jugador j) { return j == null ? "" : j.getNombreCompleto(); }
            public Jugador fromString(String s) { return null; }
        });
        cmbJug.setDisable(true);
        rbJugador.selectedProperty().addListener((o, v, n) -> cmbJug.setDisable(!n));
        rbEquipo.selectedProperty().addListener((o, v, n) -> cmbJug.setDisable(n));
        if (editar != null && editar.getJugadorId() != null)
            jugadores.stream().filter(j -> j.getId().equals(editar.getJugadorId())).findFirst().ifPresent(cmbJug::setValue);

        TextArea taDesc = new TextArea(editar != null ? editar.getDescripcion() : "");
        taDesc.setPrefRowCount(3);
        DatePicker dpInicio = new DatePicker(editar != null ? editar.getFechaInicio() : LocalDate.now());
        DatePicker dpLimite = new DatePicker(editar != null ? editar.getFechaLimite() : null);
        ComboBox<String> cmbPrio = new ComboBox<>(FXCollections.observableArrayList("alta","media","baja"));
        cmbPrio.setValue(editar != null ? editar.getPrioridad() : "media");
        ComboBox<String> cmbEst = new ComboBox<>(FXCollections.observableArrayList("pendiente","en_progreso","completado","cancelado"));
        cmbEst.setValue(editar != null ? editar.getEstado() : "pendiente");
        Spinner<Integer> spProgreso = new Spinner<>(0, 100, editar != null && editar.getProgresoPct() != null ? editar.getProgresoPct() : 0, 5);
        spProgreso.setEditable(true);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setStyle("-fx-padding: 20;");
        grid.add(new Label("Tipo:"),       0, 0);
        HBox hbTipo = new HBox(15, rbEquipo, rbJugador);
        grid.add(hbTipo, 1, 0);
        grid.add(new Label("Jugador:"),    0, 1); grid.add(cmbJug,    1, 1);
        grid.add(new Label("Descripcion:"),0, 2); grid.add(taDesc,    1, 2);
        grid.add(new Label("F. Inicio:"),  0, 3); grid.add(dpInicio,  1, 3);
        grid.add(new Label("F. Limite:"),  0, 4); grid.add(dpLimite,  1, 4);
        grid.add(new Label("Prioridad:"),  0, 5); grid.add(cmbPrio,   1, 5);
        grid.add(new Label("Estado:"),     0, 6); grid.add(cmbEst,    1, 6);
        grid.add(new Label("Progreso %:"), 0, 7); grid.add(spProgreso,1, 7);
        taDesc.setPrefWidth(280); cmbJug.setPrefWidth(280);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/scouteo.css").toExternalForm());
        dialog.setOnShowing(e -> StageUtils.setAppIcon((Stage) dialog.getDialogPane().getScene().getWindow()));

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            if (taDesc.getText().trim().isEmpty() || dpInicio.getValue() == null) {
                mostrarError("Descripcion y fecha de inicio son obligatorias"); return;
            }
            Objetivo obj = editar != null ? editar : new Objetivo();
            if (rbJugador.isSelected() && cmbJug.getValue() != null) {
                obj.setJugadorId(cmbJug.getValue().getId());
                obj.setEquipoId(null);
            } else {
                obj.setEquipoId(equipo.getId());
                obj.setJugadorId(null);
            }
            obj.setDescripcion(taDesc.getText().trim());
            obj.setFechaInicio(dpInicio.getValue());
            obj.setFechaLimite(dpLimite.getValue());
            obj.setPrioridad(cmbPrio.getValue());
            obj.setEstado(cmbEst.getValue());
            obj.setProgresoPct(spProgreso.getValue());

            boolean ok = editar == null ? objetivoDAO.insertar(obj) > 0 : objetivoDAO.actualizar(obj);
            if (ok) cargarObjetivos(equipo.getId());
            else mostrarError("Error al guardar");
        }
    }

    private void eliminar(Objetivo obj) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Eliminar"); a.setContentText("Eliminar este objetivo?");
        a.setOnShowing(e -> StageUtils.setAppIcon((Stage) a.getDialogPane().getScene().getWindow()));
        if (a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            objetivoDAO.eliminar(obj.getId());
            Equipo equipo = cmbEquipo.getValue();
            if (equipo != null) cargarObjetivos(equipo.getId());
        }
    }

    private void mostrarError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error"); a.setContentText(msg);
        a.setOnShowing(e -> StageUtils.setAppIcon((Stage) a.getDialogPane().getScene().getWindow()));
        a.showAndWait();
    }
}

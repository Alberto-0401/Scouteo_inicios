package com.javafx.scouteo.controller;

import com.javafx.scouteo.dao.EntrenamientoDAO;
import com.javafx.scouteo.dao.EquipoDAO;
import com.javafx.scouteo.model.AsistenciaEntrenamiento;
import com.javafx.scouteo.model.Entrenamiento;
import com.javafx.scouteo.model.Equipo;
import com.javafx.scouteo.util.SesionUsuario;
import com.javafx.scouteo.utils.StageUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class EntrenamientosController {

    @FXML private ComboBox<Equipo>           cmbEquipo;
    @FXML private TableView<Entrenamiento>   tablaEntrenamientos;
    @FXML private TableColumn<Entrenamiento, LocalDateTime> colFecha;
    @FXML private TableColumn<Entrenamiento, String>        colTipo;
    @FXML private TableColumn<Entrenamiento, String>        colIntensidad;
    @FXML private TableColumn<Entrenamiento, Integer>       colDuracion;
    @FXML private TableColumn<Entrenamiento, String>        colObjetivos;
    @FXML private TableColumn<Entrenamiento, Void>          colAcciones;
    @FXML private Label lblTotal;
    @FXML private Label lblTotalSesiones;
    @FXML private Label lblEsteMes;
    @FXML private Label lblMinutos;

    private final EntrenamientoDAO entrenamientoDAO = new EntrenamientoDAO();
    private final EquipoDAO        equipoDAO        = new EquipoDAO();
    private ObservableList<Entrenamiento> lista;

    @FXML
    public void initialize() {
        cargarEquipos();
        configurarTabla();
    }

    private void cargarEquipos() {
        List<Equipo> equipos;
        SesionUsuario sesion = SesionUsuario.getInstance();
        if (sesion.esDirectiva()) equipos = equipoDAO.obtenerPorClub(sesion.getUsuarioActual().getClubId());
        else equipos = equipoDAO.obtenerPorEntrenador(sesion.getUsuarioActual().getId());

        cmbEquipo.setItems(FXCollections.observableArrayList(equipos));
        if (!equipos.isEmpty()) {
            cmbEquipo.setValue(equipos.get(0));
            cargarEntrenamientos(equipos.get(0).getId());
        }
        cmbEquipo.setOnAction(e -> { Equipo sel = cmbEquipo.getValue(); if (sel != null) cargarEntrenamientos(sel.getId()); });
    }

    private void configurarTabla() {
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaHora"));
        colFecha.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(LocalDateTime val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : val.toLocalDate() + " " + val.toLocalTime().toString().substring(0, 5));
            }
        });
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipoDisplay"));
        colIntensidad.setCellValueFactory(new PropertyValueFactory<>("intensidadDisplay"));
        colDuracion.setCellValueFactory(new PropertyValueFactory<>("duracionMin"));
        colObjetivos.setCellValueFactory(new PropertyValueFactory<>("objetivos"));

        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnAsistencia = new Button("\uD83D\uDCCB");
            private final Button btnEditar     = new Button("\u270E");
            private final Button btnEliminar   = new Button("\u2716");
            private final HBox   contenedor    = new HBox(4, btnAsistencia, btnEditar, btnEliminar);

            {
                Tooltip.install(btnAsistencia, new Tooltip("Ver y registrar asistencia"));
                btnAsistencia.setOnAction(e -> abrirAsistencia(getTableView().getItems().get(getIndex())));
                btnEditar.setOnAction(e -> abrirFormularioEditar(getTableView().getItems().get(getIndex())));
                btnEliminar.setOnAction(e -> eliminar(getTableView().getItems().get(getIndex())));
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : contenedor);
            }
        });
    }

    private void cargarEntrenamientos(int equipoId) {
        lista = FXCollections.observableArrayList(entrenamientoDAO.obtenerPorEquipo(equipoId));
        tablaEntrenamientos.setItems(lista);
        actualizarResumen();
    }

    private void actualizarResumen() {
        lblTotal.setText("Total: " + lista.size() + " sesiones");
        lblTotalSesiones.setText(String.valueOf(lista.size()));

        LocalDate primerDelMes = LocalDate.now().withDayOfMonth(1);
        long esteMes = lista.stream()
            .filter(e -> e.getFechaHora() != null && !e.getFechaHora().toLocalDate().isBefore(primerDelMes))
            .count();
        lblEsteMes.setText(String.valueOf(esteMes));

        int minutos = lista.stream()
            .filter(e -> e.getDuracionMin() != null)
            .mapToInt(Entrenamiento::getDuracionMin).sum();
        lblMinutos.setText(String.valueOf(minutos));
    }

    @FXML
    private void abrirFormularioNuevo() {
        mostrarDialogo(null);
    }

    private void abrirFormularioEditar(Entrenamiento ent) {
        mostrarDialogo(ent);
    }

    private void abrirAsistencia(Entrenamiento ent) {
        Equipo equipo = cmbEquipo.getValue();
        if (equipo == null) { mostrarError("Selecciona un equipo primero"); return; }

        List<AsistenciaEntrenamiento> asistencia = entrenamientoDAO.obtenerAsistenciaPorEntrenamiento(ent.getId(), equipo.getId());

        // Tabla de asistencia
        TableView<AsistenciaEntrenamiento> tabla = new TableView<>();
        tabla.setPrefHeight(340);

        TableColumn<AsistenciaEntrenamiento, Integer> colDorsal = new TableColumn<>("Dor.");
        colDorsal.setPrefWidth(48);
        colDorsal.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getDorsal()).asObject());

        TableColumn<AsistenciaEntrenamiento, String> colNombre = new TableColumn<>("Jugador");
        colNombre.setPrefWidth(180);
        colNombre.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getJugadorNombre()));

        TableColumn<AsistenciaEntrenamiento, String> colEstado = new TableColumn<>("Estado");
        colEstado.setPrefWidth(160);
        colEstado.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEstadoDisplay()));
        colEstado.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> cmb = new ComboBox<>(FXCollections.observableArrayList(
                "asistio", "falta_justificada", "falta_injustificada"));
            {
                cmb.setOnAction(e -> {
                    AsistenciaEntrenamiento row = getTableView().getItems().get(getIndex());
                    row.setEstado(cmb.getValue());
                });
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    String est = getTableRow().getItem().getEstado();
                    cmb.setValue(est != null ? est : "asistio");
                    setGraphic(cmb);
                }
            }
        });

        TableColumn<AsistenciaEntrenamiento, String> colMotivo = new TableColumn<>("Motivo");
        colMotivo.setPrefWidth(150);
        colMotivo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getMotivo()));
        colMotivo.setCellFactory(col -> new TableCell<>() {
            private final TextField txt = new TextField();
            {
                txt.setOnAction(e -> {
                    AsistenciaEntrenamiento row = getTableView().getItems().get(getIndex());
                    row.setMotivo(txt.getText().trim().isEmpty() ? null : txt.getText().trim());
                });
                txt.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                    if (!isFocused && getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                        AsistenciaEntrenamiento row = getTableView().getItems().get(getIndex());
                        row.setMotivo(txt.getText().trim().isEmpty() ? null : txt.getText().trim());
                    }
                });
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    txt.setText(item != null ? item : "");
                    setGraphic(txt);
                }
            }
        });

        tabla.getColumns().addAll(colDorsal, colNombre, colEstado, colMotivo);
        tabla.setItems(FXCollections.observableArrayList(asistencia));

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Asistencia - " + ent.getFechaHora().toLocalDate());
        dialog.setHeaderText("Registra la asistencia de cada jugador");
        dialog.getDialogPane().setContent(tabla);
        dialog.getDialogPane().setPrefWidth(580);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/scouteo.css").toExternalForm());
        dialog.setOnShowing(e -> StageUtils.setAppIcon((Stage) dialog.getDialogPane().getScene().getWindow()));

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            for (AsistenciaEntrenamiento a : tabla.getItems()) {
                if (a.getEstado() != null) {
                    entrenamientoDAO.registrarAsistencia(ent.getId(), a.getJugadorId(), a.getEstado(), a.getMotivo());
                }
            }
        }
    }

    private void mostrarDialogo(Entrenamiento editar) {
        Equipo equipo = cmbEquipo.getValue();
        if (equipo == null) { mostrarError("Selecciona un equipo primero"); return; }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(editar == null ? "Nuevo Entrenamiento" : "Editar Entrenamiento");

        DatePicker dpFecha = new DatePicker(editar != null ? editar.getFechaHora().toLocalDate() : LocalDate.now());
        TextField txtHora  = new TextField(editar != null ? editar.getFechaHora().toLocalTime().toString().substring(0, 5) : "09:00");
        ComboBox<String> cmbTipo = new ComboBox<>(FXCollections.observableArrayList(
            "fisico","tactico","tecnico","mixto","partido_entrenamiento"));
        cmbTipo.setValue(editar != null ? editar.getTipo() : "mixto");
        ComboBox<String> cmbIntensidad = new ComboBox<>(FXCollections.observableArrayList("baja","media","alta"));
        cmbIntensidad.setValue(editar != null ? editar.getIntensidad() : "media");
        TextField txtDuracion = new TextField(editar != null && editar.getDuracionMin() != null ? String.valueOf(editar.getDuracionMin()) : "90");
        TextArea  taObjetivos = new TextArea(editar != null && editar.getObjetivos() != null ? editar.getObjetivos() : "");
        taObjetivos.setPrefRowCount(3);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setStyle("-fx-padding: 20;");
        grid.add(new Label("Fecha:"),      0, 0); grid.add(dpFecha,      1, 0);
        grid.add(new Label("Hora (HH:mm):"), 0, 1); grid.add(txtHora,   1, 1);
        grid.add(new Label("Tipo:"),       0, 2); grid.add(cmbTipo,      1, 2);
        grid.add(new Label("Intensidad:"), 0, 3); grid.add(cmbIntensidad, 1, 3);
        grid.add(new Label("Duracion (min):"), 0, 4); grid.add(txtDuracion, 1, 4);
        grid.add(new Label("Objetivos:"),  0, 5); grid.add(taObjetivos,  1, 5);
        dpFecha.setPrefWidth(200); cmbTipo.setPrefWidth(200);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/scouteo.css").toExternalForm());
        dialog.setOnShowing(e -> StageUtils.setAppIcon((Stage) dialog.getDialogPane().getScene().getWindow()));

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                LocalDate fecha = dpFecha.getValue();
                LocalTime hora  = LocalTime.parse(txtHora.getText().trim());
                if (fecha == null) { mostrarError("La fecha es obligatoria"); return; }

                Entrenamiento ent = editar != null ? editar : new Entrenamiento();
                ent.setEquipoId(equipo.getId());
                ent.setFechaHora(LocalDateTime.of(fecha, hora));
                ent.setTipo(cmbTipo.getValue());
                ent.setIntensidad(cmbIntensidad.getValue());
                ent.setDuracionMin(txtDuracion.getText().trim().isEmpty() ? null : Integer.parseInt(txtDuracion.getText().trim()));
                ent.setObjetivos(taObjetivos.getText().trim().isEmpty() ? null : taObjetivos.getText().trim());

                boolean ok = editar == null ? entrenamientoDAO.insertar(ent) > 0 : entrenamientoDAO.actualizar(ent);
                if (ok) cargarEntrenamientos(equipo.getId());
                else mostrarError("Error al guardar");
            } catch (Exception ex) {
                mostrarError("Formato invalido (hora debe ser HH:mm)");
            }
        }
    }

    private void eliminar(Entrenamiento ent) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Eliminar"); a.setContentText("Eliminar este entrenamiento?");
        a.setOnShowing(e -> StageUtils.setAppIcon((Stage) a.getDialogPane().getScene().getWindow()));
        if (a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            entrenamientoDAO.eliminar(ent.getId());
            Equipo equipo = cmbEquipo.getValue();
            if (equipo != null) cargarEntrenamientos(equipo.getId());
        }
    }

    private void mostrarError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error"); a.setContentText(msg);
        a.setOnShowing(e -> StageUtils.setAppIcon((Stage) a.getDialogPane().getScene().getWindow()));
        a.showAndWait();
    }
}

package com.javafx.scouteo.controller;

import com.javafx.scouteo.dao.EquipoDAO;
import com.javafx.scouteo.dao.HistorialMedicoDAO;
import com.javafx.scouteo.dao.JugadorDAO;
import com.javafx.scouteo.model.Equipo;
import com.javafx.scouteo.model.HistorialMedico;
import com.javafx.scouteo.model.Jugador;
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

public class HistorialMedicoController {

    @FXML private ComboBox<Equipo>        cmbEquipo;
    @FXML private TextField               txtBuscar;
    @FXML private ComboBox<String>        cmbEstado;
    @FXML private TableView<HistorialMedico> tablaHistorial;
    @FXML private TableColumn<HistorialMedico, String>    colJugador;
    @FXML private TableColumn<HistorialMedico, String>    colLesion;
    @FXML private TableColumn<HistorialMedico, String>    colZona;
    @FXML private TableColumn<HistorialMedico, LocalDate> colFechaLes;
    @FXML private TableColumn<HistorialMedico, LocalDate> colFechaRec;
    @FXML private TableColumn<HistorialMedico, Integer>   colDias;
    @FXML private TableColumn<HistorialMedico, String>    colEstado;
    @FXML private TableColumn<HistorialMedico, Void>      colAcciones;
    @FXML private Label lblTotal;
    @FXML private Label lblLesionesActivas;

    private final HistorialMedicoDAO historialDAO = new HistorialMedicoDAO();
    private final EquipoDAO          equipoDAO    = new EquipoDAO();
    private final JugadorDAO         jugadorDAO   = new JugadorDAO();

    private ObservableList<HistorialMedico> listaCompleta;
    private FilteredList<HistorialMedico>   listaFiltrada;

    @FXML
    public void initialize() {
        cmbEstado.setItems(FXCollections.observableArrayList("Todos", "En recuperacion", "Recuperado", "Retrasado"));
        cmbEstado.setValue("Todos");

        cargarEquipos();
        configurarTabla();
        configurarFiltros();
    }

    private void cargarEquipos() {
        List<Equipo> equipos;
        SesionUsuario sesion = SesionUsuario.getInstance();
        if (sesion.esDirectiva()) {
            equipos = equipoDAO.obtenerPorClub(sesion.getUsuarioActual().getClubId());
        } else {
            equipos = equipoDAO.obtenerPorEntrenador(sesion.getUsuarioActual().getId());
        }
        cmbEquipo.setItems(FXCollections.observableArrayList(equipos));
        if (!equipos.isEmpty()) {
            cmbEquipo.setValue(equipos.get(0));
            cargarHistorial(equipos.get(0).getId());
        }
        cmbEquipo.setOnAction(e -> {
            Equipo sel = cmbEquipo.getValue();
            if (sel != null) cargarHistorial(sel.getId());
        });
    }

    private void configurarTabla() {
        colJugador.setCellValueFactory(new PropertyValueFactory<>("nombreJugador"));
        colLesion.setCellValueFactory(new PropertyValueFactory<>("tipoLesion"));
        colZona.setCellValueFactory(new PropertyValueFactory<>("zonaAfectada"));
        colFechaLes.setCellValueFactory(new PropertyValueFactory<>("fechaLesion"));
        colFechaRec.setCellValueFactory(new PropertyValueFactory<>("fechaRecuperacionEst"));
        colDias.setCellValueFactory(new PropertyValueFactory<>("diasFuera"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoLesion"));

        // Color por estado
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); setStyle(""); return; }
                setText(val);
                setStyle(val.equals("Recuperado")
                    ? "-fx-text-fill: #2E7D32; -fx-font-weight: bold;"
                    : "-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
            }
        });

        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnAltaMedica = new Button("Alta");
            private final Button btnEditar     = new Button("\u270E");
            private final Button btnEliminar   = new Button("\u2716");
            private final HBox   contenedor    = new HBox(4, btnAltaMedica, btnEditar, btnEliminar);

            {
                btnAltaMedica.getStyleClass().setAll("btn-alta");
                btnEliminar.getStyleClass().setAll("btn-peligro");
                btnAltaMedica.setOnAction(e -> darAltaMedica(getTableView().getItems().get(getIndex())));
                btnEditar.setOnAction(e -> abrirFormularioEditar(getTableView().getItems().get(getIndex())));
                btnEliminar.setOnAction(e -> eliminar(getTableView().getItems().get(getIndex())));
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                HistorialMedico h = getTableView().getItems().get(getIndex());
                btnAltaMedica.setVisible(h.getFechaAlta() == null);
                setGraphic(contenedor);
            }
        });
    }

    private void configurarFiltros() {
        txtBuscar.textProperty().addListener((o, v, n) -> aplicarFiltros());
        cmbEstado.valueProperty().addListener((o, v, n) -> aplicarFiltros());
    }

    private void cargarHistorial(int equipoId) {
        List<HistorialMedico> lista = historialDAO.obtenerPorEquipo(equipoId);
        autoRecuperarPorFecha(lista);
        listaCompleta  = FXCollections.observableArrayList(lista);
        listaFiltrada  = new FilteredList<>(listaCompleta, p -> true);
        tablaHistorial.setItems(listaFiltrada);
        actualizarContadores(equipoId);
        aplicarFiltros();
    }

    private void autoRecuperarPorFecha(List<HistorialMedico> lista) {
        LocalDate hoy = LocalDate.now();
        for (HistorialMedico h : lista) {
            if (h.getFechaAlta() != null) continue;
            LocalDate rec = h.getFechaRecuperacionEst();
            if (rec != null && !hoy.isBefore(rec)) {
                h.setFechaAlta(hoy);
                historialDAO.actualizar(h);
                jugadorDAO.cambiarEstado(h.getJugadorId(), "activo");
            }
        }
    }

    @FXML private void limpiarFiltros() {
        txtBuscar.clear();
        cmbEstado.setValue("Todos");
    }

    private void aplicarFiltros() {
        if (listaFiltrada == null) return;
        String texto  = txtBuscar.getText() != null ? txtBuscar.getText().toLowerCase() : "";
        String estado = cmbEstado.getValue();
        listaFiltrada.setPredicate(h -> {
            boolean txt = texto.isEmpty() || (h.getNombreJugador() != null && h.getNombreJugador().toLowerCase().contains(texto))
                          || (h.getTipoLesion() != null && h.getTipoLesion().toLowerCase().contains(texto));
            boolean est = "Todos".equals(estado) || estado == null || estado.equals(h.getEstadoLesion());
            return txt && est;
        });
        lblTotal.setText("Total: " + listaFiltrada.size() + " registros");
    }

    private void actualizarContadores(int equipoId) {
        int activas = historialDAO.contarLesionesActivas(equipoId);
        lblLesionesActivas.setText("Lesiones activas: " + activas);
    }

    @FXML
    private void abrirFormularioNuevo() {
        mostrarDialogo(null);
    }

    private void abrirFormularioEditar(HistorialMedico h) {
        mostrarDialogo(h);
    }

    private void mostrarDialogo(HistorialMedico editar) {
        Equipo equipo = cmbEquipo.getValue();
        if (equipo == null) { mostrarError("Selecciona un equipo primero"); return; }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(editar == null ? "Nueva Lesion" : "Editar Lesion");

        List<Jugador> jugadores = jugadorDAO.obtenerPorEquipo(equipo.getId());
        ComboBox<Jugador> cmbJug = new ComboBox<>(FXCollections.observableArrayList(jugadores));
        cmbJug.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Jugador j) { return j == null ? "" : j.getNombreCompleto(); }
            public Jugador fromString(String s) { return null; }
        });
        if (editar != null) {
            jugadores.stream().filter(jj -> jj.getId().equals(editar.getJugadorId())).findFirst().ifPresent(cmbJug::setValue);
        }
        TextField txtTipo = new TextField(editar != null ? editar.getTipoLesion() : "");
        TextField txtZona = new TextField(editar != null && editar.getZonaAfectada() != null ? editar.getZonaAfectada() : "");
        DatePicker dpLesion = new DatePicker(editar != null ? editar.getFechaLesion() : LocalDate.now());
        DatePicker dpRecEst = new DatePicker(editar != null ? editar.getFechaRecuperacionEst() : null);
        TextArea   taObs    = new TextArea(editar != null && editar.getObservaciones() != null ? editar.getObservaciones() : "");
        taObs.setPrefRowCount(3);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setStyle("-fx-padding: 20;");
        grid.add(new Label("Jugador:"),      0, 0); grid.add(cmbJug,  1, 0);
        grid.add(new Label("Tipo lesion:"),  0, 1); grid.add(txtTipo, 1, 1);
        grid.add(new Label("Zona:"),         0, 2); grid.add(txtZona, 1, 2);
        grid.add(new Label("Fecha lesion:"), 0, 3); grid.add(dpLesion, 1, 3);
        grid.add(new Label("Alta estimada:"),0, 4); grid.add(dpRecEst, 1, 4);
        grid.add(new Label("Observaciones:"),0, 5); grid.add(taObs,   1, 5);
        cmbJug.setPrefWidth(250); txtTipo.setPrefWidth(250);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/scouteo.css").toExternalForm());
        dialog.setOnShowing(e -> StageUtils.setAppIcon((Stage) dialog.getDialogPane().getScene().getWindow()));

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            Jugador jSel = cmbJug.getValue();
            if (jSel == null || txtTipo.getText().trim().isEmpty() || dpLesion.getValue() == null) {
                mostrarError("Jugador, tipo y fecha de lesion son obligatorios"); return;
            }
            HistorialMedico h = editar != null ? editar : new HistorialMedico();
            h.setJugadorId(jSel.getId());
            h.setTipoLesion(txtTipo.getText().trim());
            h.setZonaAfectada(txtZona.getText().trim().isEmpty() ? null : txtZona.getText().trim());
            h.setFechaLesion(dpLesion.getValue());
            h.setFechaRecuperacionEst(dpRecEst.getValue());
            h.setObservaciones(taObs.getText().trim().isEmpty() ? null : taObs.getText().trim());

            boolean ok = editar == null ? historialDAO.insertar(h) > 0 : historialDAO.actualizar(h);
            if (ok) {
                if (editar == null) {
                    jugadorDAO.cambiarEstado(jSel.getId(), "lesionado");
                }
                cargarHistorial(equipo.getId());
            } else mostrarError("Error al guardar");
        }
    }

    private void darAltaMedica(HistorialMedico h) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Alta medica");
        a.setContentText("Dar de alta medica a " + h.getNombreJugador() + "?");
        a.setOnShowing(e -> StageUtils.setAppIcon((Stage) a.getDialogPane().getScene().getWindow()));
        if (a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            h.setFechaAlta(LocalDate.now());
            historialDAO.actualizar(h);
            jugadorDAO.cambiarEstado(h.getJugadorId(), "activo");
            Equipo equipo = cmbEquipo.getValue();
            if (equipo != null) cargarHistorial(equipo.getId());
        }
    }

    private void eliminar(HistorialMedico h) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Eliminar"); a.setContentText("Eliminar este registro medico?");
        a.setOnShowing(e -> StageUtils.setAppIcon((Stage) a.getDialogPane().getScene().getWindow()));
        if (a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            historialDAO.eliminar(h.getId());
            Equipo equipo = cmbEquipo.getValue();
            if (equipo != null) cargarHistorial(equipo.getId());
        }
    }

    private void mostrarError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error"); a.setContentText(msg);
        a.setOnShowing(e -> StageUtils.setAppIcon((Stage) a.getDialogPane().getScene().getWindow()));
        a.showAndWait();
    }
}

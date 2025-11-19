package com.javafx.scouteo.controller;

import com.javafx.scouteo.dao.PartidoDAO;
import com.javafx.scouteo.model.Partido;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class FormPartidoController {

    @FXML
    private DatePicker dpFechaPartido;

    @FXML
    private TextField txtRival;

    @FXML
    private TextField txtResultado;

    @FXML
    private ComboBox<String> cmbLocalVisitante;

    @FXML
    private TextArea txtObservaciones;

    @FXML
    private Label lblError;

    private PartidoDAO partidoDAO;
    private PartidosController partidosController;

    @FXML
    public void initialize() {
        partidoDAO = new PartidoDAO();
        cmbLocalVisitante.setValue("LOCAL");
    }

    public void setPartidosController(PartidosController controller) {
        this.partidosController = controller;
    }

    @FXML
    private void guardar() {
        if (!validarCampos()) {
            return;
        }

        Partido partido = new Partido();
        partido.setFechaPartido(dpFechaPartido.getValue());
        partido.setRival(txtRival.getText().trim());
        partido.setResultado(txtResultado.getText().trim());
        partido.setLocalVisitante(cmbLocalVisitante.getValue());
        partido.setObservaciones(txtObservaciones.getText().trim());

        if (partidoDAO.insertar(partido)) {
            if (partidosController != null) {
                partidosController.cargarPartidos();
            }
            cerrarVentana();
        } else {
            lblError.setText("Error al guardar el partido");
        }
    }

    @FXML
    private void cancelar() {
        cerrarVentana();
    }

    private boolean validarCampos() {
        if (dpFechaPartido.getValue() == null) {
            lblError.setText("La fecha es obligatoria");
            dpFechaPartido.requestFocus();
            return false;
        }

        if (txtRival.getText().trim().isEmpty()) {
            lblError.setText("El rival es obligatorio");
            txtRival.requestFocus();
            return false;
        }

        if (cmbLocalVisitante.getValue() == null || cmbLocalVisitante.getValue().isEmpty()) {
            lblError.setText("Debe seleccionar Local o Visitante");
            cmbLocalVisitante.requestFocus();
            return false;
        }

        lblError.setText("");
        return true;
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtRival.getScene().getWindow();
        stage.close();
    }
}

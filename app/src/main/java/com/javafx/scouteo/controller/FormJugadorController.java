package com.javafx.scouteo.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class FormJugadorController {

    @FXML
    private ComboBox<?> cmbCategoria;

    @FXML
    private ComboBox<?> cmbPosicion;

    @FXML
    private DatePicker dpFechaNacimiento;

    @FXML
    private Label lblError;

    @FXML
    private Label lblNombreFoto;

    @FXML
    private Label lblTitulo;

    @FXML
    private TextField txtAltura;

    @FXML
    private TextField txtApellidos;

    @FXML
    private TextField txtDorsal;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtPeso;

    @FXML
    void cancelar(ActionEvent event) {
        javafx.stage.Stage stage = (javafx.stage.Stage) txtNombre.getScene().getWindow();
        stage.close();
    }

    @FXML
    void guardar(ActionEvent event) {
        lblError.setText("Guardar (no implementado)");
    }

    @FXML
    void seleccionarFoto(ActionEvent event) {
        lblNombreFoto.setText("Seleccionar foto (no implementado)");
    }

}
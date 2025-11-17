package com.javafx.scouteo.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class FormEstadisticaController {

    @FXML
    private ComboBox<?> cmbJugador;

    @FXML
    private DatePicker dpFecha;

    @FXML
    private HBox hboxParadas;

    @FXML
    private HBox hboxRecuperaciones;

    @FXML
    private Label lblError;

    @FXML
    private Label lblPosicionJugador;

    @FXML
    private Label lblValoracion;

    @FXML
    private TextField txtAmarillas;

    @FXML
    private TextField txtAsistencias;

    @FXML
    private TextField txtGoles;

    @FXML
    private TextField txtMinutos;

    @FXML
    private TextField txtParadas;

    @FXML
    private TextField txtRecuperaciones;

    @FXML
    private TextField txtResultado;

    @FXML
    private TextField txtRival;

    @FXML
    private TextField txtRojas;

    @FXML
    private TextField txtValoracion;

    @FXML
    private VBox vboxEspecificas;

    @FXML
    void cancelar(ActionEvent event) {

    }

    @FXML
    void guardar(ActionEvent event) {

    }

}


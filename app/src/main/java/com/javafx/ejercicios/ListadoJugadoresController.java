package com.javafx.ejercicios;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class ListadoJugadoresController {

    @FXML
    private ComboBox<?> cmbPosicion;

    @FXML
    private TableColumn<?, ?> colAcciones;

    @FXML
    private TableColumn<?, ?> colApellidos;

    @FXML
    private TableColumn<?, ?> colCategoria;

    @FXML
    private TableColumn<?, ?> colDorsal;

    @FXML
    private TableColumn<?, ?> colEdad;

    @FXML
    private TableColumn<?, ?> colId;

    @FXML
    private TableColumn<?, ?> colNombre;

    @FXML
    private TableColumn<?, ?> colPosicion;

    @FXML
    private Label lblInfo;

    @FXML
    private Label lblTotal;

    @FXML
    private TableView<?> tablaJugadores;

    @FXML
    private TextField txtBuscar;

    @FXML
    void abrirFormularioNuevo(ActionEvent event) {

    }

    @FXML
    void volverDashboard(ActionEvent event) {

    }

}

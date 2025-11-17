package com.javafx.scouteo.controller;

import com.javafx.scouteo.model.Configuracion;
import com.javafx.scouteo.dao.ConfiguracionDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class ConfiguracionController {

    @FXML
    private ImageView imgEscudo;

    @FXML
    private Label lblMensaje;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtLocalidad;

    @FXML
    private TextField txtNombreClub;

    @FXML
    private TextField txtPresidente;

    @FXML
    private TextField txtTelefono;

    @FXML
    private TextField txtTemporada;

    private ConfiguracionDAO configuracionDAO;
    private Configuracion configuracion;

    @FXML
    public void initialize() {
        configuracionDAO = new ConfiguracionDAO();
        cargarDatos();
    }

    private void cargarDatos() {
        configuracion = configuracionDAO.obtener();

        if (configuracion != null) {
            txtNombreClub.setText(configuracion.getNombreClub());
            txtLocalidad.setText(configuracion.getLocalidad());
            txtPresidente.setText(configuracion.getPresidente());
            txtEmail.setText(configuracion.getEmail());
            txtTelefono.setText(configuracion.getTelefono());
            txtTemporada.setText(configuracion.getTemporadaActual());
        }
    }

    @FXML
    void cambiarEscudo(ActionEvent event) {
        // TODO: Implementar selector de archivo para el escudo
    }

    @FXML
    void guardarCambios(ActionEvent event) {
        lblMensaje.setText("Guardar cambios (no implementado)");
        lblMensaje.setStyle("-fx-text-fill: orange;");
    }

    @FXML
    void volverDashboard(ActionEvent event) {
        Stage stage = (Stage) txtNombreClub.getScene().getWindow();
        stage.close();
    }

}

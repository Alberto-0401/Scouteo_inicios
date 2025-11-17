package com.javafx.scouteo.controller;

import com.javafx.scouteo.model.Configuracion;
import com.javafx.scouteo.dao.ConfiguracionDAO;
import com.javafx.scouteo.dao.JugadorDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class InfoClubController {

    @FXML
    private Label lblNombreClub;

    @FXML
    private Label lblFundacion;

    @FXML
    private Label lblCiudad;

    @FXML
    private Label lblEstadio;

    @FXML
    private Label lblEntrenador;

    @FXML
    private Label lblCategoria;

    @FXML
    private Label lblTotalPlantilla;

    @FXML
    private Label lblVictorias;

    @FXML
    private Label lblEmpates;

    @FXML
    private Label lblDerrotas;

    private ConfiguracionDAO configuracionDAO;
    private JugadorDAO jugadorDAO;

    @FXML
    public void initialize() {
        configuracionDAO = new ConfiguracionDAO();
        jugadorDAO = new JugadorDAO();
        cargarInformacionClub();
    }

    private void cargarInformacionClub() {
        Configuracion config = configuracionDAO.obtener();

        if (config != null) {
            lblNombreClub.setText(config.getNombreClub());
            lblCiudad.setText(config.getLocalidad());
            lblEntrenador.setText(config.getPresidente());
            lblCategoria.setText(config.getTemporadaActual());
        }

        // Datos fijos o calculables
        lblFundacion.setText("01/01/1900");
        lblEstadio.setText("Estadio Municipal");

        // Estadísticas
        int totalPlantilla = jugadorDAO.contarTotal();
        lblTotalPlantilla.setText(String.valueOf(totalPlantilla));

        // TODO: Cargar victorias, empates y derrotas desde base de datos
        lblVictorias.setText("0");
        lblEmpates.setText("0");
        lblDerrotas.setText("0");
    }

    @FXML
    private void editarInfoClub() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/ConfiguracionClub.fxml"));
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setScene(new javafx.scene.Scene(loader.load()));
            stage.setTitle("Configuración del Club");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Recargar datos después de cerrar la ventana
            cargarInformacionClub();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}

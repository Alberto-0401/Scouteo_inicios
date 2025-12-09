package com.javafx.scouteo.controller;

import com.javafx.scouteo.model.Configuracion;
import com.javafx.scouteo.dao.ConfiguracionDAO;
import com.javafx.scouteo.dao.JugadorDAO;
import com.javafx.scouteo.utils.StageUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.ByteArrayInputStream;

public class InfoClubController {

    @FXML
    private ImageView imgEscudoClub;

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

            // Cargar escudo del club
            if (config.getEscudo() != null && config.getEscudo().length > 0) {
                try {
                    ByteArrayInputStream bis = new ByteArrayInputStream(config.getEscudo());
                    Image image = new Image(bis);
                    imgEscudoClub.setImage(image);
                    imgEscudoClub.setVisible(true);
                    imgEscudoClub.setManaged(true);
                } catch (Exception e) {
                    System.err.println("Error al cargar el escudo del club: " + e.getMessage());
                    imgEscudoClub.setVisible(false);
                    imgEscudoClub.setManaged(false);
                }
            } else {
                imgEscudoClub.setVisible(false);
                imgEscudoClub.setManaged(false);
            }
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
            StageUtils.setAppIcon(stage);
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

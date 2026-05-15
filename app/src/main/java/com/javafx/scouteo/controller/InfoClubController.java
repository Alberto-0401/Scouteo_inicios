package com.javafx.scouteo.controller;

import com.javafx.scouteo.model.Configuracion;
import com.javafx.scouteo.model.Equipo;
import com.javafx.scouteo.dao.ConfiguracionDAO;
import com.javafx.scouteo.dao.JugadorDAO;
import com.javafx.scouteo.dao.PartidoDAO;
import com.javafx.scouteo.util.SesionUsuario;
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
    private PartidoDAO partidoDAO;
    private Equipo equipoActivo;

    public void setEquipoActivo(Equipo equipo) {
        this.equipoActivo = equipo;
        cargarInformacionClub();
    }

    @FXML
    public void initialize() {
        configuracionDAO = new ConfiguracionDAO();
        jugadorDAO = new JugadorDAO();
        partidoDAO = new PartidoDAO();
        cargarInformacionClub();
    }

    private void cargarInformacionClub() {
        int clubId = SesionUsuario.getInstance().getUsuarioActual().getClubId();
        Configuracion config = configuracionDAO.obtener(clubId);

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

        // Estadísticas — filtradas por equipo si hay uno activo, si no por club
        int totalPlantilla;
        int[] resultados;
        if (equipoActivo != null) {
            totalPlantilla = jugadorDAO.contarPorEquipo(equipoActivo.getId());
            resultados = partidoDAO.contarResultadosPorEquipo(equipoActivo.getId());
        } else {
            totalPlantilla = jugadorDAO.contarPorClub(clubId);
            resultados = partidoDAO.contarResultadosPorClub(clubId);
        }
        lblTotalPlantilla.setText(String.valueOf(totalPlantilla));
        lblVictorias.setText(String.valueOf(resultados[0]));
        lblEmpates.setText(String.valueOf(resultados[1]));
        lblDerrotas.setText(String.valueOf(resultados[2]));
    }

    @FXML
    private void editarInfoClub() {
        abrirModal("/views/ConfiguracionClub.fxml", "Configuracion del Club");
        cargarInformacionClub();
    }

    @FXML
    private void editarPerfil() {
        abrirModal("/views/EditarPerfil.fxml", "Mi Perfil");
    }

    private void abrirModal(String fxml, String titulo) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxml));
            javafx.stage.Stage stage = new javafx.stage.Stage();
            StageUtils.setAppIcon(stage);
            stage.setScene(new javafx.scene.Scene(loader.load()));
            stage.setTitle(titulo);
            stage.initOwner(lblNombreClub.getScene().getWindow());
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}

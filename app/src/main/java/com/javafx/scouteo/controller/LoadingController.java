package com.javafx.scouteo.controller;

import com.javafx.scouteo.utils.StageUtils;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class LoadingController {

    @FXML private Label lblEstado;
    @FXML private Rectangle barraProgreso;

    private static final double BARRA_ANCHO = 280.0;

    @FXML
    public void initialize() {
        barraProgreso.setWidth(0);
    }

    public void iniciarCarga(Stage stage, String nombreUsuario) {
        Timeline progreso = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(barraProgreso.widthProperty(), 0)),
            new KeyFrame(Duration.millis(400),
                e -> actualizarEstado("Cargando datos del club..."),
                new KeyValue(barraProgreso.widthProperty(), BARRA_ANCHO * 0.3)),
            new KeyFrame(Duration.millis(900),
                e -> actualizarEstado("Preparando equipos y jugadores..."),
                new KeyValue(barraProgreso.widthProperty(), BARRA_ANCHO * 0.65)),
            new KeyFrame(Duration.millis(1400),
                e -> actualizarEstado("Listo"),
                new KeyValue(barraProgreso.widthProperty(), BARRA_ANCHO))
        );

        progreso.setOnFinished(e -> abrirDashboard(stage, nombreUsuario));
        progreso.play();
    }

    private void actualizarEstado(String texto) {
        Platform.runLater(() -> lblEstado.setText(texto));
    }

    private void abrirDashboard(Stage stage, String nombreUsuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Dashboard.fxml"));
            Parent root = loader.load();

            String css = getClass().getClassLoader().getResource("scouteo.css").toExternalForm();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(css);

            StageUtils.setAppIcon(stage);
            stage.setScene(scene);
            stage.setTitle("SCOUTEO - " + nombreUsuario);
            stage.setWidth(1280);
            stage.setHeight(780);
            stage.setResizable(false);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

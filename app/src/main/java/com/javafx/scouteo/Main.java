package com.javafx.scouteo;

import com.javafx.scouteo.utils.StageUtils;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primeraEscena) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/views/Login.fxml"));

        Scene scene = new Scene(root);

        try {
            String css = getClass().getClassLoader().getResource("scouteo.css").toExternalForm();
            StageUtils.setAppIcon(primeraEscena);
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.err.println("Error al cargar CSS: " + e.getMessage());
        }

        primeraEscena.setScene(scene);
        primeraEscena.setTitle("SCOUTEO - Iniciar Sesion");
        primeraEscena.setWidth(900);
        primeraEscena.setHeight(600);
        primeraEscena.setResizable(false);
        primeraEscena.centerOnScreen();
        primeraEscena.show();
    }
}


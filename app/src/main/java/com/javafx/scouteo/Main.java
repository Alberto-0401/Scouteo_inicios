package com.javafx.scouteo;

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
        Parent root = FXMLLoader.load(getClass().getResource("/views/Dashboard.fxml"));

        Scene scene = new Scene(root);

        // Cargar CSS globalmente usando el ClassLoader
        try {
            String css = getClass().getClassLoader().getResource("scouteo.css").toExternalForm();
            scene.getStylesheets().add(css);
            System.out.println("CSS cargado correctamente: " + css);
        } catch (Exception e) {
            System.err.println("Error al cargar CSS: " + e.getMessage());
            e.printStackTrace();
        }

        primeraEscena.setScene(scene);
        primeraEscena.setTitle("SCOUTEO - Sistema de Gestión de Jugadores");
        primeraEscena.setWidth(1200);
        primeraEscena.setHeight(750);
        primeraEscena.setResizable(false);
        primeraEscena.show();
    }
}

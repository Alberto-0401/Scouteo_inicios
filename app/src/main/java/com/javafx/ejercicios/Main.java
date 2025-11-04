package com.javafx.ejercicios;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Juan
 */
public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primeraEscena) throws Exception { //puede lanzar excep'
        Parent root = FXMLLoader.load(getClass().getResource("/ejercicio1.fxml"));

        Scene scene = new Scene(root);
        primeraEscena.setScene(scene);
        primeraEscena.setTitle("Ejercicio 1");
        primeraEscena.show();
        
        
    }
}

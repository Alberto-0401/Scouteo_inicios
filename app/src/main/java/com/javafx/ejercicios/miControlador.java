package com.javafx.ejercicios;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class miControlador implements Initializable {

     @FXML
    private TextField txtDescuento;

    @FXML
    private TextField txtPrecio;


    @FXML
    void btnCalcular(MouseEvent event) {
        
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setHeaderText("Descuento aplicado. Precio inicial "+txtPrecio.getText());
        alert.showAndWait();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
       
    }

}

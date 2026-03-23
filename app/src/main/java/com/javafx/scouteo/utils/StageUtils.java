package com.javafx.scouteo.utils;

import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Utilidad para configurar propiedades comunes de las ventanas (Stage)
 */
public class StageUtils {

    private static Image appIcon = null;

    /**
     * Establece el icono de la aplicación para todas las ventanas
     */
    public static void setAppIcon(Stage stage) {
        if (appIcon == null) {
            try {
                appIcon = new Image(StageUtils.class.getResourceAsStream("/images/logo1.png"));
            } catch (Exception e) {
                System.out.println("No se pudo cargar el icono de la aplicación: " + e.getMessage());
                return;
            }
        }

        if (appIcon != null) {
            stage.getIcons().clear();
            stage.getIcons().add(appIcon);
        }
    }
}

package com.javafx.scouteo.utils;

import javafx.scene.control.Tooltip;
import javafx.util.Duration;

/**
 * Utilidad para configurar tooltips con duración personalizada
 */
public class TooltipUtils {

    /**
     * Crea un tooltip que permanece visible mientras el cursor esté encima
     * @param text Texto del tooltip
     * @return Tooltip configurado
     */
    public static Tooltip crearTooltip(String text) {
        Tooltip tooltip = new Tooltip(text);

        // Configurar tiempos de visualización
        tooltip.setShowDelay(Duration.millis(500));     // Demora antes de aparecer: 0.5s
        tooltip.setShowDuration(Duration.INDEFINITE);   // Duración: infinita mientras cursor encima
        tooltip.setHideDelay(Duration.millis(200));     // Demora antes de ocultarse: 0.2s

        return tooltip;
    }

    /**
     * Instala un tooltip en un nodo con duración infinita
     * @param node Nodo donde instalar el tooltip
     * @param text Texto del tooltip
     */
    public static void instalarTooltip(javafx.scene.Node node, String text) {
        Tooltip.install(node, crearTooltip(text));
    }
}

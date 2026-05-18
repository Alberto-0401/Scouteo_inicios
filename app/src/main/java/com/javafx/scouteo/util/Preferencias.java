package com.javafx.scouteo.util;

import java.io.File;
import java.util.prefs.Preferences;

public class Preferencias {

    private static final Preferences prefs = Preferences.userNodeForPackage(Preferencias.class);

    private static final String KEY_PDF_DIR          = "pdfDirectory";
    private static final String KEY_PIE_PDF          = "piePaginaPdf";
    private static final String KEY_FIRMANTE_PDF     = "firmantePdf";
    private static final String KEY_ABRIR_PDF_AUTO   = "abrirPdfAuto";
    private static final String KEY_VISTA_INICIO     = "vistaInicio";
    private static final String KEY_CONFIRMAR_ELIM   = "confirmarEliminacion";
    private static final String KEY_OCULTAR_BAJAS    = "ocultarJugadoresBaja";

    // ---- Carpeta PDFs ----
    public static String getPdfDirectory() {
        String defaultDir = System.getProperty("user.home")
                + File.separator + "Documents"
                + File.separator + "Scouteo";
        return prefs.get(KEY_PDF_DIR, defaultDir);
    }

    public static void setPdfDirectory(String dir) {
        prefs.put(KEY_PDF_DIR, dir);
    }

    // ---- Pie de página en informes ----
    public static String getPiePaginaPdf() {
        return prefs.get(KEY_PIE_PDF, "");
    }

    public static void setPiePaginaPdf(String texto) {
        prefs.put(KEY_PIE_PDF, texto);
    }

    // ---- Firmante en informes ----
    public static String getFirmantePdf() {
        return prefs.get(KEY_FIRMANTE_PDF, "");
    }

    public static void setFirmantePdf(String nombre) {
        prefs.put(KEY_FIRMANTE_PDF, nombre);
    }

    // ---- Abrir PDF automáticamente al generarlo ----
    public static boolean getAbrirPdfAuto() {
        return prefs.getBoolean(KEY_ABRIR_PDF_AUTO, false);
    }

    public static void setAbrirPdfAuto(boolean valor) {
        prefs.putBoolean(KEY_ABRIR_PDF_AUTO, valor);
    }

    // ---- Pantalla de inicio ("inicio", "jugadores", "partidos", "entrenamientos") ----
    public static String getVistaInicio() {
        return prefs.get(KEY_VISTA_INICIO, "inicio");
    }

    public static void setVistaInicio(String vista) {
        prefs.put(KEY_VISTA_INICIO, vista);
    }

    // ---- Confirmar antes de eliminar ----
    public static boolean getConfirmarEliminacion() {
        return prefs.getBoolean(KEY_CONFIRMAR_ELIM, true);
    }

    public static void setConfirmarEliminacion(boolean valor) {
        prefs.putBoolean(KEY_CONFIRMAR_ELIM, valor);
    }

    // ---- Ocultar jugadores de baja en la tabla ----
    public static boolean getOcultarJugadoresBaja() {
        return prefs.getBoolean(KEY_OCULTAR_BAJAS, false);
    }

    public static void setOcultarJugadoresBaja(boolean valor) {
        prefs.putBoolean(KEY_OCULTAR_BAJAS, valor);
    }
}

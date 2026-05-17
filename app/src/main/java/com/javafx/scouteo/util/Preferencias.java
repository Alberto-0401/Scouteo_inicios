package com.javafx.scouteo.util;

import java.io.File;
import java.util.prefs.Preferences;

public class Preferencias {

    private static final Preferences prefs = Preferences.userNodeForPackage(Preferencias.class);

    private static final String KEY_TEMA    = "tema";
    private static final String KEY_PDF_DIR = "pdfDirectory";

    public static String getTema() {
        return prefs.get(KEY_TEMA, "claro");
    }

    public static void setTema(String tema) {
        prefs.put(KEY_TEMA, tema);
    }

    public static boolean esModoOscuro() {
        return "oscuro".equals(getTema());
    }

    public static String getPdfDirectory() {
        String defaultDir = System.getProperty("user.home")
                + File.separator + "Documents"
                + File.separator + "Scouteo";
        return prefs.get(KEY_PDF_DIR, defaultDir);
    }

    public static void setPdfDirectory(String dir) {
        prefs.put(KEY_PDF_DIR, dir);
    }
}

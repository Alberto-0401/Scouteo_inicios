package com.javafx.scouteo.controller;

import com.javafx.scouteo.util.Preferencias;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class AjustesController {

    @FXML private TextField        txtRutaPdf;
    @FXML private Label            lblMensajePdf;
    @FXML private TextField        txtFirmante;
    @FXML private TextField        txtPiePdf;
    @FXML private CheckBox         chkAbrirPdfAuto;
    @FXML private ComboBox<String> cmbVistaInicio;
    @FXML private CheckBox         chkConfirmarEliminar;
    @FXML private CheckBox         chkOcultarBajas;
    @FXML private Label            lblEstado;

    private static final Map<String, String> VISTAS = new LinkedHashMap<>();
    static {
        VISTAS.put("Inicio (Info del club)", "inicio");
        VISTAS.put("Jugadores",              "jugadores");
        VISTAS.put("Partidos",               "partidos");
        VISTAS.put("Entrenamientos",         "entrenamientos");
    }

    @FXML
    public void initialize() {
        txtRutaPdf.setText(Preferencias.getPdfDirectory());
        txtFirmante.setText(Preferencias.getFirmantePdf());
        txtPiePdf.setText(Preferencias.getPiePaginaPdf());
        chkAbrirPdfAuto.setSelected(Preferencias.getAbrirPdfAuto());
        chkConfirmarEliminar.setSelected(Preferencias.getConfirmarEliminacion());
        chkOcultarBajas.setSelected(Preferencias.getOcultarJugadoresBaja());

        cmbVistaInicio.setItems(FXCollections.observableArrayList(VISTAS.keySet()));
        String vistaGuardada = Preferencias.getVistaInicio();
        VISTAS.entrySet().stream()
                .filter(e -> e.getValue().equals(vistaGuardada))
                .map(Map.Entry::getKey)
                .findFirst()
                .ifPresent(cmbVistaInicio::setValue);
    }

    @FXML
    private void cambiarCarpetaPdf() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Seleccionar carpeta para PDFs");
        File actual = new File(Preferencias.getPdfDirectory());
        if (actual.exists()) chooser.setInitialDirectory(actual);

        File seleccionada = chooser.showDialog((Stage) txtRutaPdf.getScene().getWindow());
        if (seleccionada != null) {
            Preferencias.setPdfDirectory(seleccionada.getAbsolutePath());
            txtRutaPdf.setText(seleccionada.getAbsolutePath());
            lblMensajePdf.setText("Carpeta actualizada");
            lblMensajePdf.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void aplicar() {
        Preferencias.setFirmantePdf(txtFirmante.getText().trim());
        Preferencias.setPiePaginaPdf(txtPiePdf.getText().trim());
        Preferencias.setAbrirPdfAuto(chkAbrirPdfAuto.isSelected());
        Preferencias.setConfirmarEliminacion(chkConfirmarEliminar.isSelected());
        Preferencias.setOcultarJugadoresBaja(chkOcultarBajas.isSelected());

        String vistaSeleccionada = cmbVistaInicio.getValue();
        if (vistaSeleccionada != null)
            Preferencias.setVistaInicio(VISTAS.getOrDefault(vistaSeleccionada, "inicio"));

        lblEstado.setText("Cambios aplicados correctamente");
        lblEstado.setStyle("-fx-text-fill: #4CAF50;");
    }
}

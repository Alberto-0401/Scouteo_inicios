package com.javafx.scouteo.controller;

import com.javafx.scouteo.model.Configuracion;
import com.javafx.scouteo.dao.ConfiguracionDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class ConfiguracionController {

    @FXML
    private ImageView imgEscudo;

    @FXML
    private Label lblMensaje;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtLocalidad;

    @FXML
    private TextField txtNombreClub;

    @FXML
    private TextField txtPresidente;

    @FXML
    private TextField txtTelefono;

    @FXML
    private TextField txtTemporada;

    private ConfiguracionDAO configuracionDAO;
    private Configuracion configuracion;
    private byte[] escudoSeleccionado;

    @FXML
    public void initialize() {
        configuracionDAO = new ConfiguracionDAO();
        cargarDatos();
    }

    private void cargarDatos() {
        configuracion = configuracionDAO.obtener();

        if (configuracion != null) {
            txtNombreClub.setText(configuracion.getNombreClub());
            txtLocalidad.setText(configuracion.getLocalidad());
            txtPresidente.setText(configuracion.getPresidente());
            txtEmail.setText(configuracion.getEmail());
            txtTelefono.setText(configuracion.getTelefono());
            txtTemporada.setText(configuracion.getTemporadaActual());

            // Cargar escudo si existe
            if (configuracion.getEscudo() != null && configuracion.getEscudo().length > 0) {
                escudoSeleccionado = configuracion.getEscudo();
                Image imagen = new Image(new ByteArrayInputStream(configuracion.getEscudo()));
                imgEscudo.setImage(imagen);
            }
        }
    }

    @FXML
    void cambiarEscudo(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar escudo del club");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imagenes", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) txtNombreClub.getScene().getWindow();
        File archivo = fileChooser.showOpenDialog(stage);

        if (archivo != null) {
            try {
                escudoSeleccionado = Files.readAllBytes(archivo.toPath());
                Image imagen = new Image(new ByteArrayInputStream(escudoSeleccionado));
                imgEscudo.setImage(imagen);
                lblMensaje.setText("Escudo seleccionado: " + archivo.getName());
                lblMensaje.setStyle("-fx-text-fill: blue;");
            } catch (IOException e) {
                lblMensaje.setText("Error al cargar la imagen");
                lblMensaje.setStyle("-fx-text-fill: red;");
                e.printStackTrace();
            }
        }
    }

    @FXML
    void guardarCambios(ActionEvent event) {
        lblMensaje.setText("");

        // Validar campos
        if (txtNombreClub.getText().trim().isEmpty()) {
            lblMensaje.setText("El nombre del club es obligatorio");
            lblMensaje.setStyle("-fx-text-fill: red;");
            txtNombreClub.requestFocus();
            return;
        }

        if (txtLocalidad.getText().trim().isEmpty()) {
            lblMensaje.setText("La localidad es obligatoria");
            lblMensaje.setStyle("-fx-text-fill: red;");
            txtLocalidad.requestFocus();
            return;
        }

        if (txtPresidente.getText().trim().isEmpty()) {
            lblMensaje.setText("El presidente es obligatorio");
            lblMensaje.setStyle("-fx-text-fill: red;");
            txtPresidente.requestFocus();
            return;
        }

        if (txtEmail.getText().trim().isEmpty()) {
            lblMensaje.setText("El email es obligatorio");
            lblMensaje.setStyle("-fx-text-fill: red;");
            txtEmail.requestFocus();
            return;
        }

        // Validar formato email
        if (!txtEmail.getText().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            lblMensaje.setText("El formato del email no es valido");
            lblMensaje.setStyle("-fx-text-fill: red;");
            txtEmail.requestFocus();
            return;
        }

        if (txtTelefono.getText().trim().isEmpty()) {
            lblMensaje.setText("El telefono es obligatorio");
            lblMensaje.setStyle("-fx-text-fill: red;");
            txtTelefono.requestFocus();
            return;
        }

        if (txtTemporada.getText().trim().isEmpty()) {
            lblMensaje.setText("La temporada es obligatoria");
            lblMensaje.setStyle("-fx-text-fill: red;");
            txtTemporada.requestFocus();
            return;
        }

        // Validar formato temporada
        if (!txtTemporada.getText().matches("^\\d{4}/\\d{2}$")) {
            lblMensaje.setText("La temporada debe tener el formato YYYY/YY (ej: 2024/25)");
            lblMensaje.setStyle("-fx-text-fill: red;");
            txtTemporada.requestFocus();
            return;
        }

        // Actualizar configuracion
        if (configuracion == null) {
            configuracion = new Configuracion();
            configuracion.setIdConfig(1);
        }

        configuracion.setNombreClub(txtNombreClub.getText().trim());
        configuracion.setLocalidad(txtLocalidad.getText().trim());
        configuracion.setPresidente(txtPresidente.getText().trim());
        configuracion.setEmail(txtEmail.getText().trim());
        configuracion.setTelefono(txtTelefono.getText().trim());
        configuracion.setTemporadaActual(txtTemporada.getText().trim());

        if (escudoSeleccionado != null) {
            configuracion.setEscudo(escudoSeleccionado);
        }

        // Guardar
        boolean exito = configuracionDAO.actualizar(configuracion);

        if (exito) {
            lblMensaje.setText("Configuracion guardada correctamente");
            lblMensaje.setStyle("-fx-text-fill: green;");
        } else {
            lblMensaje.setText("Error al guardar la configuracion");
            lblMensaje.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    void volverDashboard(ActionEvent event) {
        Stage stage = (Stage) txtNombreClub.getScene().getWindow();
        stage.close();
    }
}

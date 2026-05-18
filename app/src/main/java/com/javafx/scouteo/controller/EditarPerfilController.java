package com.javafx.scouteo.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.javafx.scouteo.util.ApiClient;
import com.javafx.scouteo.util.SesionUsuario;
import com.javafx.scouteo.utils.StageUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;

public class EditarPerfilController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtTelefono;
    @FXML private PasswordField txtPass1;
    @FXML private PasswordField txtPass2;
    @FXML private Label lblMensaje;

    @FXML
    public void initialize() {
        var usuario = SesionUsuario.getInstance().getUsuarioActual();
        txtNombre.setText(usuario.getNombre() != null ? usuario.getNombre() : "");
        txtApellidos.setText(usuario.getApellidos() != null ? usuario.getApellidos() : "");
        txtTelefono.setText(usuario.getTelefono() != null ? usuario.getTelefono() : "");
    }

    @FXML
    void guardarPerfil(ActionEvent event) {
        lblMensaje.setText("");
        String nombre    = txtNombre.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String pass1     = txtPass1.getText();
        String pass2     = txtPass2.getText();

        if (nombre.isEmpty()) {
            lblMensaje.setText("El nombre es obligatorio.");
            lblMensaje.setStyle("-fx-text-fill: #d32f2f;");
            return;
        }
        if (!pass1.isEmpty()) {
            if (pass1.length() < 6) {
                lblMensaje.setText("La contrasena debe tener al menos 6 caracteres.");
                lblMensaje.setStyle("-fx-text-fill: #d32f2f;");
                return;
            }
            if (!pass1.equals(pass2)) {
                lblMensaje.setText("Las contrasenas no coinciden.");
                lblMensaje.setStyle("-fx-text-fill: #d32f2f;");
                return;
            }
        }

        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("nombre",    nombre);
        body.put("apellidos", apellidos);
        body.put("telefono",  txtTelefono.getText().trim());
        if (!pass1.isEmpty()) body.put("password", pass1);

        int id = SesionUsuario.getInstance().getUsuarioActual().getId();
        String json = ApiClient.getInstance().put("/usuarios/" + id, body);
        if (json != null) {
            SesionUsuario.getInstance().getUsuarioActual().setNombre(nombre);
            SesionUsuario.getInstance().getUsuarioActual().setApellidos(apellidos);
            txtPass1.clear();
            txtPass2.clear();
            lblMensaje.setText("Perfil actualizado correctamente.");
            lblMensaje.setStyle("-fx-text-fill: #388e3c;");
        } else {
            lblMensaje.setText("Error al guardar. Intentalo de nuevo.");
            lblMensaje.setStyle("-fx-text-fill: #d32f2f;");
        }
    }

    @FXML
    void eliminarCuenta(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar cuenta");
        confirm.setHeaderText("¿Seguro que quieres eliminar tu cuenta?");
        confirm.setContentText("Esta accion desactivara tu acceso permanentemente.\nNo podras iniciar sesion con esta cuenta.");
        confirm.setOnShowing(e -> StageUtils.setAppIcon((Stage) confirm.getDialogPane().getScene().getWindow()));

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        int usuarioId = SesionUsuario.getInstance().getUsuarioActual().getId();
        String response = ApiClient.getInstance().deleteWithBody("/usuarios/" + usuarioId);

        if (response != null) {
            try {
                JsonObject json = JsonParser.parseString(response).getAsJsonObject();
                if (json.has("clubDesactivado") && json.get("clubDesactivado").getAsBoolean()) {
                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.setTitle("Club desactivado");
                    info.setHeaderText("El club ha sido desactivado");
                    info.setContentText("No quedan directivos activos en el club.\nEl club ha sido desactivado automaticamente en el sistema.");
                    info.setOnShowing(e -> StageUtils.setAppIcon((Stage) info.getDialogPane().getScene().getWindow()));
                    info.showAndWait();
                }
            } catch (Exception ignored) {}

            SesionUsuario.getInstance().cerrarSesion();
            Stage modal = (Stage) txtNombre.getScene().getWindow();
            Window owner = modal.getOwner();
            modal.close();

            if (owner instanceof Stage dashboardStage) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Login.fxml"));
                    Parent root = loader.load();
                    Scene scene = new Scene(root);
                    scene.getStylesheets().add(getClass().getClassLoader().getResource("scouteo.css").toExternalForm());
                    StageUtils.setAppIcon(dashboardStage);
                    dashboardStage.setScene(scene);
                    dashboardStage.setTitle("SCOUTEO - Iniciar Sesion");
                    dashboardStage.setWidth(900);
                    dashboardStage.setHeight(600);
                    dashboardStage.setResizable(false);
                    dashboardStage.centerOnScreen();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Error");
            error.setHeaderText(null);
            error.setContentText("No se pudo eliminar la cuenta. Intentalo de nuevo.");
            error.setOnShowing(e -> StageUtils.setAppIcon((Stage) error.getDialogPane().getScene().getWindow()));
            error.showAndWait();
        }
    }

    @FXML
    void volver(ActionEvent event) {
        ((Stage) txtNombre.getScene().getWindow()).close();
    }
}

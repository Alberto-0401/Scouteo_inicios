package com.javafx.scouteo.controller;

import com.javafx.scouteo.dao.ClubesDAO;
import com.javafx.scouteo.dao.UsuarioDAO;
import com.javafx.scouteo.model.Usuario;
import com.javafx.scouteo.util.ApiClient;
import com.javafx.scouteo.util.ConexionBD;
import com.javafx.scouteo.util.SesionUsuario;
import com.javafx.scouteo.utils.StageUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;
    @FXML private Button btnLogin;
    @FXML private ProgressIndicator progressIndicator;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final ClubesDAO  clubesDAO  = new ClubesDAO();

    @FXML
    public void initialize() {
        lblError.setText("");
        txtEmail.setText("h.flick@fcbarcelona.cat");
        txtPassword.setText("Flick2024!");
        // Enter en el campo de contraseña dispara el login
        txtPassword.setOnAction(e -> iniciarSesion());
    }

    @FXML
    private void iniciarSesion() {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();

        lblError.setText("");

        if (email.isEmpty() || password.isEmpty()) {
            mostrarError("Introduce tu correo y contrasena.");
            return;
        }

        if (!ApiClient.getInstance().isDisponible()) {
            mostrarError("No se puede conectar al servidor.\nVerifica que la API esta en marcha.");
            return;
        }

        btnLogin.setDisable(true);
        progressIndicator.setVisible(true);

        Thread authThread = new Thread(() -> {
            // Build login request
            java.util.Map<String, String> body = new java.util.HashMap<>();
            body.put("email", email);
            body.put("password", password);
            String json = ApiClient.getInstance().post("/auth/login", body);

            Platform.runLater(() -> {
                progressIndicator.setVisible(false);
                btnLogin.setDisable(false);

                if (json != null) {
                    com.google.gson.JsonObject resp = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
                    String token = resp.get("token").getAsString();
                    String rol   = resp.get("rol").getAsString();
                    String nombre = resp.get("nombre").getAsString();
                    String apellidos = resp.has("apellidos") && !resp.get("apellidos").isJsonNull()
                        ? resp.get("apellidos").getAsString() : "";
                    int clubId    = resp.get("clubId").getAsInt();
                    int usuarioId = resp.get("usuarioId").getAsInt();
                    Integer equipoId = resp.has("equipoId") && !resp.get("equipoId").isJsonNull()
                        ? resp.get("equipoId").getAsInt() : null;
                    Integer jugadorId = resp.has("jugadorId") && !resp.get("jugadorId").isJsonNull()
                        ? resp.get("jugadorId").getAsInt() : null;

                    Usuario usuario = new Usuario();
                    usuario.setId(usuarioId);
                    usuario.setNombre(nombre);
                    usuario.setApellidos(apellidos);
                    usuario.setEmail(email);
                    usuario.setRol(rol);
                    usuario.setClubId(clubId);
                    usuario.setActivo(true);

                    SesionUsuario sesion = SesionUsuario.getInstance();
                    sesion.iniciarSesion(usuario);
                    sesion.setJwtToken(token);
                    sesion.setEquipoId(equipoId);
                    sesion.setJugadorId(jugadorId);

                    abrirDashboard();
                } else {
                    mostrarError("Correo o contrasena incorrectos,\no cuenta inactiva.");
                    txtPassword.clear();
                    txtPassword.requestFocus();
                }
            });
        });
        authThread.setDaemon(true);
        authThread.start();
    }

    private void abrirDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            String css = getClass().getClassLoader().getResource("scouteo.css").toExternalForm();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(css);

            StageUtils.setAppIcon(stage);
            stage.setScene(scene);
            stage.setTitle("SCOUTEO - " + SesionUsuario.getInstance().getUsuarioActual().getNombreCompleto());
            stage.setWidth(1280);
            stage.setHeight(780);
            stage.setResizable(false);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("Error al cargar el dashboard.");
        }
    }

    @FXML
    private void registrarse() {
        if (ConexionBD.getConexion() == null) {
            mostrarError("No se puede conectar a la base de datos.");
            return;
        }

        // Campos del formulario
        TextField txtNombre      = new TextField(); txtNombre.setPromptText("Nombre");
        TextField txtApellidos   = new TextField(); txtApellidos.setPromptText("Apellidos");
        TextField txtEmailReg    = new TextField(); txtEmailReg.setPromptText("correo@club.es");
        PasswordField txtPass1   = new PasswordField(); txtPass1.setPromptText("Contrasena");
        PasswordField txtPass2   = new PasswordField(); txtPass2.setPromptText("Repetir contrasena");
        Label lblRegError        = new Label();
        lblRegError.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 11px;");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setStyle("-fx-padding: 20;");
        grid.add(new Label("Nombre:"),     0, 0); grid.add(txtNombre,    1, 0);
        grid.add(new Label("Apellidos:"),  0, 1); grid.add(txtApellidos, 1, 1);
        grid.add(new Label("Email:"),      0, 2); grid.add(txtEmailReg,  1, 2);
        grid.add(new Label("Contrasena:"), 0, 3); grid.add(txtPass1,     1, 3);
        grid.add(new Label("Repetir:"),    0, 4); grid.add(txtPass2,     1, 4);
        grid.add(lblRegError,              0, 5, 2, 1);

        txtNombre.setPrefWidth(220);
        txtApellidos.setPrefWidth(220);
        txtEmailReg.setPrefWidth(220);
        txtPass1.setPrefWidth(220);
        txtPass2.setPrefWidth(220);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Crear cuenta de directiva");
        dialog.setHeaderText("Nueva cuenta — rol: Directiva");
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getClassLoader().getResource("scouteo.css").toExternalForm());
        dialog.setOnShowing(e ->
                StageUtils.setAppIcon((Stage) dialog.getDialogPane().getScene().getWindow()));

        // Validar antes de cerrar al pulsar OK
        Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            String nombre    = txtNombre.getText().trim();
            String apellidos = txtApellidos.getText().trim();
            String email     = txtEmailReg.getText().trim();
            String pass1     = txtPass1.getText();
            String pass2     = txtPass2.getText();

            if (nombre.isEmpty() || apellidos.isEmpty() || email.isEmpty() || pass1.isEmpty()) {
                lblRegError.setText("Todos los campos son obligatorios.");
                e.consume(); return;
            }
            if (!email.contains("@")) {
                lblRegError.setText("El email no es valido.");
                e.consume(); return;
            }
            if (pass1.length() < 6) {
                lblRegError.setText("La contrasena debe tener al menos 6 caracteres.");
                e.consume(); return;
            }
            if (!pass1.equals(pass2)) {
                lblRegError.setText("Las contrasenas no coinciden.");
                e.consume(); return;
            }
            if (usuarioDAO.existeEmail(email)) {
                lblRegError.setText("Ese email ya esta registrado.");
                e.consume(); return;
            }
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String nombreClub = txtNombre.getText().trim() + " Club";
            int clubId = clubesDAO.insertar(nombreClub);
            if (clubId < 0) {
                mostrarError("Error al crear el club. Revisa la conexion.");
                return;
            }

            Usuario nuevo = new Usuario();
            nuevo.setNombre(txtNombre.getText().trim());
            nuevo.setApellidos(txtApellidos.getText().trim());
            nuevo.setEmail(txtEmailReg.getText().trim());
            nuevo.setRol("directiva");
            nuevo.setActivo(true);
            nuevo.setClubId(clubId);

            int id = usuarioDAO.insertar(nuevo, txtPass1.getText());
            if (id > 0) {
                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setTitle("Cuenta creada");
                ok.setHeaderText(null);
                ok.setContentText("Cuenta creada correctamente. Ya puedes iniciar sesion.");
                ok.setOnShowing(e -> StageUtils.setAppIcon((Stage) ok.getDialogPane().getScene().getWindow()));
                ok.showAndWait();
                txtEmail.setText(txtEmailReg.getText().trim());
                txtPassword.requestFocus();
            } else {
                mostrarError("Error al crear la cuenta. Revisa la conexion.");
            }
        }
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
    }
}

package com.javafx.scouteo.controller;

import com.javafx.scouteo.dao.JugadorDAO;
import com.javafx.scouteo.model.Equipo;
import com.javafx.scouteo.model.Jugador;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import java.io.File;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FormJugadorController {

    @FXML private ComboBox<String> cmbCategoria;   // repurposed: pierna dominante
    @FXML private ComboBox<String> cmbPosicion;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private Label lblError;
    @FXML private Label lblNombreFoto;
    @FXML private Label lblTitulo;
    @FXML private TextField txtAltura;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtDorsal;
    @FXML private TextField txtNombre;
    @FXML private TextField txtPeso;

    private JugadorDAO jugadorDAO;
    private ListadoJugadoresController listadoController;
    private Jugador jugadorEditar;
    private String rutaFoto;
    private Equipo equipoActivo;
    private List<ValidationSupport> validadores;

    @FXML
    public void initialize() {
        jugadorDAO = new JugadorDAO();
        configurarValidaciones();
    }

    private void configurarValidaciones() {
        ValidationSupport vsNombre = new ValidationSupport();
        vsNombre.registerValidator(txtNombre, (Control c, String texto) -> {
            if (texto == null || texto.trim().isEmpty())
                return ValidationResult.fromWarning(c, "El nombre no debe estar vacio");
            if (texto.length() < 2 || texto.length() > 50)
                return ValidationResult.fromError(c, "El nombre debe tener entre 2 y 50 caracteres");
            return ValidationResult.fromInfo(c, "OK");
        });

        ValidationSupport vsApellidos = new ValidationSupport();
        vsApellidos.registerValidator(txtApellidos, (Control c, String texto) -> {
            if (texto == null || texto.trim().isEmpty())
                return ValidationResult.fromWarning(c, "Los apellidos no deben estar vacios");
            if (texto.length() < 2 || texto.length() > 100)
                return ValidationResult.fromError(c, "Los apellidos deben tener entre 2 y 100 caracteres");
            return ValidationResult.fromInfo(c, "OK");
        });

        ValidationSupport vsDorsal = new ValidationSupport();
        vsDorsal.registerValidator(txtDorsal, Validator.<String>createPredicateValidator(valor -> {
            if (valor == null || valor.isEmpty()) return false;
            try { int n = Integer.parseInt(valor); return n >= 1 && n <= 99; }
            catch (NumberFormatException e) { return false; }
        }, "El dorsal debe ser un numero entre 1 y 99"));

        ValidationSupport vsAltura = new ValidationSupport();
        vsAltura.registerValidator(txtAltura, false, (Control c, String texto) -> {
            if (texto != null && !texto.trim().isEmpty()) {
                try {
                    double v = Double.parseDouble(texto);
                    if (v < 100 || v > 230) return ValidationResult.fromError(c, "Altura entre 100 y 230 cm");
                } catch (NumberFormatException e) { return ValidationResult.fromError(c, "Debe ser un numero"); }
            }
            return ValidationResult.fromInfo(c, "OK");
        });

        ValidationSupport vsPeso = new ValidationSupport();
        vsPeso.registerValidator(txtPeso, false, (Control c, String texto) -> {
            if (texto != null && !texto.trim().isEmpty()) {
                try {
                    double v = Double.parseDouble(texto);
                    if (v < 20 || v > 150) return ValidationResult.fromError(c, "Peso entre 20 y 150 kg");
                } catch (NumberFormatException e) { return ValidationResult.fromError(c, "Debe ser un numero"); }
            }
            return ValidationResult.fromInfo(c, "OK");
        });

        ValidationSupport vsFecha = new ValidationSupport();
        vsFecha.registerValidator(dpFechaNacimiento, (Control c, LocalDate fecha) -> {
            if (fecha == null) return ValidationResult.fromWarning(c, "La fecha de nacimiento es obligatoria");
            int edad = Period.between(fecha, LocalDate.now()).getYears();
            if (edad < 4 || edad > 45) return ValidationResult.fromError(c, "Edad debe estar entre 4 y 45 anos");
            return ValidationResult.fromInfo(c, "OK");
        });

        ValidationSupport vsPosicion = new ValidationSupport();
        vsPosicion.registerValidator(cmbPosicion,
                Validator.<String>createPredicateValidator(t -> t != null && !t.isEmpty(), "Selecciona una posicion"));

        validadores = new ArrayList<>(Arrays.asList(vsNombre, vsApellidos, vsDorsal, vsAltura, vsPeso, vsFecha, vsPosicion));
        Platform.runLater(() -> validadores.forEach(ValidationSupport::initInitialDecoration));
    }

    public void setListadoController(ListadoJugadoresController controller) {
        this.listadoController = controller;
    }

    public void setEquipoActivo(Equipo equipo) {
        this.equipoActivo = equipo;
    }

    public void setJugadorEditar(Jugador jugador) {
        this.jugadorEditar = jugador;
        if (jugador != null) {
            lblTitulo.setText("Editar Jugador");
            cargarDatosJugador(jugador);
        }
    }

    private void cargarDatosJugador(Jugador jugador) {
        txtNombre.setText(jugador.getNombre());
        txtApellidos.setText(jugador.getApellidos());
        txtDorsal.setText(String.valueOf(jugador.getDorsal()));
        dpFechaNacimiento.setValue(jugador.getFechaNacimiento());
        cmbPosicion.setValue(jugador.getPosicion());
        cmbCategoria.setValue(jugador.getPiernaDominante() != null ? jugador.getPiernaDominante() : "derecha");

        if (jugador.getAlturaCm() != null) txtAltura.setText(String.valueOf(jugador.getAlturaCm()));
        if (jugador.getPesoKg() != null)   txtPeso.setText(String.valueOf(jugador.getPesoKg()));
        if (jugador.getFotoUrl() != null) {
            rutaFoto = jugador.getFotoUrl();
            lblNombreFoto.setText("Foto cargada");
        }
    }

    @FXML
    void guardar(ActionEvent event) {
        lblError.setText("");

        boolean todoOK = validadores.stream()
                .allMatch(vs -> vs.getValidationResult().getErrors().isEmpty());
        if (!todoOK) {
            lblError.setText("Por favor, corrija los errores del formulario");
            return;
        }

        int dorsal;
        try {
            dorsal = Integer.parseInt(txtDorsal.getText().trim());
        } catch (NumberFormatException e) {
            lblError.setText("El dorsal debe ser un numero");
            txtDorsal.requestFocus();
            return;
        }

        int equipoId = 0;
        if (jugadorEditar != null && jugadorEditar.getEquipoId() != null) {
            equipoId = jugadorEditar.getEquipoId();
        } else if (equipoActivo != null) {
            equipoId = equipoActivo.getId();
        }

        if (equipoId == 0) {
            lblError.setText("No hay equipo activo seleccionado");
            return;
        }

        Integer idExcluir = (jugadorEditar != null) ? jugadorEditar.getId() : null;
        if (!jugadorDAO.dorsalDisponible(dorsal, equipoId, idExcluir)) {
            lblError.setText("El dorsal ya esta en uso en este equipo");
            txtDorsal.requestFocus();
            return;
        }

        Jugador jugador = (jugadorEditar != null) ? jugadorEditar : new Jugador();
        jugador.setNombre(txtNombre.getText().trim());
        jugador.setApellidos(txtApellidos.getText().trim());
        jugador.setDorsal(dorsal);
        jugador.setFechaNacimiento(dpFechaNacimiento.getValue());
        jugador.setPosicion(cmbPosicion.getValue());
        jugador.setPiernaDominante(cmbCategoria.getValue() != null ? cmbCategoria.getValue() : "derecha");
        jugador.setEstado("activo");
        jugador.setEquipoId(equipoId);

        String alturaText = txtAltura.getText().trim();
        if (!alturaText.isEmpty()) {
            try { jugador.setAlturaCm((int) Double.parseDouble(alturaText)); }
            catch (NumberFormatException ignored) {}
        }

        String pesoText = txtPeso.getText().trim();
        if (!pesoText.isEmpty()) {
            try { jugador.setPesoKg(Double.parseDouble(pesoText)); }
            catch (NumberFormatException ignored) {}
        }

        if (rutaFoto != null) jugador.setFotoUrl(rutaFoto);

        boolean exito = (jugadorEditar != null) ? jugadorDAO.actualizar(jugador) : jugadorDAO.insertar(jugador) > 0;

        if (exito) {
            if (listadoController != null) listadoController.cargarJugadores();
            cerrarVentana();
        } else {
            lblError.setText("Error al guardar el jugador");
        }
    }

    @FXML
    void cancelar(ActionEvent event) {
        cerrarVentana();
    }

    @FXML
    void seleccionarFoto(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar foto del jugador");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Imagenes", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        File archivo = fileChooser.showOpenDialog(stage);
        if (archivo != null) {
            rutaFoto = archivo.getAbsolutePath();
            lblNombreFoto.setText(archivo.getName());
        }
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }
}

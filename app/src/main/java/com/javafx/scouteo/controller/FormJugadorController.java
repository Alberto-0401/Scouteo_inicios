package com.javafx.scouteo.controller;

import com.javafx.scouteo.dao.JugadorDAO;
import com.javafx.scouteo.model.Jugador;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FormJugadorController {

    @FXML
    private ComboBox<String> cmbCategoria;

    @FXML
    private ComboBox<String> cmbPosicion;

    @FXML
    private DatePicker dpFechaNacimiento;

    @FXML
    private Label lblError;

    @FXML
    private Label lblNombreFoto;

    @FXML
    private Label lblTitulo;

    @FXML
    private TextField txtAltura;

    @FXML
    private TextField txtApellidos;

    @FXML
    private TextField txtDorsal;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtPeso;

    private JugadorDAO jugadorDAO;
    private ListadoJugadoresController listadoController;
    private Jugador jugadorEditar;
    private byte[] fotoSeleccionada;
    private List<ValidationSupport> validadores;

    @FXML
    public void initialize() {
        jugadorDAO = new JugadorDAO();
        configurarValidaciones();
    }

    private void configurarValidaciones() {
        // Validador para Nombre
        ValidationSupport vsNombre = new ValidationSupport();
        vsNombre.registerValidator(txtNombre, (Control c, String texto) -> {
            if (texto == null || texto.trim().isEmpty()) {
                return ValidationResult.fromWarning(c, "El nombre no debe estar vacío");
            } else if (texto.length() < 2 || texto.length() > 50) {
                return ValidationResult.fromError(c, "El nombre debe tener entre 2 y 50 caracteres");
            } else {
                return ValidationResult.fromInfo(c, "OK");
            }
        });

        // Validador para Apellidos
        ValidationSupport vsApellidos = new ValidationSupport();
        vsApellidos.registerValidator(txtApellidos, (Control c, String texto) -> {
            if (texto == null || texto.trim().isEmpty()) {
                return ValidationResult.fromWarning(c, "Los apellidos no deben estar vacíos");
            } else if (texto.length() < 2 || texto.length() > 100) {
                return ValidationResult.fromError(c, "Los apellidos deben tener entre 2 y 100 caracteres");
            } else {
                return ValidationResult.fromInfo(c, "OK");
            }
        });

        // Validador para Dorsal
        ValidationSupport vsDorsal = new ValidationSupport();
        Validator<String> dorsalValidator = Validator.createPredicateValidator(valor -> {
            if (valor == null || valor.isEmpty()) {
                return false;
            }
            try {
                int number = Integer.parseInt(valor);
                return number >= 1 && number <= 99;
            } catch (NumberFormatException e) {
                return false;
            }
        }, "El dorsal debe ser un número entre 1 y 99");
        vsDorsal.registerValidator(txtDorsal, dorsalValidator);

        // Validador para Altura (opcional)
        ValidationSupport vsAltura = new ValidationSupport();
        vsAltura.registerValidator(txtAltura, false, (Control c, String texto) -> {
            if (texto != null && !texto.trim().isEmpty()) {
                try {
                    double altura = Double.parseDouble(texto);
                    if (altura < 100 || altura > 220) {
                        return ValidationResult.fromError(c, "La altura debe estar entre 100 y 220 cm");
                    }
                } catch (NumberFormatException e) {
                    return ValidationResult.fromError(c, "La altura debe ser un número válido");
                }
            }
            return ValidationResult.fromInfo(c, "OK");
        });

        // Validador para Peso (opcional)
        ValidationSupport vsPeso = new ValidationSupport();
        vsPeso.registerValidator(txtPeso, false, (Control c, String texto) -> {
            if (texto != null && !texto.trim().isEmpty()) {
                try {
                    double peso = Double.parseDouble(texto);
                    if (peso < 20 || peso > 120) {
                        return ValidationResult.fromError(c, "El peso debe estar entre 20 y 120 kg");
                    }
                } catch (NumberFormatException e) {
                    return ValidationResult.fromError(c, "El peso debe ser un número válido");
                }
            }
            return ValidationResult.fromInfo(c, "OK");
        });

        // Validador para Fecha de Nacimiento
        ValidationSupport vsFecha = new ValidationSupport();
        vsFecha.registerValidator(dpFechaNacimiento, (Control c, LocalDate fecha) -> {
            if (fecha == null) {
                return ValidationResult.fromWarning(c, "La fecha de nacimiento es obligatoria");
            } else {
                int edad = Period.between(fecha, LocalDate.now()).getYears();
                if (edad < 5 || edad > 25) {
                    return ValidationResult.fromError(c, "La edad debe estar entre 5 y 25 años");
                }
            }
            return ValidationResult.fromInfo(c, "OK");
        });

        // Validador para Posición
        ValidationSupport vsPosicion = new ValidationSupport();
        Validator<String> posicionValidator = Validator.createPredicateValidator(
                texto -> texto != null && !texto.trim().isEmpty(),
                "Debe seleccionar una posición"
        );
        vsPosicion.registerValidator(cmbPosicion, posicionValidator);

        // Validador para Categoría
        ValidationSupport vsCategoria = new ValidationSupport();
        Validator<String> categoriaValidator = Validator.createPredicateValidator(
                texto -> texto != null && !texto.trim().isEmpty(),
                "Debe seleccionar una categoría"
        );
        vsCategoria.registerValidator(cmbCategoria, categoriaValidator);

        // Registro de todos los validadores
        validadores = new ArrayList<>();
        validadores.addAll(Arrays.asList(vsNombre, vsApellidos, vsDorsal, vsAltura, vsPeso, vsFecha, vsPosicion, vsCategoria));

        // Inicializar decoración en un nuevo hilo
        Platform.runLater(() -> {
            for (ValidationSupport validationSupport : validadores) {
                validationSupport.initInitialDecoration();
            }
        });
    }

    public void setListadoController(ListadoJugadoresController controller) {
        this.listadoController = controller;
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
        cmbCategoria.setValue(jugador.getCategoria());

        if (jugador.getAltura() != null) {
            txtAltura.setText(String.valueOf(jugador.getAltura()));
        }
        if (jugador.getPeso() != null) {
            txtPeso.setText(String.valueOf(jugador.getPeso()));
        }
        if (jugador.getFoto() != null) {
            fotoSeleccionada = jugador.getFoto();
            lblNombreFoto.setText("Foto cargada");
        }
    }

    @FXML
    void guardar(ActionEvent event) {
        lblError.setText("");

        // Validar con los validadores de ControlsFX
        boolean todoOK = true;
        for (ValidationSupport validationSupport : validadores) {
            todoOK = todoOK && validationSupport.getValidationResult().getErrors().isEmpty();
        }

        if (!todoOK) {
            lblError.setText("Por favor, corrija los errores del formulario");
            return;
        }

        // Validar dorsal numerico y disponible
        int dorsal;
        try {
            dorsal = Integer.parseInt(txtDorsal.getText().trim());
        } catch (NumberFormatException e) {
            lblError.setText("El dorsal debe ser un numero");
            txtDorsal.requestFocus();
            return;
        }

        // Verificar dorsal disponible
        Integer idExcluir = (jugadorEditar != null) ? jugadorEditar.getId() : null;
        if (!jugadorDAO.dorsalDisponible(dorsal, idExcluir)) {
            lblError.setText("El dorsal ya esta en uso por otro jugador");
            txtDorsal.requestFocus();
            return;
        }

        // Crear o actualizar jugador
        Jugador jugador = (jugadorEditar != null) ? jugadorEditar : new Jugador();
        jugador.setNombre(txtNombre.getText().trim());
        jugador.setApellidos(txtApellidos.getText().trim());
        jugador.setDorsal(dorsal);
        jugador.setFechaNacimiento(dpFechaNacimiento.getValue());
        jugador.setPosicion(cmbPosicion.getValue());
        jugador.setCategoria(cmbCategoria.getValue());
        jugador.setEstado("Activo");

        // Altura (opcional)
        if (!txtAltura.getText().trim().isEmpty()) {
            jugador.setAltura(Double.parseDouble(txtAltura.getText().trim()));
        }

        // Peso (opcional)
        if (!txtPeso.getText().trim().isEmpty()) {
            jugador.setPeso(Double.parseDouble(txtPeso.getText().trim()));
        }

        // Foto
        if (fotoSeleccionada != null) {
            jugador.setFoto(fotoSeleccionada);
        }

        // Guardar
        boolean exito;
        if (jugadorEditar != null) {
            exito = jugadorDAO.actualizar(jugador);
        } else {
            exito = jugadorDAO.insertar(jugador) > 0;
        }

        if (exito) {
            if (listadoController != null) {
                listadoController.cargarJugadores();
            }
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
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imagenes", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) txtNombre.getScene().getWindow();
        File archivo = fileChooser.showOpenDialog(stage);

        if (archivo != null) {
            try {
                fotoSeleccionada = Files.readAllBytes(archivo.toPath());
                lblNombreFoto.setText(archivo.getName());
            } catch (IOException e) {
                lblError.setText("Error al cargar la imagen");
                e.printStackTrace();
            }
        }
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }
}

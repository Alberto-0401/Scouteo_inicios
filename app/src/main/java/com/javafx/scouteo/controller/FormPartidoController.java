package com.javafx.scouteo.controller;

import com.javafx.scouteo.dao.PartidoDAO;
import com.javafx.scouteo.model.Partido;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FormPartidoController {

    @FXML
    private DatePicker dpFechaPartido;

    @FXML
    private TextField txtRival;

    @FXML
    private TextField txtResultado;

    @FXML
    private ComboBox<String> cmbLocalVisitante;

    @FXML
    private TextArea txtObservaciones;

    @FXML
    private Label lblError;

    private PartidoDAO partidoDAO;
    private PartidosController partidosController;
    private Partido partidoEditar;
    private List<ValidationSupport> validadores;

    @FXML
    public void initialize() {
        partidoDAO = new PartidoDAO();
        cmbLocalVisitante.setValue("LOCAL");
        configurarValidaciones();
    }

    private void configurarValidaciones() {
        // Validador para Fecha
        ValidationSupport vsFecha = new ValidationSupport();
        vsFecha.registerValidator(dpFechaPartido, (Control c, LocalDate fecha) -> {
            if (fecha == null) {
                return ValidationResult.fromWarning(c, "La fecha del partido es obligatoria");
            }
            return ValidationResult.fromInfo(c, "OK");
        });

        // Validador para Rival
        ValidationSupport vsRival = new ValidationSupport();
        vsRival.registerValidator(txtRival, (Control c, String texto) -> {
            if (texto == null || texto.trim().isEmpty()) {
                return ValidationResult.fromWarning(c, "El rival no debe estar vacío");
            } else if (texto.length() < 3 || texto.length() > 100) {
                return ValidationResult.fromError(c, "El rival debe tener entre 3 y 100 caracteres");
            } else {
                return ValidationResult.fromInfo(c, "OK");
            }
        });

        // Validador para Resultado (opcional pero con formato)
        ValidationSupport vsResultado = new ValidationSupport();
        vsResultado.registerValidator(txtResultado, false, (Control c, String texto) -> {
            if (texto != null && !texto.trim().isEmpty()) {
                // Formato esperado: X-Y
                if (!texto.matches("\\d+-\\d+")) {
                    return ValidationResult.fromError(c, "El formato debe ser X-Y (ej: 2-1)");
                }
            }
            return ValidationResult.fromInfo(c, "OK");
        });

        // Validador para Local/Visitante
        ValidationSupport vsLocal = new ValidationSupport();
        Validator<String> localValidator = Validator.createPredicateValidator(
                texto -> texto != null && !texto.trim().isEmpty(),
                "Debe seleccionar Local o Visitante"
        );
        vsLocal.registerValidator(cmbLocalVisitante, localValidator);

        // Registro de todos los validadores
        validadores = new ArrayList<>();
        validadores.addAll(Arrays.asList(vsFecha, vsRival, vsResultado, vsLocal));

        // Inicializar decoración en un nuevo hilo
        Platform.runLater(() -> {
            for (ValidationSupport validationSupport : validadores) {
                validationSupport.initInitialDecoration();
            }
        });
    }

    public void setPartidosController(PartidosController controller) {
        this.partidosController = controller;
    }

    public void setPartidoEditar(Partido partido) {
        this.partidoEditar = partido;
        if (partido != null) {
            dpFechaPartido.setValue(partido.getFechaPartido());
            txtRival.setText(partido.getRival());
            txtResultado.setText(partido.getResultado());
            cmbLocalVisitante.setValue(partido.getLocalVisitante());
            txtObservaciones.setText(partido.getObservaciones());
        }
    }

    @FXML
    private void guardar() {
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

        Partido partido = (partidoEditar != null) ? partidoEditar : new Partido();
        partido.setFechaPartido(dpFechaPartido.getValue());
        partido.setRival(txtRival.getText().trim());
        partido.setResultado(txtResultado.getText().trim());
        partido.setLocalVisitante(cmbLocalVisitante.getValue());
        partido.setObservaciones(txtObservaciones.getText().trim());

        boolean exito;
        if (partidoEditar != null) {
            exito = partidoDAO.actualizar(partido);
        } else {
            exito = partidoDAO.insertar(partido) > 0;
        }

        if (exito) {
            if (partidosController != null) {
                partidosController.cargarPartidos();
            }
            cerrarVentana();
        } else {
            lblError.setText("Error al guardar el partido");
        }
    }

    @FXML
    private void cancelar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtRival.getScene().getWindow();
        stage.close();
    }
}

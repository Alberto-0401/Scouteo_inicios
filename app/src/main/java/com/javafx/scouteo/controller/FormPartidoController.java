package com.javafx.scouteo.controller;

import com.javafx.scouteo.dao.PartidoDAO;
import com.javafx.scouteo.model.Equipo;
import com.javafx.scouteo.model.Partido;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class FormPartidoController {

    @FXML private DatePicker dpFechaPartido;
    @FXML private TextField txtRival;
    @FXML private TextField txtResultado;
    @FXML private ComboBox<String> cmbLocalVisitante;
    @FXML private TextArea txtObservaciones;
    @FXML private Label lblError;

    private PartidoDAO partidoDAO;
    private PartidosController partidosController;
    private Partido partidoEditar;
    private Equipo equipoActivo;

    @FXML
    public void initialize() {
        partidoDAO = new PartidoDAO();
        if (cmbLocalVisitante.getItems().isEmpty()) {
            cmbLocalVisitante.getItems().addAll("LOCAL", "VISITANTE", "NEUTRAL");
        } else if (!cmbLocalVisitante.getItems().contains("NEUTRAL")) {
            cmbLocalVisitante.getItems().add("NEUTRAL");
        }
        cmbLocalVisitante.setValue("LOCAL");
    }

    public void setPartidosController(PartidosController controller) {
        this.partidosController = controller;
    }

    public void setEquipoActivo(Equipo equipo) {
        this.equipoActivo = equipo;
    }

    public void setPartidoEditar(Partido partido) {
        this.partidoEditar = partido;
        if (partido != null) {
            if (partido.getFechaHora() != null) {
                dpFechaPartido.setValue(partido.getFechaHora().toLocalDate());
            }
            txtRival.setText(partido.getRival());
            if (partido.getGolesFavor() != null && partido.getGolesContra() != null) {
                txtResultado.setText(partido.getGolesFavor() + "-" + partido.getGolesContra());
            }
            if (partido.getTipo() != null) {
                cmbLocalVisitante.setValue(partido.getTipo().toUpperCase());
            }
            txtObservaciones.setText(partido.getNotas() != null ? partido.getNotas() : "");
        }
    }

    @FXML
    private void guardar() {
        lblError.setText("");

        LocalDate fecha = dpFechaPartido.getValue();
        String rival = txtRival.getText() != null ? txtRival.getText().trim() : "";

        if (fecha == null) {
            lblError.setText("La fecha del partido es obligatoria");
            return;
        }
        if (rival.isEmpty()) {
            lblError.setText("El nombre del rival es obligatorio");
            return;
        }

        Partido partido = (partidoEditar != null) ? partidoEditar : new Partido();
        partido.setFechaHora(fecha.atStartOfDay());
        partido.setRival(rival);
        partido.setTipo(cmbLocalVisitante.getValue() != null ? cmbLocalVisitante.getValue().toLowerCase() : "local");
        partido.setNotas(txtObservaciones.getText() != null ? txtObservaciones.getText().trim() : null);

        String resultado = txtResultado.getText() != null ? txtResultado.getText().trim() : "";
        if (!resultado.isEmpty()) {
            if (!resultado.matches("\\d+-\\d+")) {
                lblError.setText("El resultado debe tener formato X-Y (ej: 2-1)");
                return;
            }
            String[] partes = resultado.split("-");
            partido.setGolesFavor(Integer.parseInt(partes[0]));
            partido.setGolesContra(Integer.parseInt(partes[1]));
        }

        if (partido.getEquipoId() == null) {
            if (equipoActivo != null) {
                partido.setEquipoId(equipoActivo.getId());
            } else {
                lblError.setText("No hay equipo activo seleccionado");
                return;
            }
        }

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

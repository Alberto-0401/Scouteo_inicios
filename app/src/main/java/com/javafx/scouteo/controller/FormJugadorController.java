package com.javafx.scouteo.controller;

import com.javafx.scouteo.dao.JugadorDAO;
import com.javafx.scouteo.model.Jugador;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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

    @FXML
    public void initialize() {
        jugadorDAO = new JugadorDAO();
        // Los ComboBox ya están inicializados en el FXML
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

        // Validar campos obligatorios
        if (txtNombre.getText().trim().isEmpty()) {
            lblError.setText("El nombre es obligatorio");
            txtNombre.requestFocus();
            return;
        }

        if (txtApellidos.getText().trim().isEmpty()) {
            lblError.setText("Los apellidos son obligatorios");
            txtApellidos.requestFocus();
            return;
        }

        if (txtDorsal.getText().trim().isEmpty()) {
            lblError.setText("El dorsal es obligatorio");
            txtDorsal.requestFocus();
            return;
        }

        if (dpFechaNacimiento.getValue() == null) {
            lblError.setText("La fecha de nacimiento es obligatoria");
            dpFechaNacimiento.requestFocus();
            return;
        }

        if (cmbPosicion.getValue() == null) {
            lblError.setText("La posicion es obligatoria");
            cmbPosicion.requestFocus();
            return;
        }

        if (cmbCategoria.getValue() == null) {
            lblError.setText("La categoria es obligatoria");
            cmbCategoria.requestFocus();
            return;
        }

        // Validar dorsal numerico
        int dorsal;
        try {
            dorsal = Integer.parseInt(txtDorsal.getText().trim());
            if (dorsal < 1 || dorsal > 99) {
                lblError.setText("El dorsal debe estar entre 1 y 99");
                return;
            }
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
            try {
                jugador.setAltura(Double.parseDouble(txtAltura.getText().trim()));
            } catch (NumberFormatException e) {
                lblError.setText("La altura debe ser un numero valido");
                return;
            }
        }

        // Peso (opcional)
        if (!txtPeso.getText().trim().isEmpty()) {
            try {
                jugador.setPeso(Double.parseDouble(txtPeso.getText().trim()));
            } catch (NumberFormatException e) {
                lblError.setText("El peso debe ser un numero valido");
                return;
            }
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

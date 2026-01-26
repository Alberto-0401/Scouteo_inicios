package com.javafx.scouteo.controller;

import com.javafx.scouteo.dao.EstadisticaPartidoDAO;
import com.javafx.scouteo.dao.JugadorDAO;
import com.javafx.scouteo.dao.JugadorPartidoDAO;
import com.javafx.scouteo.model.EstadisticaPartido;
import com.javafx.scouteo.model.Jugador;
import com.javafx.scouteo.model.JugadorPartido;
import com.javafx.scouteo.model.Partido;
import com.javafx.scouteo.utils.StageUtils;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;

import java.util.List;

public class FormEstadisticasPartidoController {

    @FXML
    private Label lblTitulo;

    @FXML
    private ComboBox<Jugador> cmbJugadores;

    @FXML
    private Spinner<Integer> spnGoles;

    @FXML
    private Spinner<Integer> spnAsistencias;

    @FXML
    private Spinner<Integer> spnMinutos;

    @FXML
    private Spinner<Integer> spnParadas;

    @FXML
    private Spinner<Integer> spnRecuperaciones;

    @FXML
    private Spinner<Integer> spnTarjetasAmarillas;

    @FXML
    private Spinner<Integer> spnTarjetasRojas;

    @FXML
    private Spinner<Double> spnValoracion;

    @FXML
    private TextArea txtObservaciones;

    @FXML
    private Label lblError;

    private Partido partido;
    private JugadorDAO jugadorDAO;
    private EstadisticaPartidoDAO estadisticaDAO;
    private JugadorPartidoDAO jugadorPartidoDAO;

    @FXML
    public void initialize() {
        jugadorDAO = new JugadorDAO();
        estadisticaDAO = new EstadisticaPartidoDAO();
        jugadorPartidoDAO = new JugadorPartidoDAO();

        cargarJugadores();
        configurarSpinners();
    }

    public void setPartido(Partido partido) {
        this.partido = partido;
        lblTitulo.setText("AÑADIR ESTADÍSTICAS - " + partido.getRival());
    }

    private void cargarJugadores() {
        List<Jugador> jugadores = jugadorDAO.obtenerTodos();
        cmbJugadores.setItems(FXCollections.observableArrayList(jugadores));

        cmbJugadores.setCellFactory(param -> new ListCell<Jugador>() {
            @Override
            protected void updateItem(Jugador item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDorsal() + " - " + item.getNombre() + " " + item.getApellidos() + " (" + item.getPosicion() + ")");
                }
            }
        });

        cmbJugadores.setButtonCell(new ListCell<Jugador>() {
            @Override
            protected void updateItem(Jugador item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDorsal() + " - " + item.getNombre() + " " + item.getApellidos() + " (" + item.getPosicion() + ")");
                }
            }
        });
    }

    private void configurarSpinners() {
        spnGoles.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0));
        spnAsistencias.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0));
        spnMinutos.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 120, 0));
        spnParadas.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20, 0));
        spnRecuperaciones.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 50, 0));
        spnTarjetasAmarillas.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 2, 0));
        spnTarjetasRojas.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1, 0));
        spnValoracion.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 10.0, 5.0, 0.5));
    }

    @FXML
    private void guardar() {
        if (!validarCampos()) {
            return;
        }

        // 1. Crear estadística
        EstadisticaPartido estadistica = new EstadisticaPartido();
        estadistica.setGoles(spnGoles.getValue());
        estadistica.setAsistencias(spnAsistencias.getValue());
        estadistica.setParadas(spnParadas.getValue());
        estadistica.setRecuperaciones(spnRecuperaciones.getValue());
        estadistica.setTarjetasAmarillas(spnTarjetasAmarillas.getValue());
        estadistica.setTarjetasRojas(spnTarjetasRojas.getValue());
        estadistica.setMinutosJugados(spnMinutos.getValue());
        estadistica.setValoracion(spnValoracion.getValue());
        estadistica.setObservaciones(txtObservaciones.getText().trim());

        Integer idEstadistica = estadisticaDAO.insertar(estadistica);

        if (idEstadistica != null) {
            // 2. Crear relación jugador-partido-estadística
            JugadorPartido jugadorPartido = new JugadorPartido();
            jugadorPartido.setIdJugador(cmbJugadores.getValue().getId());
            jugadorPartido.setIdPartido(partido.getIdPartido());
            jugadorPartido.setIdEstadistica(idEstadistica);
            jugadorPartido.setTitular(spnMinutos.getValue() > 0);
            jugadorPartido.setConvocado(true);

            if (jugadorPartidoDAO.insertar(jugadorPartido)) {
                mostrarExito("Estadísticas guardadas correctamente");
                cerrarVentana();
            } else {
                lblError.setText("Error al vincular estadísticas con el jugador");
            }
        } else {
            lblError.setText("Error al guardar las estadísticas");
        }
    }

    @FXML
    private void cancelar() {
        cerrarVentana();
    }

    private boolean validarCampos() {
        if (cmbJugadores.getValue() == null) {
            lblError.setText("Debe seleccionar un jugador");
            cmbJugadores.requestFocus();
            return false;
        }

        lblError.setText("");
        return true;
    }

    private void mostrarExito(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        // Agregar icono a la ventana de alerta
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        StageUtils.setAppIcon(alertStage);

        alert.showAndWait();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) cmbJugadores.getScene().getWindow();
        stage.close();
    }
}

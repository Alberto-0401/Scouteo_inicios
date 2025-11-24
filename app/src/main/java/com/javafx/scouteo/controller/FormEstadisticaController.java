package com.javafx.scouteo.controller;

import com.javafx.scouteo.model.Jugador;
import com.javafx.scouteo.model.Partido;
import com.javafx.scouteo.model.EstadisticaPartido;
import com.javafx.scouteo.model.JugadorPartido;
import com.javafx.scouteo.dao.JugadorDAO;
import com.javafx.scouteo.dao.PartidoDAO;
import com.javafx.scouteo.dao.EstadisticaPartidoDAO;
import com.javafx.scouteo.dao.JugadorPartidoDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;

import java.util.List;

public class FormEstadisticaController {

    @FXML
    private ComboBox<String> cmbJugador;

    @FXML
    private DatePicker dpFecha;

    @FXML
    private HBox hboxParadas;

    @FXML
    private HBox hboxRecuperaciones;

    @FXML
    private Label lblError;

    @FXML
    private Label lblPosicionJugador;

    @FXML
    private Label lblValoracion;

    @FXML
    private TextField txtAmarillas;

    @FXML
    private TextField txtAsistencias;

    @FXML
    private TextField txtGoles;

    @FXML
    private TextField txtMinutos;

    @FXML
    private TextField txtParadas;

    @FXML
    private TextField txtRecuperaciones;

    @FXML
    private TextField txtResultado;

    @FXML
    private TextField txtRival;

    @FXML
    private TextField txtRojas;

    @FXML
    private TextField txtValoracion;

    @FXML
    private VBox vboxEspecificas;

    private JugadorDAO jugadorDAO;
    private PartidoDAO partidoDAO;
    private EstadisticaPartidoDAO estadisticaDAO;
    private JugadorPartidoDAO jugadorPartidoDAO;
    private List<Jugador> listaJugadores;

    // Para modo edicion
    private EstadisticaPartido estadisticaEditar;
    private int idEstadisticaEditar = -1;
    private Runnable onGuardado;

    @FXML
    public void initialize() {
        jugadorDAO = new JugadorDAO();
        partidoDAO = new PartidoDAO();
        estadisticaDAO = new EstadisticaPartidoDAO();
        jugadorPartidoDAO = new JugadorPartidoDAO();

        cargarJugadores();

        // Listener para cambiar campos segun posicion
        cmbJugador.valueProperty().addListener((obs, oldVal, newVal) -> {
            actualizarCamposPosicion(newVal);
        });

        // Listener para valoracion
        txtValoracion.textProperty().addListener((obs, oldVal, newVal) -> {
            actualizarLabelValoracion(newVal);
        });
    }

    private void cargarJugadores() {
        listaJugadores = jugadorDAO.obtenerTodos();
        cmbJugador.getItems().clear();
        for (Jugador j : listaJugadores) {
            cmbJugador.getItems().add(j.getDorsal() + " - " + j.getNombre() + " " + j.getApellidos());
        }
    }

    private void actualizarCamposPosicion(String seleccion) {
        if (seleccion == null) return;

        Jugador jugador = buscarJugadorPorSeleccion(seleccion);
        if (jugador == null) return;

        String posicion = jugador.getPosicion();
        lblPosicionJugador.setText("(" + posicion + ")");

        // Mostrar campos segun posicion
        boolean esPortero = "POR".equals(posicion);
        boolean esDefensaOMedio = "DEF".equals(posicion) || "MED".equals(posicion);

        hboxParadas.setVisible(esPortero);
        hboxParadas.setManaged(esPortero);
        hboxRecuperaciones.setVisible(esDefensaOMedio);
        hboxRecuperaciones.setManaged(esDefensaOMedio);
    }

    private void actualizarLabelValoracion(String valor) {
        try {
            double val = Double.parseDouble(valor);
            if (val >= 8) {
                lblValoracion.setText("Excelente");
            } else if (val >= 6) {
                lblValoracion.setText("Bueno");
            } else if (val >= 4) {
                lblValoracion.setText("Regular");
            } else {
                lblValoracion.setText("Bajo");
            }
        } catch (NumberFormatException e) {
            lblValoracion.setText("Sin valorar");
        }
    }

    private Jugador buscarJugadorPorSeleccion(String seleccion) {
        for (Jugador j : listaJugadores) {
            String texto = j.getDorsal() + " - " + j.getNombre() + " " + j.getApellidos();
            if (texto.equals(seleccion)) {
                return j;
            }
        }
        return null;
    }

    /**
     * Configura el formulario para editar una estadistica existente
     */
    public void setEstadisticaEditar(int idEstadistica, Jugador jugador, Partido partido) {
        this.idEstadisticaEditar = idEstadistica;
        this.estadisticaEditar = estadisticaDAO.obtenerPorId(idEstadistica);

        if (estadisticaEditar != null && jugador != null && partido != null) {
            // Seleccionar jugador
            String textoJugador = jugador.getDorsal() + " - " + jugador.getNombre() + " " + jugador.getApellidos();
            cmbJugador.setValue(textoJugador);
            cmbJugador.setDisable(true); // No permitir cambiar jugador al editar

            // Cargar datos del partido
            dpFecha.setValue(partido.getFechaPartido());
            dpFecha.setDisable(true);
            txtRival.setText(partido.getRival());
            txtRival.setDisable(true);
            txtResultado.setText(partido.getResultado());
            txtResultado.setDisable(true);

            // Cargar estadisticas
            txtMinutos.setText(String.valueOf(estadisticaEditar.getMinutosJugados()));
            txtGoles.setText(String.valueOf(estadisticaEditar.getGoles() != null ? estadisticaEditar.getGoles() : 0));
            txtAsistencias.setText(String.valueOf(estadisticaEditar.getAsistencias() != null ? estadisticaEditar.getAsistencias() : 0));
            txtAmarillas.setText(String.valueOf(estadisticaEditar.getTarjetasAmarillas() != null ? estadisticaEditar.getTarjetasAmarillas() : 0));
            txtRojas.setText(String.valueOf(estadisticaEditar.getTarjetasRojas() != null ? estadisticaEditar.getTarjetasRojas() : 0));
            txtValoracion.setText(String.valueOf(estadisticaEditar.getValoracion()));

            if (estadisticaEditar.getParadas() != null) {
                txtParadas.setText(String.valueOf(estadisticaEditar.getParadas()));
            }
            if (estadisticaEditar.getRecuperaciones() != null) {
                txtRecuperaciones.setText(String.valueOf(estadisticaEditar.getRecuperaciones()));
            }
        }
    }

    public void setOnGuardado(Runnable callback) {
        this.onGuardado = callback;
    }

    @FXML
    void cancelar(ActionEvent event) {
        cerrarVentana();
    }

    @FXML
    void guardar(ActionEvent event) {
        lblError.setText("");

        // Validaciones
        if (cmbJugador.getValue() == null) {
            lblError.setText("Selecciona un jugador");
            return;
        }

        int minutos;
        try {
            minutos = Integer.parseInt(txtMinutos.getText().trim());
            if (minutos < 0 || minutos > 120) {
                lblError.setText("Minutos debe estar entre 0 y 120");
                return;
            }
        } catch (NumberFormatException e) {
            lblError.setText("Minutos debe ser un numero valido");
            return;
        }

        double valoracion;
        try {
            valoracion = Double.parseDouble(txtValoracion.getText().trim());
            if (valoracion < 0 || valoracion > 10) {
                lblError.setText("Valoracion debe estar entre 0 y 10");
                return;
            }
        } catch (NumberFormatException e) {
            lblError.setText("Valoracion debe ser un numero valido");
            return;
        }

        // Crear o actualizar estadistica
        EstadisticaPartido est = (estadisticaEditar != null) ? estadisticaEditar : new EstadisticaPartido();

        est.setMinutosJugados(minutos);
        est.setValoracion(valoracion);

        try {
            est.setGoles(Integer.parseInt(txtGoles.getText().trim()));
        } catch (NumberFormatException e) {
            est.setGoles(0);
        }

        try {
            est.setAsistencias(Integer.parseInt(txtAsistencias.getText().trim()));
        } catch (NumberFormatException e) {
            est.setAsistencias(0);
        }

        try {
            est.setTarjetasAmarillas(Integer.parseInt(txtAmarillas.getText().trim()));
        } catch (NumberFormatException e) {
            est.setTarjetasAmarillas(0);
        }

        try {
            est.setTarjetasRojas(Integer.parseInt(txtRojas.getText().trim()));
        } catch (NumberFormatException e) {
            est.setTarjetasRojas(0);
        }

        if (!txtParadas.getText().trim().isEmpty()) {
            try {
                est.setParadas(Integer.parseInt(txtParadas.getText().trim()));
            } catch (NumberFormatException e) {
                est.setParadas(null);
            }
        }

        if (!txtRecuperaciones.getText().trim().isEmpty()) {
            try {
                est.setRecuperaciones(Integer.parseInt(txtRecuperaciones.getText().trim()));
            } catch (NumberFormatException e) {
                est.setRecuperaciones(null);
            }
        }

        boolean exito;
        if (idEstadisticaEditar > 0) {
            // Modo edicion
            est.setIdEstadistica(idEstadisticaEditar);
            exito = estadisticaDAO.actualizar(est);
        } else {
            // Modo nuevo - necesita crear partido y relacion
            // Por ahora solo funciona edicion
            lblError.setText("Para crear nuevas estadisticas usa Convocatorias");
            return;
        }

        if (exito) {
            if (onGuardado != null) {
                onGuardado.run();
            }
            cerrarVentana();
        } else {
            lblError.setText("Error al guardar");
        }
    }

    private void cerrarVentana() {
        Stage stage = (Stage) cmbJugador.getScene().getWindow();
        stage.close();
    }
}

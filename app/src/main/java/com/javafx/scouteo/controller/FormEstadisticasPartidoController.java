package com.javafx.scouteo.controller;

import com.javafx.scouteo.dao.EventoPartidoDAO;
import com.javafx.scouteo.dao.JugadorDAO;
import com.javafx.scouteo.dao.JugadorPartidoDAO;
import com.javafx.scouteo.model.EstadisticaPartido;
import com.javafx.scouteo.model.EventoPartido;
import com.javafx.scouteo.model.Jugador;
import com.javafx.scouteo.model.JugadorPartido;
import com.javafx.scouteo.model.Partido;
import com.javafx.scouteo.utils.StageUtils;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Callback;

import java.util.List;

public class FormEstadisticasPartidoController {

    // --- Tab: Estadísticas Jugador ---
    @FXML private Label lblTitulo;
    @FXML private ComboBox<Jugador> cmbJugadores;
    @FXML private Spinner<Integer> spnGoles;
    @FXML private Spinner<Integer> spnAsistencias;
    @FXML private Spinner<Integer> spnMinutos;
    @FXML private Spinner<Integer> spnParadas;
    @FXML private Spinner<Integer> spnRecuperaciones;
    @FXML private Spinner<Integer> spnTarjetasAmarillas;
    @FXML private Spinner<Integer> spnTarjetasRojas;
    @FXML private Spinner<Double> spnValoracion;
    @FXML private TextArea txtObservaciones;
    @FXML private Label lblError;

    // --- Tab: Eventos del Partido ---
    @FXML private Spinner<Integer> spnMinutoEvento;
    @FXML private ComboBox<String> cmbTipoEvento;
    @FXML private ComboBox<Jugador> cmbJugadorEvento;
    @FXML private ComboBox<Jugador> cmbJugador2Evento;
    @FXML private TextField txtDescripcionEvento;
    @FXML private Label lblErrorEvento;
    @FXML private TableView<EventoPartido> tablaEventos;
    @FXML private TableColumn<EventoPartido, Integer> colMinutoEvento;
    @FXML private TableColumn<EventoPartido, String> colTipoEvento;
    @FXML private TableColumn<EventoPartido, String> colJugadorEvento;
    @FXML private TableColumn<EventoPartido, String> colJugador2Evento;
    @FXML private TableColumn<EventoPartido, String> colDescripcionEvento;
    @FXML private TableColumn<EventoPartido, Void> colAccionesEvento;

    private Partido partido;
    private JugadorDAO jugadorDAO;
    private JugadorPartidoDAO jugadorPartidoDAO;
    private EventoPartidoDAO eventoPartidoDAO;
    private List<Jugador> listaJugadores;

    private static final List<String> TIPOS_EVENTO = List.of(
        "gol", "gol_penal", "gol_propio", "tarjeta_amarilla", "tarjeta_roja",
        "doble_amarilla", "cambio", "lesion", "penalti_fallado", "otro"
    );

    @FXML
    public void initialize() {
        jugadorDAO = new JugadorDAO();
        jugadorPartidoDAO = new JugadorPartidoDAO();
        eventoPartidoDAO = new EventoPartidoDAO();

        int clubId = com.javafx.scouteo.util.SesionUsuario.getInstance().getUsuarioActual().getClubId();
        listaJugadores = jugadorDAO.obtenerPorClub(clubId);

        cargarJugadores();
        configurarSpinners();
        configurarTablaEventos();
        configurarFormEvento();
    }

    public void setPartido(Partido partido) {
        this.partido = partido;
        lblTitulo.setText("ESTADÍSTICAS DEL PARTIDO - " + partido.getRival());
        cargarEventos();
    }

    // ─── Tab 1: Estadísticas Jugador ───────────────────────────────────────────

    private void cargarJugadores() {
        Callback<ListView<Jugador>, ListCell<Jugador>> cellFactory = param -> new ListCell<>() {
            @Override
            protected void updateItem(Jugador item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null :
                    item.getDorsal() + " - " + item.getNombre() + " " + item.getApellidos() + " (" + item.getPosicion() + ")");
            }
        };

        ObservableList<Jugador> jugadores = FXCollections.observableArrayList(listaJugadores);
        cmbJugadores.setItems(jugadores);
        cmbJugadores.setCellFactory(cellFactory);
        cmbJugadores.setButtonCell(cellFactory.call(null));
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
        if (!validarCampos()) return;

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

        JugadorPartido jugadorPartido = new JugadorPartido();
        jugadorPartido.setIdJugador(cmbJugadores.getValue().getId());
        jugadorPartido.setIdPartido(partido.getId());
        jugadorPartido.setTitular(spnMinutos.getValue() > 0);
        jugadorPartido.setConvocado(true);

        if (jugadorPartidoDAO.insertarConEstadistica(jugadorPartido, estadistica)) {
            mostrarExito("Estadísticas guardadas correctamente");
            cerrarVentana();
        } else {
            lblError.setText("Error al guardar las estadísticas");
        }
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

    // ─── Tab 2: Eventos del Partido ────────────────────────────────────────────

    private void configurarFormEvento() {
        spnMinutoEvento.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 120, 1));

        cmbTipoEvento.setItems(FXCollections.observableArrayList(TIPOS_EVENTO));
        cmbTipoEvento.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : tipoDisplay(item));
            }
        });
        cmbTipoEvento.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : tipoDisplay(item));
            }
        });

        Callback<ListView<Jugador>, ListCell<Jugador>> jugadorCell = param -> new ListCell<>() {
            @Override
            protected void updateItem(Jugador item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null :
                    item.getDorsal() + " - " + item.getNombre() + " " + item.getApellidos());
            }
        };

        // Add a blank option at top for optional selection
        ObservableList<Jugador> jugadoresOpcional = FXCollections.observableArrayList();
        jugadoresOpcional.add(null);
        jugadoresOpcional.addAll(listaJugadores);

        cmbJugadorEvento.setItems(jugadoresOpcional);
        cmbJugadorEvento.setCellFactory(jugadorCell);
        cmbJugadorEvento.setButtonCell(jugadorCell.call(null));

        cmbJugador2Evento.setItems(FXCollections.observableArrayList(jugadoresOpcional));
        cmbJugador2Evento.setCellFactory(jugadorCell);
        cmbJugador2Evento.setButtonCell(jugadorCell.call(null));
    }

    private void configurarTablaEventos() {
        colMinutoEvento.setCellValueFactory(new PropertyValueFactory<>("minuto"));
        colMinutoEvento.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item + "'");
            }
        });

        colTipoEvento.setCellValueFactory(new PropertyValueFactory<>("tipoDisplay"));

        colJugadorEvento.setCellValueFactory(new PropertyValueFactory<>("jugadorNombre"));
        colJugador2Evento.setCellValueFactory(new PropertyValueFactory<>("jugador2Nombre"));
        colDescripcionEvento.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        colAccionesEvento.setCellFactory(col -> new TableCell<>() {
            private final Button btnEliminar = new Button("Eliminar");
            {
                btnEliminar.setOnAction(e -> {
                    EventoPartido ev = getTableView().getItems().get(getIndex());
                    if (eventoPartidoDAO.eliminar(ev.getId())) {
                        cargarEventos();
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnEliminar);
            }
        });
    }

    private void cargarEventos() {
        if (partido == null) return;
        List<EventoPartido> eventos = eventoPartidoDAO.obtenerPorPartido(partido.getId());
        tablaEventos.setItems(FXCollections.observableArrayList(eventos));
    }

    @FXML
    private void añadirEvento() {
        if (cmbTipoEvento.getValue() == null) {
            lblErrorEvento.setText("Debe seleccionar el tipo de evento");
            return;
        }

        EventoPartido ev = new EventoPartido();
        ev.setPartidoId(partido.getId());
        ev.setMinuto(spnMinutoEvento.getValue());
        ev.setTipo(cmbTipoEvento.getValue());

        Jugador j1 = cmbJugadorEvento.getValue();
        ev.setJugadorId(j1 != null ? j1.getId() : null);

        Jugador j2 = cmbJugador2Evento.getValue();
        ev.setJugador2Id(j2 != null ? j2.getId() : null);

        ev.setDescripcion(txtDescripcionEvento.getText().trim().isEmpty() ? null : txtDescripcionEvento.getText().trim());

        if (eventoPartidoDAO.insertar(ev)) {
            lblErrorEvento.setText("");
            txtDescripcionEvento.clear();
            cmbTipoEvento.setValue(null);
            cmbJugadorEvento.setValue(null);
            cmbJugador2Evento.setValue(null);
            spnMinutoEvento.getValueFactory().setValue(1);
            cargarEventos();
        } else {
            lblErrorEvento.setText("Error al guardar el evento");
        }
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private String tipoDisplay(String tipo) {
        return switch (tipo) {
            case "gol" -> "Gol";
            case "gol_penal" -> "Gol de Penal";
            case "gol_propio" -> "Gol en Propia";
            case "tarjeta_amarilla" -> "Tarjeta Amarilla";
            case "tarjeta_roja" -> "Tarjeta Roja";
            case "doble_amarilla" -> "Doble Amarilla";
            case "cambio" -> "Cambio";
            case "lesion" -> "Lesion";
            case "penalti_fallado" -> "Penalti Fallado";
            default -> "Otro";
        };
    }

    @FXML
    private void cancelar() {
        cerrarVentana();
    }

    private void mostrarExito(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Exito");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        StageUtils.setAppIcon(alertStage);
        alert.showAndWait();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) lblTitulo.getScene().getWindow();
        stage.close();
    }
}

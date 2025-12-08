package com.javafx.scouteo.controller;

import com.javafx.scouteo.model.Partido;
import com.javafx.scouteo.model.Jugador;
import com.javafx.scouteo.model.JugadorPartido;
import com.javafx.scouteo.model.EstadisticaPartido;
import com.javafx.scouteo.dao.PartidoDAO;
import com.javafx.scouteo.dao.JugadorDAO;
import com.javafx.scouteo.dao.JugadorPartidoDAO;
import com.javafx.scouteo.util.ConexionBD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ConvocatoriasController {

    @FXML
    private TableView<ConvocatoriaItem> tablaConvocatorias;

    @FXML
    private TableColumn<ConvocatoriaItem, String> colPartido;

    @FXML
    private TableColumn<ConvocatoriaItem, LocalDate> colFecha;

    @FXML
    private TableColumn<ConvocatoriaItem, String> colJugador;

    @FXML
    private TableColumn<ConvocatoriaItem, Integer> colDorsal;

    @FXML
    private TableColumn<ConvocatoriaItem, String> colPosicion;

    @FXML
    private TableColumn<ConvocatoriaItem, String> colTitular;

    @FXML
    private TableColumn<ConvocatoriaItem, String> colConvocado;

    @FXML
    private TableColumn<ConvocatoriaItem, Void> colAcciones;

    @FXML
    private ComboBox<String> cmbPartido;

    @FXML
    private Label lblTotal;

    private ObservableList<ConvocatoriaItem> listaConvocatorias;
    private PartidoDAO partidoDAO;
    private JugadorDAO jugadorDAO;
    private JugadorPartidoDAO jugadorPartidoDAO;
    private List<Partido> listaPartidos;

    @FXML
    public void initialize() {
        partidoDAO = new PartidoDAO();
        jugadorDAO = new JugadorDAO();
        jugadorPartidoDAO = new JugadorPartidoDAO();

        configurarTabla();
        cargarPartidosCombo();
        cargarConvocatorias(null);
    }

    private void configurarTabla() {
        colPartido.setCellValueFactory(new PropertyValueFactory<>("partido"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colJugador.setCellValueFactory(new PropertyValueFactory<>("jugador"));
        colDorsal.setCellValueFactory(new PropertyValueFactory<>("dorsal"));
        colPosicion.setCellValueFactory(new PropertyValueFactory<>("posicion"));
        colConvocado.setCellValueFactory(new PropertyValueFactory<>("convocado"));
        colTitular.setCellValueFactory(new PropertyValueFactory<>("titular"));

        // Columna de acciones
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("\u270E");   // ✎ Editar
            private final Button btnEliminar = new Button("\u2716"); // ✖ Eliminar
            private final HBox contenedor = new HBox(5, btnEditar, btnEliminar);

            {
                // Configurar tamaños mínimos para que se vean los iconos
                btnEditar.setMinWidth(40);
                btnEditar.setPrefWidth(40);
                btnEliminar.setMinWidth(40);
                btnEliminar.setPrefWidth(40);

                btnEditar.setOnAction(event -> {
                    ConvocatoriaItem item = getTableView().getItems().get(getIndex());
                    editarConvocatoria(item);
                });

                btnEliminar.setOnAction(event -> {
                    ConvocatoriaItem item = getTableView().getItems().get(getIndex());
                    eliminarConvocatoria(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(contenedor);
                }
            }
        });
    }

    private void cargarPartidosCombo() {
        listaPartidos = partidoDAO.obtenerTodos();
        cmbPartido.getItems().clear();
        cmbPartido.getItems().add("Todos los partidos");

        for (Partido p : listaPartidos) {
            cmbPartido.getItems().add(p.getFechaPartido() + " - " + p.getRival());
        }
    }

    private void cargarConvocatorias(Integer idPartidoFiltro) {
        listaConvocatorias = FXCollections.observableArrayList();

        String sql = "SELECT p.id_partido, p.rival, p.fecha_partido, " +
                     "j.id_jugador, CONCAT(j.nombre, ' ', j.apellidos) as jugador, " +
                     "j.dorsal, j.posicion, " +
                     "jp.titular, jp.convocado " +
                     "FROM jugadores_partidos jp " +
                     "INNER JOIN partidos p ON jp.id_partido = p.id_partido " +
                     "INNER JOIN jugadores j ON jp.id_jugador = j.id_jugador " +
                     (idPartidoFiltro != null ? "WHERE p.id_partido = ? " : "") +
                     "ORDER BY p.fecha_partido DESC, j.dorsal";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (idPartidoFiltro != null) {
                pstmt.setInt(1, idPartidoFiltro);
            }

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ConvocatoriaItem item = new ConvocatoriaItem(
                    rs.getInt("id_partido"),
                    rs.getInt("id_jugador"),
                    rs.getString("rival"),
                    rs.getDate("fecha_partido").toLocalDate(),
                    rs.getString("jugador"),
                    rs.getInt("dorsal"),
                    rs.getString("posicion"),
                    rs.getBoolean("titular"),
                    rs.getBoolean("convocado")
                );
                listaConvocatorias.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        tablaConvocatorias.setItems(listaConvocatorias);
        lblTotal.setText("Total: " + listaConvocatorias.size() + " convocatorias");
    }

    @FXML
    private void filtrar() {
        String seleccion = cmbPartido.getValue();
        if (seleccion == null || seleccion.equals("Todos los partidos")) {
            cargarConvocatorias(null);
        } else {
            // Buscar el partido seleccionado
            for (Partido p : listaPartidos) {
                String texto = p.getFechaPartido() + " - " + p.getRival();
                if (texto.equals(seleccion)) {
                    cargarConvocatorias(p.getIdPartido());
                    break;
                }
            }
        }
    }

    @FXML
    private void limpiarFiltro() {
        cmbPartido.setValue("Todos los partidos");
        cargarConvocatorias(null);
    }

    @FXML
    private void nuevaConvocatoria() {
        // Dialogo para seleccionar partido y jugador
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nueva Convocatoria");
        dialog.setHeaderText("Añadir jugador a partido");

        // Campos del dialogo
        ComboBox<String> cmbPartidoDialog = new ComboBox<>();
        ComboBox<String> cmbJugadorDialog = new ComboBox<>();
        CheckBox chkConvocado = new CheckBox("Convocado");
        CheckBox chkTitular = new CheckBox("Titular");

        chkConvocado.setSelected(true);
        chkTitular.setSelected(true);

        // Cargar partidos
        for (Partido p : listaPartidos) {
            cmbPartidoDialog.getItems().add(p.getFechaPartido() + " - " + p.getRival());
        }

        // Cargar jugadores
        List<Jugador> jugadores = jugadorDAO.obtenerTodos();
        for (Jugador j : jugadores) {
            cmbJugadorDialog.getItems().add(j.getDorsal() + " - " + j.getNombre() + " " + j.getApellidos());
        }

        // Layout
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Partido:"), 0, 0);
        grid.add(cmbPartidoDialog, 1, 0);
        grid.add(new Label("Jugador:"), 0, 1);
        grid.add(cmbJugadorDialog, 1, 1);
        grid.add(chkConvocado, 0, 2);
        grid.add(chkTitular, 1, 2);

        cmbPartidoDialog.setPrefWidth(250);
        cmbJugadorDialog.setPrefWidth(250);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            String partidoSel = cmbPartidoDialog.getValue();
            String jugadorSel = cmbJugadorDialog.getValue();

            if (partidoSel == null || jugadorSel == null) {
                mostrarError("Debes seleccionar un partido y un jugador");
                return;
            }

            // Buscar IDs
            Partido partidoObj = null;
            Jugador jugadorObj = null;

            for (Partido p : listaPartidos) {
                if ((p.getFechaPartido() + " - " + p.getRival()).equals(partidoSel)) {
                    partidoObj = p;
                    break;
                }
            }

            for (Jugador j : jugadores) {
                if ((j.getDorsal() + " - " + j.getNombre() + " " + j.getApellidos()).equals(jugadorSel)) {
                    jugadorObj = j;
                    break;
                }
            }

            if (partidoObj == null || jugadorObj == null) {
                mostrarError("Error al obtener partido o jugador");
                return;
            }

            // Verificar si ya existe
            if (jugadorPartidoDAO.existeRelacion(jugadorObj.getId(), partidoObj.getIdPartido())) {
                mostrarError("Este jugador ya esta convocado para este partido");
                return;
            }

            // Crear convocatoria con estadisticas vacias
            JugadorPartido jp = new JugadorPartido();
            jp.setIdJugador(jugadorObj.getId());
            jp.setIdPartido(partidoObj.getIdPartido());
            jp.setConvocado(chkConvocado.isSelected());
            jp.setTitular(chkTitular.isSelected());

            EstadisticaPartido est = new EstadisticaPartido();
            est.setGoles(0);
            est.setAsistencias(0);
            est.setTarjetasAmarillas(0);
            est.setTarjetasRojas(0);
            est.setMinutosJugados(0);
            est.setValoracion(5.0);

            if (jugadorPartidoDAO.insertarConEstadistica(jp, est)) {
                cargarConvocatorias(null);
                mostrarInfo("Convocatoria añadida correctamente");
            } else {
                mostrarError("Error al añadir convocatoria");
            }
        }
    }

    private void editarConvocatoria(ConvocatoriaItem item) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Editar Convocatoria");
        dialog.setHeaderText(item.getJugador() + " vs " + item.getPartido());

        CheckBox chkConvocado = new CheckBox("Convocado");
        CheckBox chkTitular = new CheckBox("Titular");

        chkConvocado.setSelected(item.isConvocadoBool());
        chkTitular.setSelected(item.isTitularBool());

        javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(10, chkConvocado, chkTitular);
        vbox.setStyle("-fx-padding: 20;");

        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            JugadorPartido jp = new JugadorPartido();
            jp.setIdJugador(item.getIdJugador());
            jp.setIdPartido(item.getIdPartido());
            jp.setConvocado(chkConvocado.isSelected());
            jp.setTitular(chkTitular.isSelected());

            if (jugadorPartidoDAO.actualizar(jp)) {
                cargarConvocatorias(null);
            } else {
                mostrarError("Error al actualizar convocatoria");
            }
        }
    }

    private void eliminarConvocatoria(ConvocatoriaItem item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminacion");
        alert.setHeaderText("Eliminar convocatoria");
        alert.setContentText("¿Eliminar a " + item.getJugador() + " del partido contra " + item.getPartido() + "?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (jugadorPartidoDAO.eliminar(item.getIdJugador(), item.getIdPartido())) {
                cargarConvocatorias(null);
            } else {
                mostrarError("Error al eliminar convocatoria");
            }
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Clase interna para representar items de convocatorias
    public static class ConvocatoriaItem {
        private int idPartido;
        private int idJugador;
        private String partido;
        private LocalDate fecha;
        private String jugador;
        private Integer dorsal;
        private String posicion;
        private boolean titularBool;
        private boolean convocadoBool;

        public ConvocatoriaItem(int idPartido, int idJugador, String partido, LocalDate fecha,
                               String jugador, Integer dorsal, String posicion,
                               boolean titular, boolean convocado) {
            this.idPartido = idPartido;
            this.idJugador = idJugador;
            this.partido = partido;
            this.fecha = fecha;
            this.jugador = jugador;
            this.dorsal = dorsal;
            this.posicion = posicion;
            this.titularBool = titular;
            this.convocadoBool = convocado;
        }

        public int getIdPartido() { return idPartido; }
        public int getIdJugador() { return idJugador; }
        public String getPartido() { return partido; }
        public LocalDate getFecha() { return fecha; }
        public String getJugador() { return jugador; }
        public Integer getDorsal() { return dorsal; }
        public String getPosicion() { return posicion; }
        public String getTitular() { return titularBool ? "Si" : "No"; }
        public String getConvocado() { return convocadoBool ? "Si" : "No"; }
        public boolean isTitularBool() { return titularBool; }
        public boolean isConvocadoBool() { return convocadoBool; }
    }
}

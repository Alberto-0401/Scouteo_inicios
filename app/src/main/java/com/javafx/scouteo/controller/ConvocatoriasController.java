package com.javafx.scouteo.controller;

import com.javafx.scouteo.model.Equipo;
import com.javafx.scouteo.model.Partido;
import com.javafx.scouteo.model.Jugador;
import com.javafx.scouteo.dao.PartidoDAO;
import com.javafx.scouteo.dao.JugadorDAO;
import com.javafx.scouteo.util.ConexionBD;
import com.javafx.scouteo.utils.StageUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class ConvocatoriasController {

    @FXML private TableView<ConvocatoriaItem> tablaConvocatorias;
    @FXML private TableColumn<ConvocatoriaItem, String>    colPartido;
    @FXML private TableColumn<ConvocatoriaItem, LocalDate> colFecha;
    @FXML private TableColumn<ConvocatoriaItem, String>    colJugador;
    @FXML private TableColumn<ConvocatoriaItem, Integer>   colDorsal;
    @FXML private TableColumn<ConvocatoriaItem, String>    colPosicion;
    @FXML private TableColumn<ConvocatoriaItem, String>    colTitular;
    @FXML private TableColumn<ConvocatoriaItem, String>    colConvocado;
    @FXML private TableColumn<ConvocatoriaItem, Void>      colAcciones;
    @FXML private ComboBox<String> cmbPartido;
    @FXML private Label lblTotal;

    private ObservableList<ConvocatoriaItem> listaConvocatorias;
    private final PartidoDAO partidoDAO = new PartidoDAO();
    private final JugadorDAO jugadorDAO = new JugadorDAO();
    private List<Partido> listaPartidos;
    private Equipo equipoActivo;

    public void setEquipoActivo(Equipo equipo) {
        this.equipoActivo = equipo;
        cargarPartidosCombo();
        cargarConvocatorias(null);
    }

    @FXML
    public void initialize() {
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

        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar   = new Button("\u270E");
            private final Button btnEliminar = new Button("\u2716");
            private final HBox contenedor    = new HBox(5, btnEditar, btnEliminar);
            {
                btnEditar.setOnAction(e -> editarConvocatoria(getTableView().getItems().get(getIndex())));
                btnEliminar.setOnAction(e -> eliminarConvocatoria(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : contenedor);
            }
        });
    }

    private void cargarPartidosCombo() {
        int clubId = com.javafx.scouteo.util.SesionUsuario.getInstance().getUsuarioActual().getClubId();
        listaPartidos = (equipoActivo != null)
                ? partidoDAO.obtenerPorEquipo(equipoActivo.getId())
                : partidoDAO.obtenerPorClub(clubId);
        cmbPartido.getItems().clear();
        cmbPartido.getItems().add("Todos los partidos");
        for (Partido p : listaPartidos) cmbPartido.getItems().add(textoPartido(p));
        cmbPartido.setValue("Todos los partidos");
    }

    private void cargarConvocatorias(Integer idPartidoFiltro) {
        listaConvocatorias = FXCollections.observableArrayList();

        if (!ConexionBD.isConexionValida()) {
            tablaConvocatorias.setPlaceholder(new Label("Sin conexion a la base de datos"));
            tablaConvocatorias.setItems(listaConvocatorias);
            lblTotal.setText("Total: 0 convocatorias");
            return;
        }

        int clubId = com.javafx.scouteo.util.SesionUsuario.getInstance().getUsuarioActual().getClubId();

        String sql = "SELECT c.id, c.partido_id, c.jugador_id, c.tipo, c.motivo_baja, " +
                     "p.rival, p.fecha_hora, " +
                     "j.dorsal, j.posicion, CONCAT(j.nombre, ' ', j.apellidos) AS jugador " +
                     "FROM convocatorias c " +
                     "INNER JOIN partidos p ON c.partido_id = p.id " +
                     "INNER JOIN equipos e ON p.equipo_id = e.id " +
                     "INNER JOIN jugadores j ON c.jugador_id = j.id " +
                     "WHERE e.club_id = ? " +
                     (idPartidoFiltro != null ? "AND c.partido_id = ? " : "") +
                     "ORDER BY p.fecha_hora DESC, j.dorsal";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, clubId);
            if (idPartidoFiltro != null) pstmt.setInt(2, idPartidoFiltro);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("fecha_hora");
                listaConvocatorias.add(new ConvocatoriaItem(
                    rs.getInt("id"),
                    rs.getInt("partido_id"),
                    rs.getInt("jugador_id"),
                    rs.getString("rival"),
                    ts != null ? ts.toLocalDateTime().toLocalDate() : null,
                    rs.getString("jugador"),
                    rs.getInt("dorsal"),
                    rs.getString("posicion"),
                    rs.getString("tipo"),
                    rs.getString("motivo_baja")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tablaConvocatorias.setItems(listaConvocatorias);
        tablaConvocatorias.setPlaceholder(new Label(
                listaConvocatorias.isEmpty() ? "No hay convocatorias registradas" : ""));
        lblTotal.setText("Total: " + listaConvocatorias.size() + " convocatorias");
    }

    @FXML private void filtrar() {
        String sel = cmbPartido.getValue();
        if (sel == null || sel.equals("Todos los partidos")) {
            cargarConvocatorias(null);
        } else {
            for (Partido p : listaPartidos) {
                if (textoPartido(p).equals(sel)) { cargarConvocatorias(p.getId()); break; }
            }
        }
    }

    @FXML private void limpiarFiltro() {
        cmbPartido.setValue("Todos los partidos");
        cargarConvocatorias(null);
    }

    @FXML
    private void nuevaConvocatoria() {
        ComboBox<String> cmbPartidoD = new ComboBox<>();
        ComboBox<String> cmbJugadorD = new ComboBox<>();
        ComboBox<String> cmbTipo     = new ComboBox<>();
        cmbTipo.getItems().addAll("titular", "suplente", "no_convocado");
        cmbTipo.setValue("titular");

        for (Partido p : listaPartidos) cmbPartidoD.getItems().add(textoPartido(p));
        List<Jugador> jugadores = (equipoActivo != null)
                ? jugadorDAO.obtenerActivosPorEquipo(equipoActivo.getId())
                : jugadorDAO.obtenerPorClub(com.javafx.scouteo.util.SesionUsuario.getInstance().getUsuarioActual().getClubId());
        for (Jugador j : jugadores)
            cmbJugadorD.getItems().add(j.getDorsal() + " - " + j.getNombre() + " " + j.getApellidos());

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setStyle("-fx-padding: 20;");
        grid.add(new Label("Partido:"), 0, 0); grid.add(cmbPartidoD, 1, 0);
        grid.add(new Label("Jugador:"), 0, 1); grid.add(cmbJugadorD, 1, 1);
        grid.add(new Label("Tipo:"),    0, 2); grid.add(cmbTipo,     1, 2);
        cmbPartidoD.setPrefWidth(250); cmbJugadorD.setPrefWidth(250);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nueva Convocatoria");
        dialog.setHeaderText("Anadir jugador a partido");
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/scouteo.css").toExternalForm());
        dialog.setOnShowing(e -> StageUtils.setAppIcon((Stage) dialog.getDialogPane().getScene().getWindow()));

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String selP = cmbPartidoD.getValue();
            String selJ = cmbJugadorD.getValue();
            if (selP == null || selJ == null) { mostrarError("Selecciona partido y jugador"); return; }

            Partido partido = listaPartidos.stream()
                    .filter(p -> textoPartido(p).equals(selP)).findFirst().orElse(null);
            Jugador jugador = jugadores.stream()
                    .filter(j -> (j.getDorsal() + " - " + j.getNombre() + " " + j.getApellidos()).equals(selJ))
                    .findFirst().orElse(null);

            if (partido == null || jugador == null) { mostrarError("Error al obtener datos"); return; }
            if (existeConvocatoria(jugador.getId(), partido.getId())) {
                mostrarError("Este jugador ya esta convocado para este partido"); return;
            }
            if (insertarConvocatoria(partido.getId(), jugador.getId(), cmbTipo.getValue())) {
                cargarConvocatorias(null);
                mostrarInfo("Convocatoria anadida correctamente");
            } else {
                mostrarError("Error al anadir convocatoria");
            }
        }
    }

    private void editarConvocatoria(ConvocatoriaItem item) {
        ComboBox<String> cmbTipo = new ComboBox<>();
        cmbTipo.getItems().addAll("titular", "suplente", "no_convocado");
        cmbTipo.setValue(item.getTipo());

        javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(10,
                new Label("Tipo de convocatoria:"), cmbTipo);
        vbox.setStyle("-fx-padding: 20;");

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Editar Convocatoria");
        dialog.setHeaderText(item.getJugador() + " — " + item.getPartido());
        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/scouteo.css").toExternalForm());
        dialog.setOnShowing(e -> StageUtils.setAppIcon((Stage) dialog.getDialogPane().getScene().getWindow()));

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String sql = "UPDATE convocatorias SET tipo = ? WHERE id = ?";
            try (Connection conn = ConexionBD.getConexion();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, cmbTipo.getValue());
                pstmt.setInt(2, item.getId());
                if (pstmt.executeUpdate() > 0) cargarConvocatorias(null);
                else mostrarError("Error al actualizar");
            } catch (SQLException e) {
                e.printStackTrace();
                mostrarError("Error al actualizar convocatoria");
            }
        }
    }

    private void eliminarConvocatoria(ConvocatoriaItem item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminacion");
        alert.setContentText("Eliminar a " + item.getJugador() + " del partido contra " + item.getPartido() + "?");
        alert.setOnShowing(e -> StageUtils.setAppIcon((Stage) alert.getDialogPane().getScene().getWindow()));

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String sql = "DELETE FROM convocatorias WHERE id = ?";
            try (Connection conn = ConexionBD.getConexion();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, item.getId());
                if (pstmt.executeUpdate() > 0) cargarConvocatorias(null);
                else mostrarError("Error al eliminar");
            } catch (SQLException e) {
                e.printStackTrace();
                mostrarError("Error al eliminar convocatoria");
            }
        }
    }

    // ==================== Helpers ====================

    private boolean existeConvocatoria(int jugadorId, int partidoId) {
        String sql = "SELECT COUNT(*) FROM convocatorias WHERE jugador_id = ? AND partido_id = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, jugadorId); pstmt.setInt(2, partidoId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private boolean insertarConvocatoria(int partidoId, int jugadorId, String tipo) {
        String sql = "INSERT INTO convocatorias (partido_id, jugador_id, tipo) VALUES (?, ?, ?)";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, partidoId); pstmt.setInt(2, jugadorId); pstmt.setString(3, tipo);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private String textoPartido(Partido p) {
        String fecha = p.getFechaHora() != null ? p.getFechaHora().toLocalDate().toString() : "?";
        return fecha + " - " + p.getRival();
    }

    private void mostrarError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error"); a.setContentText(msg);
        a.setOnShowing(e -> StageUtils.setAppIcon((Stage) a.getDialogPane().getScene().getWindow()));
        a.showAndWait();
    }

    private void mostrarInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Info"); a.setContentText(msg);
        a.setOnShowing(e -> StageUtils.setAppIcon((Stage) a.getDialogPane().getScene().getWindow()));
        a.showAndWait();
    }

    // ==================== Inner class ====================

    public static class ConvocatoriaItem {
        private final int id, partidoId, jugadorId;
        private final String partido, jugador, posicion, tipo, motivoBaja;
        private final Integer dorsal;
        private final LocalDate fecha;

        public ConvocatoriaItem(int id, int partidoId, int jugadorId,
                                String partido, LocalDate fecha, String jugador,
                                Integer dorsal, String posicion, String tipo, String motivoBaja) {
            this.id = id; this.partidoId = partidoId; this.jugadorId = jugadorId;
            this.partido = partido; this.fecha = fecha; this.jugador = jugador;
            this.dorsal = dorsal; this.posicion = posicion;
            this.tipo = tipo != null ? tipo : "suplente";
            this.motivoBaja = motivoBaja;
        }

        public int    getId()         { return id; }
        public int    getIdPartido()  { return partidoId; }
        public int    getIdJugador()  { return jugadorId; }
        public String getPartido()    { return partido; }
        public LocalDate getFecha()   { return fecha; }
        public String getJugador()    { return jugador; }
        public Integer getDorsal()    { return dorsal; }
        public String getPosicion()   { return posicion; }
        public String getTipo()       { return tipo; }
        public String getMotivoBaja() { return motivoBaja; }

        public String getConvocado() { return "no_convocado".equals(tipo) ? "No" : "Si"; }
        public String getTitular()   { return "titular".equals(tipo) ? "Si" : "No"; }
        public boolean isTitularBool()   { return "titular".equals(tipo); }
        public boolean isConvocadoBool() { return !"no_convocado".equals(tipo); }
    }
}

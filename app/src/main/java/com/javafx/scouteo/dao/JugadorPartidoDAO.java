package com.javafx.scouteo.dao;

import com.javafx.scouteo.model.JugadorPartido;
import com.javafx.scouteo.model.Jugador;
import com.javafx.scouteo.model.Partido;
import com.javafx.scouteo.model.EstadisticaPartido;
import com.javafx.scouteo.util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JugadorPartidoDAO {

    // ==================== CREATE ====================

    /**
     * Inserta una nueva relación jugador-partido con sus estadísticas
     * Este método crea primero la estadística y luego la relación
     * @param jugadorPartido La relación a insertar
     * @param estadistica Las estadísticas del jugador en el partido
     * @return true si se insertó correctamente
     */
    public boolean insertarConEstadistica(JugadorPartido jugadorPartido, EstadisticaPartido estadistica) {
        Connection conn = null;
        try {
            conn = ConexionBD.getConexion();
            conn.setAutoCommit(false);

            // 1. Insertar estadística
            String sqlEstadistica = "INSERT INTO estadisticas_partido (goles, asistencias, paradas, " +
                    "recuperaciones, tarjetas_amarillas, tarjetas_rojas, minutos_jugados, valoracion, observaciones) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            int idEstadistica;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlEstadistica, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, estadistica.getGoles() != null ? estadistica.getGoles() : 0);
                pstmt.setInt(2, estadistica.getAsistencias() != null ? estadistica.getAsistencias() : 0);

                if (estadistica.getParadas() != null) {
                    pstmt.setInt(3, estadistica.getParadas());
                } else {
                    pstmt.setNull(3, Types.INTEGER);
                }

                if (estadistica.getRecuperaciones() != null) {
                    pstmt.setInt(4, estadistica.getRecuperaciones());
                } else {
                    pstmt.setNull(4, Types.INTEGER);
                }

                pstmt.setInt(5, estadistica.getTarjetasAmarillas() != null ? estadistica.getTarjetasAmarillas() : 0);
                pstmt.setInt(6, estadistica.getTarjetasRojas() != null ? estadistica.getTarjetasRojas() : 0);
                pstmt.setInt(7, estadistica.getMinutosJugados());
                pstmt.setDouble(8, estadistica.getValoracion());
                pstmt.setString(9, estadistica.getObservaciones());

                pstmt.executeUpdate();
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    idEstadistica = rs.getInt(1);
                } else {
                    conn.rollback();
                    return false;
                }
            }

            // 2. Insertar relación jugador-partido
            String sqlRelacion = "INSERT INTO jugadores_partidos (id_jugador, id_partido, id_estadistica, " +
                    "convocado, titular) VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(sqlRelacion)) {
                pstmt.setInt(1, jugadorPartido.getIdJugador());
                pstmt.setInt(2, jugadorPartido.getIdPartido());
                pstmt.setInt(3, idEstadistica);
                pstmt.setBoolean(4, jugadorPartido.getConvocado() != null ? jugadorPartido.getConvocado() : true);
                pstmt.setBoolean(5, jugadorPartido.getTitular() != null ? jugadorPartido.getTitular() : true);

                pstmt.executeUpdate();
            }

            conn.commit();
            jugadorPartido.setIdEstadistica(idEstadistica);
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Inserta una relación jugador-partido con un ID de estadística existente
     * @param jugadorPartido La relación a insertar (debe tener idEstadistica)
     * @return true si se insertó correctamente
     */
    public boolean insertar(JugadorPartido jugadorPartido) {
        String sql = "INSERT INTO jugadores_partidos (id_jugador, id_partido, id_estadistica, convocado, titular) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, jugadorPartido.getIdJugador());
            pstmt.setInt(2, jugadorPartido.getIdPartido());
            pstmt.setInt(3, jugadorPartido.getIdEstadistica());
            pstmt.setBoolean(4, jugadorPartido.getConvocado() != null ? jugadorPartido.getConvocado() : true);
            pstmt.setBoolean(5, jugadorPartido.getTitular() != null ? jugadorPartido.getTitular() : true);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ==================== READ ====================

    /**
     * Obtiene todas las participaciones de un jugador en partidos
     * @param idJugador ID del jugador
     * @return Lista de participaciones
     */
    public List<JugadorPartido> obtenerPorJugador(int idJugador) {
        List<JugadorPartido> participaciones = new ArrayList<>();
        String sql = "SELECT * FROM jugadores_partidos WHERE id_jugador = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idJugador);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                participaciones.add(mapearJugadorPartido(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return participaciones;
    }

    /**
     * Obtiene todos los jugadores que participaron en un partido
     * @param idPartido ID del partido
     * @return Lista de participaciones
     */
    public List<JugadorPartido> obtenerPorPartido(int idPartido) {
        List<JugadorPartido> participaciones = new ArrayList<>();
        String sql = "SELECT * FROM jugadores_partidos WHERE id_partido = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idPartido);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                participaciones.add(mapearJugadorPartido(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return participaciones;
    }

    /**
     * Obtiene una participación específica
     * @param idJugador ID del jugador
     * @param idPartido ID del partido
     * @return La participación o null si no existe
     */
    public JugadorPartido obtenerPorJugadorYPartido(int idJugador, int idPartido) {
        String sql = "SELECT * FROM jugadores_partidos WHERE id_jugador = ? AND id_partido = ?";
        JugadorPartido participacion = null;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idJugador);
            pstmt.setInt(2, idPartido);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                participacion = mapearJugadorPartido(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return participacion;
    }

    /**
     * Obtiene todas las participaciones
     * @return Lista de todas las participaciones
     */
    public List<JugadorPartido> obtenerTodos() {
        List<JugadorPartido> participaciones = new ArrayList<>();
        String sql = "SELECT * FROM jugadores_partidos";

        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                participaciones.add(mapearJugadorPartido(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return participaciones;
    }

    /**
     * Cuenta los partidos jugados por un jugador
     * @param idJugador ID del jugador
     * @return Número de partidos jugados
     */
    public int contarPartidosJugador(int idJugador) {
        String sql = "SELECT COUNT(*) as total FROM jugadores_partidos WHERE id_jugador = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idJugador);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Cuenta los jugadores en un partido
     * @param idPartido ID del partido
     * @return Número de jugadores
     */
    public int contarJugadoresPartido(int idPartido) {
        String sql = "SELECT COUNT(*) as total FROM jugadores_partidos WHERE id_partido = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idPartido);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    // ==================== UPDATE ====================

    /**
     * Actualiza una participación (convocado/titular)
     * @param jugadorPartido La participación con los datos actualizados
     * @return true si se actualizó correctamente
     */
    public boolean actualizar(JugadorPartido jugadorPartido) {
        String sql = "UPDATE jugadores_partidos SET convocado = ?, titular = ? " +
                     "WHERE id_jugador = ? AND id_partido = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, jugadorPartido.getConvocado() != null ? jugadorPartido.getConvocado() : true);
            pstmt.setBoolean(2, jugadorPartido.getTitular() != null ? jugadorPartido.getTitular() : true);
            pstmt.setInt(3, jugadorPartido.getIdJugador());
            pstmt.setInt(4, jugadorPartido.getIdPartido());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ==================== DELETE ====================

    /**
     * Elimina una participación y su estadística asociada
     * @param idJugador ID del jugador
     * @param idPartido ID del partido
     * @return true si se eliminó correctamente
     */
    public boolean eliminar(int idJugador, int idPartido) {
        Connection conn = null;
        try {
            conn = ConexionBD.getConexion();
            conn.setAutoCommit(false);

            // Obtener el ID de estadística antes de eliminar
            int idEstadistica = -1;
            String sqlSelect = "SELECT id_estadistica FROM jugadores_partidos WHERE id_jugador = ? AND id_partido = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlSelect)) {
                pstmt.setInt(1, idJugador);
                pstmt.setInt(2, idPartido);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    idEstadistica = rs.getInt("id_estadistica");
                }
            }

            // Eliminar la relación (la FK con CASCADE eliminará la estadística)
            String sqlDelete = "DELETE FROM jugadores_partidos WHERE id_jugador = ? AND id_partido = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlDelete)) {
                pstmt.setInt(1, idJugador);
                pstmt.setInt(2, idPartido);
                pstmt.executeUpdate();
            }

            // Eliminar la estadística si existe (por si CASCADE no está configurado)
            if (idEstadistica > 0) {
                String sqlDeleteEst = "DELETE FROM estadisticas_partido WHERE id_estadistica = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlDeleteEst)) {
                    pstmt.setInt(1, idEstadistica);
                    pstmt.executeUpdate();
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Elimina todas las participaciones de un partido
     * @param idPartido ID del partido
     * @return true si se eliminaron correctamente
     */
    public boolean eliminarPorPartido(int idPartido) {
        // Las FK con CASCADE eliminarán las estadísticas
        String sql = "DELETE FROM jugadores_partidos WHERE id_partido = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idPartido);
            return pstmt.executeUpdate() >= 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ==================== VALIDACIONES ====================

    /**
     * Verifica si ya existe una relación jugador-partido
     * @param idJugador ID del jugador
     * @param idPartido ID del partido
     * @return true si ya existe la relación
     */
    public boolean existeRelacion(int idJugador, int idPartido) {
        String sql = "SELECT COUNT(*) FROM jugadores_partidos WHERE id_jugador = ? AND id_partido = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idJugador);
            pstmt.setInt(2, idPartido);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Valida los datos de una participación antes de insertar
     * @param jugadorPartido La participación a validar
     * @return Lista de errores de validación (vacía si todo es válido)
     */
    public List<String> validar(JugadorPartido jugadorPartido) {
        List<String> errores = new ArrayList<>();

        // ID de jugador obligatorio
        if (jugadorPartido.getIdJugador() == null) {
            errores.add("El jugador es obligatorio");
        }

        // ID de partido obligatorio
        if (jugadorPartido.getIdPartido() == null) {
            errores.add("El partido es obligatorio");
        }

        // Verificar que el jugador existe
        if (jugadorPartido.getIdJugador() != null) {
            JugadorDAO jugadorDAO = new JugadorDAO();
            if (jugadorDAO.obtenerPorId(jugadorPartido.getIdJugador()) == null) {
                errores.add("El jugador especificado no existe");
            }
        }

        // Verificar que el partido existe
        if (jugadorPartido.getIdPartido() != null) {
            PartidoDAO partidoDAO = new PartidoDAO();
            if (partidoDAO.obtenerPorId(jugadorPartido.getIdPartido()) == null) {
                errores.add("El partido especificado no existe");
            }
        }

        // Verificar que no existe ya la relación
        if (jugadorPartido.getIdJugador() != null && jugadorPartido.getIdPartido() != null) {
            if (existeRelacion(jugadorPartido.getIdJugador(), jugadorPartido.getIdPartido())) {
                errores.add("El jugador ya está registrado en este partido");
            }
        }

        return errores;
    }

    // ==================== HELPERS ====================

    /**
     * Mapea un ResultSet a un objeto JugadorPartido
     */
    private JugadorPartido mapearJugadorPartido(ResultSet rs) throws SQLException {
        JugadorPartido jp = new JugadorPartido();
        jp.setIdJugador(rs.getInt("id_jugador"));
        jp.setIdPartido(rs.getInt("id_partido"));
        jp.setIdEstadistica(rs.getInt("id_estadistica"));
        jp.setConvocado(rs.getBoolean("convocado"));
        jp.setTitular(rs.getBoolean("titular"));
        return jp;
    }
}

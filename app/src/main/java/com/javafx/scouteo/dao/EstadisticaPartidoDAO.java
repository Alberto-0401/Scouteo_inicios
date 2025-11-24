package com.javafx.scouteo.dao;

import com.javafx.scouteo.model.EstadisticaPartido;
import com.javafx.scouteo.util.ConexionBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EstadisticaPartidoDAO {

    // ==================== CREATE ====================

    /**
     * Inserta una nueva estadística de partido
     * @param estadistica La estadística a insertar
     * @return El ID generado, o -1 si falla
     */
    public int insertar(EstadisticaPartido estadistica) {
        String sql = "INSERT INTO estadisticas_partido (goles, asistencias, paradas, recuperaciones, " +
                     "tarjetas_amarillas, tarjetas_rojas, minutos_jugados, valoracion, observaciones) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

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

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    // ==================== READ ====================

    public List<EstadisticaPartido> obtenerTodos() {
        List<EstadisticaPartido> estadisticas = new ArrayList<>();
        String sql = "SELECT * FROM estadisticas_partido";

        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                estadisticas.add(mapearEstadistica(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return estadisticas;
    }

    public EstadisticaPartido obtenerPorId(int idEstadistica) {
        String sql = "SELECT * FROM estadisticas_partido WHERE id_estadistica = ?";
        EstadisticaPartido estadistica = null;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idEstadistica);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                estadistica = mapearEstadistica(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return estadistica;
    }

    public int contarTotalGoles() {
        String sql = "SELECT SUM(goles) as total_goles FROM estadisticas_partido";
        int total = 0;

        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                total = rs.getInt("total_goles");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return total;
    }

    public int contarTotalAsistencias() {
        String sql = "SELECT SUM(asistencias) as total FROM estadisticas_partido";
        int total = 0;

        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                total = rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return total;
    }

    public double obtenerValoracionPromedio() {
        String sql = "SELECT AVG(valoracion) as promedio FROM estadisticas_partido";
        double promedio = 0.0;

        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                promedio = rs.getDouble("promedio");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return promedio;
    }

    // ==================== UPDATE ====================

    /**
     * Actualiza una estadística existente
     * @param estadistica La estadística con los datos actualizados
     * @return true si se actualizó correctamente
     */
    public boolean actualizar(EstadisticaPartido estadistica) {
        String sql = "UPDATE estadisticas_partido SET goles = ?, asistencias = ?, paradas = ?, " +
                     "recuperaciones = ?, tarjetas_amarillas = ?, tarjetas_rojas = ?, " +
                     "minutos_jugados = ?, valoracion = ?, observaciones = ? WHERE id_estadistica = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

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
            pstmt.setInt(10, estadistica.getIdEstadistica());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ==================== DELETE ====================

    /**
     * Elimina una estadística por su ID
     * @param id El ID de la estadística a eliminar
     * @return true si se eliminó correctamente
     */
    public boolean eliminar(int id) {
        String sql = "DELETE FROM estadisticas_partido WHERE id_estadistica = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ==================== VALIDACIONES ====================

    /**
     * Valida los datos de una estadística antes de insertar/actualizar
     * @param estadistica La estadística a validar
     * @param esPorPorcion true si el jugador es portero (para validar paradas)
     * @return Lista de errores de validación (vacía si todo es válido)
     */
    public List<String> validar(EstadisticaPartido estadistica, String posicionJugador) {
        List<String> errores = new ArrayList<>();

        // Goles (no negativos)
        if (estadistica.getGoles() != null && estadistica.getGoles() < 0) {
            errores.add("Los goles no pueden ser negativos");
        }

        // Asistencias (no negativas)
        if (estadistica.getAsistencias() != null && estadistica.getAsistencias() < 0) {
            errores.add("Las asistencias no pueden ser negativas");
        }

        // Paradas (solo para porteros, no negativas)
        if (estadistica.getParadas() != null) {
            if (estadistica.getParadas() < 0) {
                errores.add("Las paradas no pueden ser negativas");
            }
            if (posicionJugador != null && !posicionJugador.equals("POR")) {
                errores.add("Solo los porteros pueden tener paradas registradas");
            }
        }

        // Recuperaciones (no negativas)
        if (estadistica.getRecuperaciones() != null && estadistica.getRecuperaciones() < 0) {
            errores.add("Las recuperaciones no pueden ser negativas");
        }

        // Tarjetas amarillas (0-2)
        if (estadistica.getTarjetasAmarillas() != null &&
            (estadistica.getTarjetasAmarillas() < 0 || estadistica.getTarjetasAmarillas() > 2)) {
            errores.add("Las tarjetas amarillas deben estar entre 0 y 2");
        }

        // Tarjetas rojas (0-1)
        if (estadistica.getTarjetasRojas() != null &&
            (estadistica.getTarjetasRojas() < 0 || estadistica.getTarjetasRojas() > 1)) {
            errores.add("Las tarjetas rojas deben ser 0 o 1");
        }

        // Minutos jugados obligatorio (0-120)
        if (estadistica.getMinutosJugados() == null) {
            errores.add("Los minutos jugados son obligatorios");
        } else if (estadistica.getMinutosJugados() < 0 || estadistica.getMinutosJugados() > 120) {
            errores.add("Los minutos jugados deben estar entre 0 y 120");
        }

        // Valoración obligatoria (0.0-10.0)
        if (estadistica.getValoracion() == null) {
            errores.add("La valoración es obligatoria");
        } else if (estadistica.getValoracion() < 0.0 || estadistica.getValoracion() > 10.0) {
            errores.add("La valoración debe estar entre 0.0 y 10.0");
        }

        return errores;
    }

    // ==================== HELPERS ====================

    /**
     * Mapea un ResultSet a un objeto EstadisticaPartido
     */
    private EstadisticaPartido mapearEstadistica(ResultSet rs) throws SQLException {
        EstadisticaPartido estadistica = new EstadisticaPartido();
        estadistica.setIdEstadistica(rs.getInt("id_estadistica"));
        estadistica.setGoles(rs.getInt("goles"));
        estadistica.setAsistencias(rs.getInt("asistencias"));

        int paradas = rs.getInt("paradas");
        if (!rs.wasNull()) {
            estadistica.setParadas(paradas);
        }

        int recuperaciones = rs.getInt("recuperaciones");
        if (!rs.wasNull()) {
            estadistica.setRecuperaciones(recuperaciones);
        }

        estadistica.setTarjetasAmarillas(rs.getInt("tarjetas_amarillas"));
        estadistica.setTarjetasRojas(rs.getInt("tarjetas_rojas"));
        estadistica.setMinutosJugados(rs.getInt("minutos_jugados"));
        estadistica.setValoracion(rs.getDouble("valoracion"));
        estadistica.setObservaciones(rs.getString("observaciones"));

        return estadistica;
    }
}

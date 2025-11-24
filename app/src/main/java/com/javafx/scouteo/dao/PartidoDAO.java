package com.javafx.scouteo.dao;

import com.javafx.scouteo.model.Partido;
import com.javafx.scouteo.util.ConexionBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class PartidoDAO {

    // ==================== CREATE ====================

    /**
     * Inserta un nuevo partido en la base de datos
     * @param partido El partido a insertar
     * @return El ID generado del partido, o -1 si falla
     */
    public int insertar(Partido partido) {
        String sql = "INSERT INTO partidos (fecha_partido, rival, resultado, local_visitante, observaciones) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setDate(1, Date.valueOf(partido.getFechaPartido()));
            pstmt.setString(2, partido.getRival());
            pstmt.setString(3, partido.getResultado());
            pstmt.setString(4, partido.getLocalVisitante());
            pstmt.setString(5, partido.getObservaciones());

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

    public List<Partido> obtenerTodos() {
        List<Partido> partidos = new ArrayList<>();
        String sql = "SELECT * FROM partidos ORDER BY fecha_partido DESC";

        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                partidos.add(mapearPartido(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return partidos;
    }

    public Partido obtenerPorId(int id) {
        String sql = "SELECT * FROM partidos WHERE id_partido = ?";
        Partido partido = null;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                partido = mapearPartido(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return partido;
    }

    public List<Partido> obtenerPorRival(String rival) {
        List<Partido> partidos = new ArrayList<>();
        String sql = "SELECT * FROM partidos WHERE rival LIKE ? ORDER BY fecha_partido DESC";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + rival + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                partidos.add(mapearPartido(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return partidos;
    }

    public List<Partido> obtenerPorLocalVisitante(String tipo) {
        List<Partido> partidos = new ArrayList<>();
        String sql = "SELECT * FROM partidos WHERE local_visitante = ? ORDER BY fecha_partido DESC";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tipo);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                partidos.add(mapearPartido(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return partidos;
    }

    public List<Partido> obtenerPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Partido> partidos = new ArrayList<>();
        String sql = "SELECT * FROM partidos WHERE fecha_partido BETWEEN ? AND ? ORDER BY fecha_partido DESC";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(fechaInicio));
            pstmt.setDate(2, Date.valueOf(fechaFin));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                partidos.add(mapearPartido(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return partidos;
    }

    public int contarTotal() {
        String sql = "SELECT COUNT(*) as total FROM partidos";
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

    public int contarVictorias() {
        String sql = "SELECT COUNT(*) as total FROM partidos WHERE " +
                     "((local_visitante = 'LOCAL' AND CAST(SUBSTRING_INDEX(resultado, '-', 1) AS UNSIGNED) > CAST(SUBSTRING_INDEX(resultado, '-', -1) AS UNSIGNED)) OR " +
                     "(local_visitante = 'VISITANTE' AND CAST(SUBSTRING_INDEX(resultado, '-', -1) AS UNSIGNED) > CAST(SUBSTRING_INDEX(resultado, '-', 1) AS UNSIGNED)))";

        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

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
     * Actualiza un partido existente
     * @param partido El partido con los datos actualizados
     * @return true si se actualizó correctamente
     */
    public boolean actualizar(Partido partido) {
        String sql = "UPDATE partidos SET fecha_partido = ?, rival = ?, resultado = ?, " +
                     "local_visitante = ?, observaciones = ? WHERE id_partido = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(partido.getFechaPartido()));
            pstmt.setString(2, partido.getRival());
            pstmt.setString(3, partido.getResultado());
            pstmt.setString(4, partido.getLocalVisitante());
            pstmt.setString(5, partido.getObservaciones());
            pstmt.setInt(6, partido.getIdPartido());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ==================== DELETE ====================

    /**
     * Elimina un partido por su ID
     * @param id El ID del partido a eliminar
     * @return true si se eliminó correctamente
     */
    public boolean eliminar(int id) {
        String sql = "DELETE FROM partidos WHERE id_partido = ?";

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
     * Verifica si ya existe un partido con la misma fecha y rival
     * @param fecha La fecha del partido
     * @param rival El rival
     * @param idPartidoExcluir ID del partido a excluir (para updates), o null
     * @return true si el partido ya existe
     */
    public boolean existePartido(LocalDate fecha, String rival, Integer idPartidoExcluir) {
        String sql = idPartidoExcluir != null
            ? "SELECT COUNT(*) FROM partidos WHERE fecha_partido = ? AND rival = ? AND id_partido != ?"
            : "SELECT COUNT(*) FROM partidos WHERE fecha_partido = ? AND rival = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(fecha));
            pstmt.setString(2, rival);
            if (idPartidoExcluir != null) {
                pstmt.setInt(3, idPartidoExcluir);
            }

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
     * Valida los datos de un partido antes de insertar/actualizar
     * @param partido El partido a validar
     * @return Lista de errores de validación (vacía si todo es válido)
     */
    public List<String> validar(Partido partido) {
        List<String> errores = new ArrayList<>();

        // Fecha obligatoria
        if (partido.getFechaPartido() == null) {
            errores.add("La fecha del partido es obligatoria");
        } else if (partido.getFechaPartido().isAfter(LocalDate.now())) {
            errores.add("La fecha del partido no puede ser futura");
        }

        // Rival obligatorio
        if (partido.getRival() == null || partido.getRival().trim().isEmpty()) {
            errores.add("El rival es obligatorio");
        } else if (partido.getRival().length() > 100) {
            errores.add("El nombre del rival no puede exceder 100 caracteres");
        }

        // Resultado obligatorio y con formato válido
        if (partido.getResultado() == null || partido.getResultado().trim().isEmpty()) {
            errores.add("El resultado es obligatorio");
        } else if (!partido.getResultado().matches("^\\d{1,2}-\\d{1,2}$")) {
            errores.add("El resultado debe tener el formato X-Y (ej: 2-1)");
        }

        // Local/Visitante obligatorio y válido
        if (partido.getLocalVisitante() == null || partido.getLocalVisitante().trim().isEmpty()) {
            errores.add("Debe indicar si es LOCAL o VISITANTE");
        } else {
            String tipo = partido.getLocalVisitante().toUpperCase();
            if (!tipo.equals("LOCAL") && !tipo.equals("VISITANTE")) {
                errores.add("El tipo debe ser LOCAL o VISITANTE");
            }
        }

        // Observaciones opcionales pero con límite
        if (partido.getObservaciones() != null && partido.getObservaciones().length() > 500) {
            errores.add("Las observaciones no pueden exceder 500 caracteres");
        }

        return errores;
    }

    // ==================== HELPERS ====================

    /**
     * Mapea un ResultSet a un objeto Partido
     */
    private Partido mapearPartido(ResultSet rs) throws SQLException {
        Partido partido = new Partido();
        partido.setIdPartido(rs.getInt("id_partido"));
        partido.setFechaPartido(rs.getDate("fecha_partido").toLocalDate());
        partido.setRival(rs.getString("rival"));
        partido.setResultado(rs.getString("resultado"));
        partido.setLocalVisitante(rs.getString("local_visitante"));
        partido.setObservaciones(rs.getString("observaciones"));
        return partido;
    }
}

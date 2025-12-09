package com.javafx.scouteo.dao;

import com.javafx.scouteo.model.Jugador;
import com.javafx.scouteo.util.ConexionBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.Period;

public class JugadorDAO {

    // ==================== CREATE ====================

    /**
     * Inserta un nuevo jugador en la base de datos
     * @param jugador El jugador a insertar
     * @return El ID generado del jugador, o -1 si falla
     */
    public int insertar(Jugador jugador) {
        String sql = "INSERT INTO jugadores (nombre, apellidos, fecha_nacimiento, dorsal, posicion, " +
                     "altura, peso, categoria, foto, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, jugador.getNombre());
            pstmt.setString(2, jugador.getApellidos());
            pstmt.setDate(3, Date.valueOf(jugador.getFechaNacimiento()));
            pstmt.setInt(4, jugador.getDorsal());
            pstmt.setString(5, jugador.getPosicion());

            if (jugador.getAltura() != null) {
                pstmt.setDouble(6, jugador.getAltura());
            } else {
                pstmt.setNull(6, Types.DECIMAL);
            }

            if (jugador.getPeso() != null) {
                pstmt.setDouble(7, jugador.getPeso());
            } else {
                pstmt.setNull(7, Types.DECIMAL);
            }

            pstmt.setString(8, jugador.getCategoria());

            if (jugador.getFoto() != null) {
                pstmt.setBytes(9, jugador.getFoto());
            } else {
                pstmt.setNull(9, Types.BLOB);
            }

            pstmt.setString(10, jugador.getEstado() != null ? jugador.getEstado() : "Activo");

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

    public List<Jugador> obtenerTodos() {
        List<Jugador> jugadores = new ArrayList<>();
        String sql = "SELECT * FROM jugadores ORDER BY dorsal";

        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                jugadores.add(mapearJugador(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return jugadores;
    }

    public Jugador obtenerPorId(int id) {
        String sql = "SELECT * FROM jugadores WHERE id_jugador = ?";
        Jugador jugador = null;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                jugador = mapearJugador(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return jugador;
    }

    public Jugador obtenerPorDorsal(int dorsal) {
        String sql = "SELECT * FROM jugadores WHERE dorsal = ?";
        Jugador jugador = null;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, dorsal);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                jugador = mapearJugador(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return jugador;
    }

    public List<Jugador> obtenerPorCategoria(String categoria) {
        List<Jugador> jugadores = new ArrayList<>();
        String sql = "SELECT * FROM jugadores WHERE categoria = ? ORDER BY dorsal";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, categoria);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                jugadores.add(mapearJugador(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return jugadores;
    }

    public List<Jugador> obtenerPorPosicion(String posicion) {
        List<Jugador> jugadores = new ArrayList<>();
        String sql = "SELECT * FROM jugadores WHERE posicion = ? ORDER BY dorsal";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, posicion);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                jugadores.add(mapearJugador(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return jugadores;
    }

    public int contarTotal() {
        String sql = "SELECT COUNT(*) as total FROM jugadores";
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

    /**
     * Obtiene la distribución de jugadores por posición
     * @return Mapa con posición como clave y cantidad como valor
     */
    public java.util.Map<String, Integer> obtenerDistribucionPorPosicion() {
        java.util.Map<String, Integer> distribucion = new java.util.HashMap<>();
        String sql = "SELECT posicion, COUNT(*) as cantidad FROM jugadores GROUP BY posicion";

        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String posicion = rs.getString("posicion");
                int cantidad = rs.getInt("cantidad");
                distribucion.put(posicion, cantidad);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return distribucion;
    }

    // ==================== UPDATE ====================

    /**
     * Actualiza un jugador existente
     * @param jugador El jugador con los datos actualizados
     * @return true si se actualizó correctamente
     */
    public boolean actualizar(Jugador jugador) {
        String sql = "UPDATE jugadores SET nombre = ?, apellidos = ?, fecha_nacimiento = ?, " +
                     "dorsal = ?, posicion = ?, altura = ?, peso = ?, categoria = ?, " +
                     "foto = ?, estado = ? WHERE id_jugador = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, jugador.getNombre());
            pstmt.setString(2, jugador.getApellidos());
            pstmt.setDate(3, Date.valueOf(jugador.getFechaNacimiento()));
            pstmt.setInt(4, jugador.getDorsal());
            pstmt.setString(5, jugador.getPosicion());

            if (jugador.getAltura() != null) {
                pstmt.setDouble(6, jugador.getAltura());
            } else {
                pstmt.setNull(6, Types.DECIMAL);
            }

            if (jugador.getPeso() != null) {
                pstmt.setDouble(7, jugador.getPeso());
            } else {
                pstmt.setNull(7, Types.DECIMAL);
            }

            pstmt.setString(8, jugador.getCategoria());

            if (jugador.getFoto() != null) {
                pstmt.setBytes(9, jugador.getFoto());
            } else {
                pstmt.setNull(9, Types.BLOB);
            }

            pstmt.setString(10, jugador.getEstado());
            pstmt.setInt(11, jugador.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ==================== DELETE ====================

    /**
     * Elimina un jugador por su ID
     * @param id El ID del jugador a eliminar
     * @return true si se eliminó correctamente
     */
    public boolean eliminar(int id) {
        String sql = "DELETE FROM jugadores WHERE id_jugador = ?";

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
     * Verifica si un dorsal ya está en uso por otro jugador
     * @param dorsal El dorsal a verificar
     * @param idJugadorExcluir ID del jugador a excluir (para updates), o null
     * @return true si el dorsal está disponible
     */
    public boolean dorsalDisponible(int dorsal, Integer idJugadorExcluir) {
        String sql = idJugadorExcluir != null
            ? "SELECT COUNT(*) FROM jugadores WHERE dorsal = ? AND id_jugador != ?"
            : "SELECT COUNT(*) FROM jugadores WHERE dorsal = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, dorsal);
            if (idJugadorExcluir != null) {
                pstmt.setInt(2, idJugadorExcluir);
            }

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Valida los datos de un jugador antes de insertar/actualizar
     * @param jugador El jugador a validar
     * @return Lista de errores de validación (vacía si todo es válido)
     */
    public List<String> validar(Jugador jugador) {
        List<String> errores = new ArrayList<>();

        // Nombre obligatorio
        if (jugador.getNombre() == null || jugador.getNombre().trim().isEmpty()) {
            errores.add("El nombre es obligatorio");
        } else if (jugador.getNombre().length() > 50) {
            errores.add("El nombre no puede exceder 50 caracteres");
        }

        // Apellidos obligatorios
        if (jugador.getApellidos() == null || jugador.getApellidos().trim().isEmpty()) {
            errores.add("Los apellidos son obligatorios");
        } else if (jugador.getApellidos().length() > 100) {
            errores.add("Los apellidos no pueden exceder 100 caracteres");
        }

        // Fecha de nacimiento obligatoria y válida
        if (jugador.getFechaNacimiento() == null) {
            errores.add("La fecha de nacimiento es obligatoria");
        } else {
            LocalDate hoy = LocalDate.now();
            int edad = Period.between(jugador.getFechaNacimiento(), hoy).getYears();
            if (edad < 5 || edad > 25) {
                errores.add("La edad del jugador debe estar entre 5 y 25 años");
            }
        }

        // Dorsal obligatorio y válido
        if (jugador.getDorsal() == null) {
            errores.add("El dorsal es obligatorio");
        } else if (jugador.getDorsal() < 1 || jugador.getDorsal() > 99) {
            errores.add("El dorsal debe estar entre 1 y 99");
        }

        // Posición obligatoria y válida
        if (jugador.getPosicion() == null || jugador.getPosicion().trim().isEmpty()) {
            errores.add("La posición es obligatoria");
        } else {
            String pos = jugador.getPosicion().toUpperCase();
            if (!pos.equals("POR") && !pos.equals("DEF") && !pos.equals("MED") && !pos.equals("DEL")) {
                errores.add("La posición debe ser POR, DEF, MED o DEL");
            }
        }

        // Categoría obligatoria
        if (jugador.getCategoria() == null || jugador.getCategoria().trim().isEmpty()) {
            errores.add("La categoría es obligatoria");
        }

        // Altura opcional pero si existe debe ser válida
        if (jugador.getAltura() != null && (jugador.getAltura() < 100 || jugador.getAltura() > 220)) {
            errores.add("La altura debe estar entre 100 y 220 cm");
        }

        // Peso opcional pero si existe debe ser válido
        if (jugador.getPeso() != null && (jugador.getPeso() < 20 || jugador.getPeso() > 120)) {
            errores.add("El peso debe estar entre 20 y 120 kg");
        }

        // Estado válido si se proporciona
        if (jugador.getEstado() != null && !jugador.getEstado().isEmpty()) {
            String estado = jugador.getEstado();
            if (!estado.equals("Activo") && !estado.equals("Lesionado") && !estado.equals("Inactivo")) {
                errores.add("El estado debe ser Activo, Lesionado o Inactivo");
            }
        }

        return errores;
    }

    // ==================== HELPERS ====================

    /**
     * Mapea un ResultSet a un objeto Jugador
     */
    private Jugador mapearJugador(ResultSet rs) throws SQLException {
        Jugador jugador = new Jugador();
        jugador.setId(rs.getInt("id_jugador"));
        jugador.setNombre(rs.getString("nombre"));
        jugador.setApellidos(rs.getString("apellidos"));
        jugador.setDorsal(rs.getInt("dorsal"));
        jugador.setPosicion(rs.getString("posicion"));
        jugador.setCategoria(rs.getString("categoria"));

        Date fechaNac = rs.getDate("fecha_nacimiento");
        if (fechaNac != null) {
            LocalDate fechaNacimiento = fechaNac.toLocalDate();
            jugador.setFechaNacimiento(fechaNacimiento);
            jugador.setEdad(Period.between(fechaNacimiento, LocalDate.now()).getYears());
        }

        // Campos opcionales
        double altura = rs.getDouble("altura");
        if (!rs.wasNull()) {
            jugador.setAltura(altura);
        }

        double peso = rs.getDouble("peso");
        if (!rs.wasNull()) {
            jugador.setPeso(peso);
        }

        jugador.setFoto(rs.getBytes("foto"));
        jugador.setEstado(rs.getString("estado"));

        return jugador;
    }
}

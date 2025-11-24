package com.javafx.scouteo.dao;

import com.javafx.scouteo.model.Configuracion;
import com.javafx.scouteo.util.ConexionBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConfiguracionDAO {

    // ==================== READ ====================

    /**
     * Obtiene la configuración del club (solo hay un registro)
     * @return La configuración del club
     */
    public Configuracion obtener() {
        String sql = "SELECT * FROM configuracion LIMIT 1";
        Configuracion config = null;

        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                config = mapearConfiguracion(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return config;
    }

    // ==================== UPDATE ====================

    /**
     * Actualiza la configuración del club
     * @param config La configuración actualizada
     * @return true si se actualizó correctamente
     */
    public boolean actualizar(Configuracion config) {
        String sql = "UPDATE configuracion SET nombre_club = ?, localidad = ?, presidente = ?, " +
                     "email = ?, telefono = ?, escudo = ?, temporada_actual = ? WHERE id_config = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, config.getNombreClub());
            pstmt.setString(2, config.getLocalidad());
            pstmt.setString(3, config.getPresidente());
            pstmt.setString(4, config.getEmail());
            pstmt.setString(5, config.getTelefono());

            if (config.getEscudo() != null) {
                pstmt.setBytes(6, config.getEscudo());
            } else {
                pstmt.setNull(6, Types.BLOB);
            }

            pstmt.setString(7, config.getTemporadaActual());
            pstmt.setInt(8, config.getIdConfig() != null ? config.getIdConfig() : 1);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Inicializa la configuración si no existe
     * @param config La configuración inicial
     * @return true si se insertó correctamente
     */
    public boolean inicializar(Configuracion config) {
        String sql = "INSERT INTO configuracion (id_config, nombre_club, localidad, presidente, " +
                     "email, telefono, escudo, temporada_actual) VALUES (1, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, config.getNombreClub());
            pstmt.setString(2, config.getLocalidad());
            pstmt.setString(3, config.getPresidente());
            pstmt.setString(4, config.getEmail());
            pstmt.setString(5, config.getTelefono());

            if (config.getEscudo() != null) {
                pstmt.setBytes(6, config.getEscudo());
            } else {
                pstmt.setNull(6, Types.BLOB);
            }

            pstmt.setString(7, config.getTemporadaActual());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ==================== VALIDACIONES ====================

    /**
     * Valida los datos de configuración antes de actualizar
     * @param config La configuración a validar
     * @return Lista de errores de validación (vacía si todo es válido)
     */
    public List<String> validar(Configuracion config) {
        List<String> errores = new ArrayList<>();

        // Nombre del club obligatorio
        if (config.getNombreClub() == null || config.getNombreClub().trim().isEmpty()) {
            errores.add("El nombre del club es obligatorio");
        } else if (config.getNombreClub().length() > 100) {
            errores.add("El nombre del club no puede exceder 100 caracteres");
        }

        // Localidad obligatoria
        if (config.getLocalidad() == null || config.getLocalidad().trim().isEmpty()) {
            errores.add("La localidad es obligatoria");
        } else if (config.getLocalidad().length() > 100) {
            errores.add("La localidad no puede exceder 100 caracteres");
        }

        // Presidente obligatorio
        if (config.getPresidente() == null || config.getPresidente().trim().isEmpty()) {
            errores.add("El nombre del presidente es obligatorio");
        } else if (config.getPresidente().length() > 100) {
            errores.add("El nombre del presidente no puede exceder 100 caracteres");
        }

        // Email obligatorio y con formato válido
        if (config.getEmail() == null || config.getEmail().trim().isEmpty()) {
            errores.add("El email es obligatorio");
        } else if (!config.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            errores.add("El formato del email no es válido");
        } else if (config.getEmail().length() > 100) {
            errores.add("El email no puede exceder 100 caracteres");
        }

        // Teléfono obligatorio
        if (config.getTelefono() == null || config.getTelefono().trim().isEmpty()) {
            errores.add("El teléfono es obligatorio");
        } else if (config.getTelefono().length() > 15) {
            errores.add("El teléfono no puede exceder 15 caracteres");
        }

        // Temporada actual obligatoria y con formato válido
        if (config.getTemporadaActual() == null || config.getTemporadaActual().trim().isEmpty()) {
            errores.add("La temporada actual es obligatoria");
        } else if (!config.getTemporadaActual().matches("^\\d{4}/\\d{2}$")) {
            errores.add("La temporada debe tener el formato YYYY/YY (ej: 2024/25)");
        }

        return errores;
    }

    // ==================== HELPERS ====================

    /**
     * Mapea un ResultSet a un objeto Configuracion
     */
    private Configuracion mapearConfiguracion(ResultSet rs) throws SQLException {
        Configuracion config = new Configuracion();
        config.setIdConfig(rs.getInt("id_config"));
        config.setNombreClub(rs.getString("nombre_club"));
        config.setLocalidad(rs.getString("localidad"));
        config.setPresidente(rs.getString("presidente"));
        config.setEmail(rs.getString("email"));
        config.setTelefono(rs.getString("telefono"));
        config.setEscudo(rs.getBytes("escudo"));
        config.setTemporadaActual(rs.getString("temporada_actual"));
        return config;
    }
}

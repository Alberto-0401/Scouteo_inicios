package com.javafx.scouteo.dao;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.google.gson.JsonObject;
import com.javafx.scouteo.model.Usuario;
import com.javafx.scouteo.util.ApiClient;
import com.javafx.scouteo.util.ConexionBD;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class UsuarioDAO {

    /**
     * Autentica un usuario por email y contraseña.
     * @return Usuario si las credenciales son válidas y el usuario está activo, null si no.
     */
    public Usuario autenticar(String email, String password) {
        String sql = "SELECT * FROM usuarios WHERE email = ? AND activo = 1";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email.trim().toLowerCase());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String hashGuardado = rs.getString("password_hash");
                BCrypt.Result resultado = BCrypt.verifyer().verify(password.toCharArray(), hashGuardado);

                if (resultado.verified) {
                    Usuario usuario = mapearUsuario(rs);
                    actualizarUltimoAcceso(usuario.getId(), conn);
                    return usuario;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Busca un usuario por su ID.
     */
    public Usuario obtenerPorId(int id) {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapearUsuario(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Verifica si un email ya está registrado.
     */
    public boolean existeEmail(String email) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email.trim().toLowerCase());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Registra un nuevo usuario con hash bcrypt de la contraseña.
     */
    public int insertar(Usuario usuario, String passwordPlano) {
        String hash = BCrypt.withDefaults().hashToString(12, passwordPlano.toCharArray());
        String sql = "INSERT INTO usuarios (club_id, email, password_hash, rol, nombre, apellidos, telefono, activo) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, 1)";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (usuario.getClubId() != null) pstmt.setInt(1, usuario.getClubId());
            else pstmt.setNull(1, java.sql.Types.INTEGER);
            pstmt.setString(2, usuario.getEmail().trim().toLowerCase());
            pstmt.setString(3, hash);
            pstmt.setString(4, usuario.getRol());
            pstmt.setString(5, usuario.getNombre());
            pstmt.setString(6, usuario.getApellidos());
            pstmt.setString(7, usuario.getTelefono());

            if (pstmt.executeUpdate() > 0) {
                ResultSet keys = pstmt.getGeneratedKeys();
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Devuelve todos los usuarios con rol 'entrenador' activos del club indicado.
     */
    public List<Usuario> obtenerEntrenadoresPorClub(int clubId) {
        String json = ApiClient.getInstance().get("/usuarios?rol=entrenador&clubId=" + clubId);
        if (json == null) return List.of();
        return ApiClient.getInstance().fromJsonList(json, JsonObject.class)
                .stream().map(this::mapearUsuarioJson).toList();
    }

    private Usuario mapearUsuarioJson(JsonObject o) {
        Usuario u = new Usuario();
        if (o.has("id") && !o.get("id").isJsonNull()) u.setId(o.get("id").getAsInt());
        if (o.has("nombre") && !o.get("nombre").isJsonNull()) u.setNombre(o.get("nombre").getAsString());
        if (o.has("apellidos") && !o.get("apellidos").isJsonNull()) u.setApellidos(o.get("apellidos").getAsString());
        if (o.has("email") && !o.get("email").isJsonNull()) u.setEmail(o.get("email").getAsString());
        if (o.has("rol") && !o.get("rol").isJsonNull()) u.setRol(o.get("rol").getAsString());
        if (o.has("clubId") && !o.get("clubId").isJsonNull()) u.setClubId(o.get("clubId").getAsInt());
        u.setActivo(!o.has("activo") || o.get("activo").getAsBoolean());
        return u;
    }

    private void actualizarUltimoAcceso(int idUsuario, Connection conn) {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "UPDATE usuarios SET ultimo_acceso = NOW() WHERE id = ?")) {
            pstmt.setInt(1, idUsuario);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        int cid = rs.getInt("club_id");
        if (!rs.wasNull()) u.setClubId(cid);
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRol(rs.getString("rol"));
        u.setNombre(rs.getString("nombre"));
        u.setApellidos(rs.getString("apellidos"));
        u.setTelefono(rs.getString("telefono"));
        u.setFotoUrl(rs.getString("foto_url"));
        u.setActivo(rs.getInt("activo") == 1);
        Timestamp ts = rs.getTimestamp("ultimo_acceso");
        if (ts != null) u.setUltimoAcceso(ts.toLocalDateTime());
        return u;
    }
}

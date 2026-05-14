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
        String json = ApiClient.getInstance().get("/usuarios/" + id);
        if (json == null) return null;
        try {
            return mapearUsuarioJson(com.google.gson.JsonParser.parseString(json).getAsJsonObject());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Verifica si un email ya está registrado consultando la lista de usuarios del club.
     */
    public boolean existeEmail(String email) {
        String json = ApiClient.getInstance().get("/usuarios");
        if (json == null) return false;
        String emailNorm = email.trim().toLowerCase();
        return ApiClient.getInstance().fromJsonList(json, JsonObject.class).stream()
            .anyMatch(o -> o.has("email") && !o.get("email").isJsonNull()
                       && emailNorm.equals(o.get("email").getAsString().toLowerCase()));
    }

    /**
     * Registra un nuevo usuario via API (el backend gestiona el hash bcrypt).
     */
    public int insertar(Usuario usuario, String passwordPlano) {
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("email",    usuario.getEmail().trim().toLowerCase());
        body.put("password", passwordPlano);
        body.put("rol",      usuario.getRol());
        body.put("nombre",   usuario.getNombre());
        if (usuario.getApellidos() != null) body.put("apellidos", usuario.getApellidos());
        if (usuario.getTelefono()  != null) body.put("telefono",  usuario.getTelefono());

        String json = ApiClient.getInstance().post("/usuarios", body);
        if (json == null) return -1;
        try {
            return com.google.gson.JsonParser.parseString(json).getAsJsonObject().get("id").getAsInt();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
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

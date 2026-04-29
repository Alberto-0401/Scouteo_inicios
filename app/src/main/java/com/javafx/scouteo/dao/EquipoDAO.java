package com.javafx.scouteo.dao;

import com.google.gson.*;
import com.javafx.scouteo.model.Equipo;
import com.javafx.scouteo.model.Usuario;
import com.javafx.scouteo.util.ApiClient;
import com.javafx.scouteo.util.SesionUsuario;

import java.util.*;

public class EquipoDAO {

    private final ApiClient api = ApiClient.getInstance();

    public int insertar(Equipo e) {
        String json = api.post("/equipos", toMap(e));
        if (json == null) return -1;
        return JsonParser.parseString(json).getAsJsonObject().get("id").getAsInt();
    }

    public List<Equipo> obtenerTodos() {
        return fromList(api.get("/equipos"));
    }

    public List<Equipo> obtenerPorClub(int clubId) {
        return fromList(api.get("/equipos"));
    }

    public List<Equipo> obtenerPorEntrenador(int entrenadorId) {
        Integer equipoId = SesionUsuario.getInstance().getEquipoId();
        if (equipoId != null && equipoId > 0) {
            Equipo eq = obtenerPorId(equipoId);
            if (eq != null) return List.of(eq);
        }
        // Fallback: all equipos (entrenador with no assigned team)
        return fromList(api.get("/equipos"));
    }

    public Equipo obtenerPorId(int id) {
        String json = api.get("/equipos/" + id);
        return json != null ? mapear(JsonParser.parseString(json).getAsJsonObject()) : null;
    }

    public boolean actualizar(Equipo e) {
        return api.put("/equipos/" + e.getId(), toMap(e)) != null;
    }

    public boolean eliminar(int id) {
        return api.delete("/equipos/" + id);
    }

    public boolean asignarEntrenador(int equipoId, int entrenadorId, String rol) {
        return api.post("/equipos/" + equipoId + "/entrenadores/" + entrenadorId, Map.of()) != null;
    }

    public List<Usuario> obtenerEntrenadoresPorEquipo(int equipoId) {
        String json = api.get("/equipos/" + equipoId + "/entrenadores");
        if (json == null) return List.of();
        return api.fromJsonList(json, JsonObject.class).stream().map(this::mapearUsuario).toList();
    }

    public boolean quitarEntrenador(int equipoId, int entrenadorId) {
        return api.delete("/equipos/" + equipoId + "/entrenadores/" + entrenadorId);
    }

    private List<Equipo> fromList(String json) {
        if (json == null) return List.of();
        return api.fromJsonList(json, JsonObject.class).stream().map(this::mapear).toList();
    }

    private Equipo mapear(JsonObject o) {
        Equipo e = new Equipo();
        e.setId(o.get("id").getAsInt());
        e.setNombre(getStr(o, "nombre"));
        e.setCategoria(getStr(o, "categoria"));
        e.setTemporada(getStr(o, "temporada"));
        e.setCampo(getStr(o, "campo"));
        e.setEscudoUrl(getStr(o, "escudoUrl"));
        e.setActivo(!o.has("activo") || o.get("activo").getAsBoolean());
        e.setClubId(o.has("clubId") && !o.get("clubId").isJsonNull() ? o.get("clubId").getAsInt() : null);
        return e;
    }

    private Map<String, Object> toMap(Equipo e) {
        Map<String, Object> m = new HashMap<>();
        m.put("nombre", e.getNombre());
        m.put("categoria", e.getCategoria() != null ? e.getCategoria() : "senior");
        m.put("temporada", e.getTemporada());
        m.put("campo", e.getCampo());
        m.put("escudoUrl", e.getEscudoUrl());
        return m;
    }

    private Usuario mapearUsuario(JsonObject o) {
        Usuario u = new Usuario();
        u.setId(o.get("id").getAsInt());
        u.setNombre(getStr(o, "nombre"));
        u.setApellidos(getStr(o, "apellidos"));
        u.setEmail(getStr(o, "email"));
        u.setRol(o.has("rol") ? o.get("rol").getAsString().toLowerCase() : "entrenador");
        u.setActivo(!o.has("activo") || o.get("activo").getAsBoolean());
        return u;
    }

    private String getStr(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : null;
    }
}

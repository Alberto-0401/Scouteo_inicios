package com.javafx.scouteo.dao;

import com.google.gson.*;
import com.javafx.scouteo.model.HistorialMedico;
import com.javafx.scouteo.util.ApiClient;

import java.time.LocalDate;
import java.util.*;

public class HistorialMedicoDAO {

    private final ApiClient api = ApiClient.getInstance();

    public int insertar(HistorialMedico h) {
        String json = api.post("/historial-medico", toMap(h));
        if (json == null) return -1;
        return JsonParser.parseString(json).getAsJsonObject().get("id").getAsInt();
    }

    public List<HistorialMedico> obtenerPorJugador(int jugadorId) {
        String json = api.get("/historial-medico/jugador/" + jugadorId);
        return fromList(json);
    }

    public List<HistorialMedico> obtenerPorEquipo(int equipoId) {
        // Fetch all jugadores for equipo, then aggregate their historial
        ApiClient apiClient = ApiClient.getInstance();
        String jugJson = apiClient.get("/jugadores?equipoId=" + equipoId);
        if (jugJson == null) return List.of();
        List<com.google.gson.JsonObject> jugadores = apiClient.fromJsonList(jugJson, com.google.gson.JsonObject.class);
        List<HistorialMedico> result = new ArrayList<>();
        for (com.google.gson.JsonObject j : jugadores) {
            if (!j.has("id") || j.get("id").isJsonNull()) continue;
            int jugadorId = j.get("id").getAsInt();
            result.addAll(obtenerPorJugador(jugadorId));
        }
        return result;
    }

    public boolean actualizar(HistorialMedico h) {
        return api.put("/historial-medico/" + h.getId(), toMap(h)) != null;
    }

    public boolean eliminar(int id) {
        return api.delete("/historial-medico/" + id);
    }

    public int contarLesionesActivas(int equipoId) {
        return 0; // Not available without iterating all players
    }

    private List<HistorialMedico> fromList(String json) {
        if (json == null) return List.of();
        return api.fromJsonList(json, JsonObject.class).stream().map(this::mapear).toList();
    }

    private HistorialMedico mapear(JsonObject o) {
        HistorialMedico h = new HistorialMedico();
        h.setId(o.has("id") ? o.get("id").getAsInt() : null);
        h.setJugadorId(o.has("jugadorId") && !o.get("jugadorId").isJsonNull() ? o.get("jugadorId").getAsInt() : null);
        h.setTipoLesion(getStr(o, "tipoLesion"));
        h.setZonaAfectada(getStr(o, "zonaAfectada"));
        h.setObservaciones(getStr(o, "observaciones"));
        h.setNombreJugador(getStr(o, "jugadorNombre"));
        if (o.has("fechaLesion") && !o.get("fechaLesion").isJsonNull())
            h.setFechaLesion(parseDate(o.get("fechaLesion")));
        if (o.has("fechaRecuperacionEst") && !o.get("fechaRecuperacionEst").isJsonNull())
            h.setFechaRecuperacionEst(parseDate(o.get("fechaRecuperacionEst")));
        if (o.has("fechaAlta") && !o.get("fechaAlta").isJsonNull())
            h.setFechaAlta(parseDate(o.get("fechaAlta")));
        return h;
    }

    private Map<String, Object> toMap(HistorialMedico h) {
        Map<String, Object> m = new HashMap<>();
        if (h.getJugadorId() != null) m.put("jugadorId", h.getJugadorId());
        m.put("tipoLesion", h.getTipoLesion());
        if (h.getZonaAfectada() != null) m.put("zonaAfectada", h.getZonaAfectada());
        if (h.getObservaciones() != null) m.put("observaciones", h.getObservaciones());
        if (h.getFechaLesion() != null) m.put("fechaLesion", h.getFechaLesion().toString());
        if (h.getFechaRecuperacionEst() != null) m.put("fechaRecuperacionEst", h.getFechaRecuperacionEst().toString());
        if (h.getFechaAlta() != null) m.put("fechaAlta", h.getFechaAlta().toString());
        return m;
    }

    private LocalDate parseDate(JsonElement el) {
        if (el.isJsonArray()) {
            JsonArray a = el.getAsJsonArray();
            return LocalDate.of(a.get(0).getAsInt(), a.get(1).getAsInt(), a.get(2).getAsInt());
        }
        return LocalDate.parse(el.getAsString());
    }

    private String getStr(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : null;
    }
}

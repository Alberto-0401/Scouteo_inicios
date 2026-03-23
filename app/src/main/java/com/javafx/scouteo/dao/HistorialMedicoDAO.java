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
        return List.of(); // API has no team-level historial endpoint
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
        h.setTipoLesion(getStr(o, "tipo"));
        h.setObservaciones(getStr(o, "descripcion"));
        h.setNombreJugador(getStr(o, "jugadorNombre"));
        if (o.has("fechaInicio") && !o.get("fechaInicio").isJsonNull())
            h.setFechaLesion(parseDate(o.get("fechaInicio")));
        if (o.has("fechaFin") && !o.get("fechaFin").isJsonNull())
            h.setFechaAlta(parseDate(o.get("fechaFin")));
        return h;
    }

    private Map<String, Object> toMap(HistorialMedico h) {
        Map<String, Object> m = new HashMap<>();
        if (h.getJugadorId() != null) m.put("jugadorId", h.getJugadorId());
        m.put("tipo", h.getTipoLesion());
        String desc = (h.getZonaAfectada() != null ? h.getZonaAfectada() : "") +
                      (h.getObservaciones() != null ? " " + h.getObservaciones() : "");
        m.put("descripcion", desc.trim());
        if (h.getFechaLesion() != null) m.put("fechaInicio", h.getFechaLesion().toString());
        LocalDate fin = h.getFechaAlta() != null ? h.getFechaAlta() : h.getFechaRecuperacionEst();
        if (fin != null) m.put("fechaFin", fin.toString());
        m.put("bajaDeportiva", true);
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

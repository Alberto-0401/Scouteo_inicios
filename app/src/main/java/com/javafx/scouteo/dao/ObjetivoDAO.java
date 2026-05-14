package com.javafx.scouteo.dao;

import com.google.gson.*;
import com.javafx.scouteo.model.Objetivo;
import com.javafx.scouteo.util.ApiClient;

import java.time.LocalDate;
import java.util.*;

public class ObjetivoDAO {

    private final ApiClient api = ApiClient.getInstance();

    public int insertar(Objetivo obj) {
        String json = api.post("/objetivos", toMap(obj));
        if (json == null) return -1;
        return JsonParser.parseString(json).getAsJsonObject().get("id").getAsInt();
    }

    public List<Objetivo> obtenerPorEquipo(int equipoId) {
        return obtenerTodosPorEquipoOJugadores(equipoId).stream()
            .filter(o -> o.getJugadorId() == null).toList();
    }

    public List<Objetivo> obtenerPorJugador(int jugadorId) {
        return obtenerTodos().stream()
            .filter(o -> jugadorId == (o.getJugadorId() != null ? o.getJugadorId() : -1)).toList();
    }

    public List<Objetivo> obtenerTodosPorEquipoOJugadores(int equipoId) {
        String jugJson = api.get("/jugadores?equipoId=" + equipoId);
        Set<Integer> jugadorIds = new HashSet<>();
        if (jugJson != null) {
            api.fromJsonList(jugJson, JsonObject.class)
               .forEach(j -> { if (j.has("id") && !j.get("id").isJsonNull()) jugadorIds.add(j.get("id").getAsInt()); });
        }
        return obtenerTodos().stream()
            .filter(o -> {
                if (o.getEquipoId() != null) return o.getEquipoId().equals(equipoId);
                if (o.getJugadorId() != null) return jugadorIds.contains(o.getJugadorId());
                return false;
            })
            .toList();
    }

    private List<Objetivo> obtenerTodos() {
        return fromList(api.get("/objetivos"));
    }

    public boolean actualizar(Objetivo obj) {
        return api.put("/objetivos/" + obj.getId(), toMap(obj)) != null;
    }

    public boolean eliminar(int id) {
        return api.delete("/objetivos/" + id);
    }

    private List<Objetivo> fromList(String json) {
        if (json == null) return List.of();
        return api.fromJsonList(json, JsonObject.class).stream().map(this::mapear).toList();
    }

    private Objetivo mapear(JsonObject o) {
        Objetivo obj = new Objetivo();
        obj.setId(o.has("id") ? o.get("id").getAsInt() : null);
        obj.setEquipoId(o.has("equipoId") && !o.get("equipoId").isJsonNull() ? o.get("equipoId").getAsInt() : null);
        obj.setJugadorId(o.has("jugadorId") && !o.get("jugadorId").isJsonNull() ? o.get("jugadorId").getAsInt() : null);
        obj.setDescripcion(getStr(o, "descripcion"));
        obj.setPrioridad(getStr(o, "prioridad"));
        obj.setEstado(getStr(o, "estado"));
        obj.setProgresoPct(o.has("progresoPct") && !o.get("progresoPct").isJsonNull() ? o.get("progresoPct").getAsInt() : 0);
        obj.setObservaciones(getStr(o, "observaciones"));
        obj.setNombreEquipo(getStr(o, "equipoNombre"));
        obj.setNombreJugador(getStr(o, "jugadorNombre"));
        if (o.has("fechaInicio") && !o.get("fechaInicio").isJsonNull())
            obj.setFechaInicio(parseDate(o.get("fechaInicio")));
        if (o.has("fechaLimite") && !o.get("fechaLimite").isJsonNull())
            obj.setFechaLimite(parseDate(o.get("fechaLimite")));
        return obj;
    }

    private Map<String, Object> toMap(Objetivo obj) {
        Map<String, Object> m = new HashMap<>();
        if (obj.getEquipoId() != null) m.put("equipoId", obj.getEquipoId());
        if (obj.getJugadorId() != null) m.put("jugadorId", obj.getJugadorId());
        m.put("descripcion", obj.getDescripcion() != null ? obj.getDescripcion() : "");
        if (obj.getFechaInicio() != null) m.put("fechaInicio", obj.getFechaInicio().toString());
        if (obj.getFechaLimite() != null) m.put("fechaLimite", obj.getFechaLimite().toString());
        m.put("prioridad", obj.getPrioridad() != null ? obj.getPrioridad() : "media");
        m.put("estado", obj.getEstado() != null ? obj.getEstado() : "pendiente");
        if (obj.getProgresoPct() != null) m.put("progresoPct", obj.getProgresoPct());
        if (obj.getObservaciones() != null) m.put("observaciones", obj.getObservaciones());
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

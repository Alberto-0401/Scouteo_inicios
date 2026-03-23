package com.javafx.scouteo.dao;

import com.google.gson.*;
import com.javafx.scouteo.model.EventoPartido;
import com.javafx.scouteo.util.ApiClient;

import java.util.*;

public class EventoPartidoDAO {

    private final ApiClient api = ApiClient.getInstance();

    public List<EventoPartido> obtenerPorPartido(int partidoId) {
        String json = api.get("/eventos/partido/" + partidoId);
        if (json == null) return List.of();
        return api.fromJsonList(json, JsonObject.class).stream().map(o -> {
            EventoPartido e = new EventoPartido();
            e.setId(o.get("id").getAsInt());
            e.setPartidoId(o.has("partidoId") ? o.get("partidoId").getAsInt() : partidoId);
            e.setMinuto(o.has("minuto") && !o.get("minuto").isJsonNull() ? o.get("minuto").getAsInt() : 0);
            e.setTipo(o.has("tipo") && !o.get("tipo").isJsonNull() ? o.get("tipo").getAsString() : null);
            e.setJugadorId(o.has("jugadorId") && !o.get("jugadorId").isJsonNull() ? o.get("jugadorId").getAsInt() : null);
            e.setJugadorNombre(getStr(o, "jugadorNombre"));
            e.setJugador2Id(o.has("jugador2Id") && !o.get("jugador2Id").isJsonNull() ? o.get("jugador2Id").getAsInt() : null);
            e.setJugador2Nombre(getStr(o, "jugador2Nombre"));
            e.setDescripcion(getStr(o, "descripcion"));
            return e;
        }).toList();
    }

    public boolean insertar(EventoPartido ev) {
        Map<String, Object> body = new HashMap<>();
        body.put("partidoId", ev.getPartidoId());
        body.put("minuto", ev.getMinuto());
        body.put("tipo", ev.getTipo());
        if (ev.getJugadorId() != null) body.put("jugadorId", ev.getJugadorId());
        if (ev.getJugador2Id() != null) body.put("jugador2Id", ev.getJugador2Id());
        body.put("descripcion", ev.getDescripcion());
        String json = api.post("/eventos", body);
        if (json != null) {
            ev.setId(JsonParser.parseString(json).getAsJsonObject().get("id").getAsInt());
            return true;
        }
        return false;
    }

    public boolean eliminar(int id) {
        return api.delete("/eventos/" + id);
    }

    private String getStr(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : null;
    }
}

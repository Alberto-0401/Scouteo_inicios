package com.javafx.scouteo.dao;

import com.google.gson.*;
import com.javafx.scouteo.model.EstadisticaPartido;
import com.javafx.scouteo.model.JugadorPartido;
import com.javafx.scouteo.util.ApiClient;

import java.util.*;

public class JugadorPartidoDAO {

    private final ApiClient api = ApiClient.getInstance();

    public boolean insertarConEstadistica(JugadorPartido jp, EstadisticaPartido est) {
        Map<String, Object> body = new HashMap<>();
        body.put("partidoId", jp.getIdPartido());
        body.put("jugadorId", jp.getIdJugador());
        body.put("minutoEntrada", 0);
        if (est != null) {
            if (est.getMinutosJugados() != null) body.put("minutoSalida", est.getMinutosJugados());
            if (est.getGoles() != null) body.put("goles", est.getGoles());
            if (est.getAsistencias() != null) body.put("asistencias", est.getAsistencias());
            if (est.getTarjetasAmarillas() != null) body.put("tarjetasAmarillas", est.getTarjetasAmarillas());
            if (est.getTarjetasRojas() != null) body.put("tarjetasRojas", est.getTarjetasRojas());
            if (est.getParadas() != null) body.put("paradas", est.getParadas());
            if (est.getValoracion() != null) body.put("valoracion", est.getValoracion());
            if (est.getObservaciones() != null) body.put("notaEntrenador", est.getObservaciones());
        }
        String json = api.post("/alineaciones", body);
        if (json != null) {
            JsonObject o = JsonParser.parseString(json).getAsJsonObject();
            jp.setIdEstadistica(o.get("id").getAsInt());
            return true;
        }
        return false;
    }

    public boolean insertar(JugadorPartido jp) {
        return insertarConEstadistica(jp, null);
    }

    public List<JugadorPartido> obtenerPorJugador(int jugadorId) {
        // Not directly available via API without partido context - return empty
        return List.of();
    }

    public List<JugadorPartido> obtenerPorPartido(int partidoId) {
        String json = api.get("/alineaciones/partido/" + partidoId);
        if (json == null) return List.of();
        return api.fromJsonList(json, JsonObject.class).stream().map(o -> {
            JugadorPartido jp = new JugadorPartido();
            jp.setIdJugadorPartido(o.get("id").getAsInt());
            jp.setIdJugador(o.has("jugadorId") && !o.get("jugadorId").isJsonNull() ? o.get("jugadorId").getAsInt() : null);
            jp.setIdPartido(partidoId);
            jp.setIdEstadistica(o.get("id").getAsInt());
            jp.setTitular(true);
            jp.setConvocado(true);
            return jp;
        }).toList();
    }

    public JugadorPartido obtenerPorJugadorYPartido(int jugadorId, int partidoId) {
        return obtenerPorPartido(partidoId).stream()
            .filter(jp -> jp.getIdJugador() != null && jp.getIdJugador() == jugadorId)
            .findFirst().orElse(null);
    }

    public List<JugadorPartido> obtenerTodos() { return List.of(); }

    public int contarPartidosJugador(int jugadorId) { return 0; }

    public boolean actualizar(JugadorPartido jp) {
        if (jp.getIdEstadistica() == null) return false;
        Map<String, Object> body = new HashMap<>();
        body.put("partidoId", jp.getIdPartido());
        body.put("jugadorId", jp.getIdJugador());
        return api.put("/alineaciones/" + jp.getIdEstadistica(), body) != null;
    }

    public boolean eliminar(int jugadorId, int partidoId) {
        JugadorPartido jp = obtenerPorJugadorYPartido(jugadorId, partidoId);
        if (jp == null || jp.getIdEstadistica() == null) return false;
        return api.delete("/alineaciones/" + jp.getIdEstadistica());
    }

    public boolean eliminarPorPartido(int partidoId) {
        obtenerPorPartido(partidoId).forEach(jp -> {
            if (jp.getIdEstadistica() != null) api.delete("/alineaciones/" + jp.getIdEstadistica());
        });
        return true;
    }

    public boolean existeRelacion(int jugadorId, int partidoId) {
        return obtenerPorJugadorYPartido(jugadorId, partidoId) != null;
    }

    public String validar(JugadorPartido jp) {
        if (jp.getIdJugador() == null) return "El jugador es obligatorio.";
        if (jp.getIdPartido() == null) return "El partido es obligatorio.";
        return null;
    }
}

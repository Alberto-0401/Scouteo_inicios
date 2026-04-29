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
        // 1. Get jugador to find equipoId
        String jugJson = api.get("/jugadores/" + jugadorId);
        if (jugJson == null) return List.of();
        JsonObject jugObj = JsonParser.parseString(jugJson).getAsJsonObject();
        if (!jugObj.has("equipoId") || jugObj.get("equipoId").isJsonNull()) return List.of();
        int equipoId = jugObj.get("equipoId").getAsInt();

        // 2. Get all partidos for that equipo
        String partJson = api.get("/partidos?equipoId=" + equipoId);
        if (partJson == null) return List.of();
        List<JsonObject> partidos = api.fromJsonList(partJson, JsonObject.class);

        // 3. For each partido, fetch alineaciones and filter by jugadorId
        List<JugadorPartido> result = new ArrayList<>();
        for (JsonObject partido : partidos) {
            int partidoId = partido.get("id").getAsInt();
            String alinJson = api.get("/alineaciones/partido/" + partidoId);
            if (alinJson == null) continue;
            for (JsonObject alin : api.fromJsonList(alinJson, JsonObject.class)) {
                if (!alin.has("jugadorId") || alin.get("jugadorId").isJsonNull()) continue;
                if (alin.get("jugadorId").getAsInt() != jugadorId) continue;
                int alinId = alin.get("id").getAsInt();
                JugadorPartido jp = new JugadorPartido();
                jp.setIdJugadorPartido(alinId);
                jp.setIdJugador(jugadorId);
                jp.setIdPartido(partidoId);
                jp.setIdEstadistica(alinId);
                jp.setTitular(true);
                jp.setConvocado(true);
                EstadisticaPartidoDAO.cacheEstadistica(mapearEstadistica(alinId, alin));
                result.add(jp);
            }
        }
        return result;
    }

    private EstadisticaPartido mapearEstadistica(int id, JsonObject o) {
        EstadisticaPartido est = new EstadisticaPartido();
        est.setIdEstadistica(id);
        if (o.has("minutosJugados") && !o.get("minutosJugados").isJsonNull())
            est.setMinutosJugados(o.get("minutosJugados").getAsInt());
        if (o.has("goles") && !o.get("goles").isJsonNull())
            est.setGoles(o.get("goles").getAsInt());
        if (o.has("asistencias") && !o.get("asistencias").isJsonNull())
            est.setAsistencias(o.get("asistencias").getAsInt());
        if (o.has("tarjetasAmarillas") && !o.get("tarjetasAmarillas").isJsonNull())
            est.setTarjetasAmarillas(o.get("tarjetasAmarillas").getAsInt());
        if (o.has("tarjetasRojas") && !o.get("tarjetasRojas").isJsonNull())
            est.setTarjetasRojas(o.get("tarjetasRojas").getAsInt());
        if (o.has("paradas") && !o.get("paradas").isJsonNull())
            est.setParadas(o.get("paradas").getAsInt());
        if (o.has("valoracion") && !o.get("valoracion").isJsonNull())
            est.setValoracion(o.get("valoracion").getAsDouble());
        if (o.has("notaEntrenador") && !o.get("notaEntrenador").isJsonNull())
            est.setObservaciones(o.get("notaEntrenador").getAsString());
        return est;
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

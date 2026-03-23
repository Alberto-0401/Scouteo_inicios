package com.javafx.scouteo.dao;

import com.google.gson.*;
import com.javafx.scouteo.model.AsistenciaEntrenamiento;
import com.javafx.scouteo.model.Entrenamiento;
import com.javafx.scouteo.util.ApiClient;

import java.time.LocalDateTime;
import java.util.*;

public class EntrenamientoDAO {

    private final ApiClient api = ApiClient.getInstance();

    public int insertar(Entrenamiento ent) {
        Map<String, Object> body = toMap(ent);
        String json = api.post("/entrenamientos", body);
        if (json == null) return -1;
        return JsonParser.parseString(json).getAsJsonObject().get("id").getAsInt();
    }

    public List<Entrenamiento> obtenerPorEquipo(int equipoId) {
        String json = api.get("/entrenamientos?equipoId=" + equipoId);
        return fromList(json);
    }

    public boolean actualizar(Entrenamiento ent) {
        return api.put("/entrenamientos/" + ent.getId(), toMap(ent)) != null;
    }

    public boolean eliminar(int id) {
        return api.delete("/entrenamientos/" + id);
    }

    public List<AsistenciaEntrenamiento> obtenerAsistenciaPorEntrenamiento(int entrenamientoId, int equipoId) {
        String json = api.get("/asistencias/entrenamiento/" + entrenamientoId);
        if (json == null) return List.of();
        return api.fromJsonList(json, JsonObject.class).stream().map(o -> {
            AsistenciaEntrenamiento a = new AsistenciaEntrenamiento();
            a.setId(o.has("id") ? o.get("id").getAsInt() : null);
            a.setEntrenamientoId(o.has("entrenamientoId") ? o.get("entrenamientoId").getAsInt() : entrenamientoId);
            a.setJugadorId(o.has("jugadorId") && !o.get("jugadorId").isJsonNull() ? o.get("jugadorId").getAsInt() : null);
            a.setEstado(getStr(o, "estado"));
            a.setMotivo(getStr(o, "motivo"));
            if (o.has("jugadorNombre")) {
                String nombre = getStr(o, "jugadorNombre");
                String apellidos = getStr(o, "jugadorApellidos");
                a.setNombreJugador((nombre != null ? nombre : "") + (apellidos != null ? " " + apellidos : ""));
            }
            if (o.has("dorsal") && !o.get("dorsal").isJsonNull()) a.setDorsal(o.get("dorsal").getAsInt());
            return a;
        }).toList();
    }

    public boolean registrarAsistencia(int entrenamientoId, int jugadorId, String estado, String motivo) {
        Map<String, Object> body = new HashMap<>();
        body.put("entrenamientoId", entrenamientoId);
        body.put("jugadorId", jugadorId);
        body.put("estado", estado);
        body.put("motivo", motivo);
        return api.post("/asistencias", body) != null;
    }

    private List<Entrenamiento> fromList(String json) {
        if (json == null) return List.of();
        return api.fromJsonList(json, JsonObject.class).stream().map(this::mapear).toList();
    }

    private Entrenamiento mapear(JsonObject o) {
        Entrenamiento e = new Entrenamiento();
        e.setId(o.get("id").getAsInt());
        e.setEquipoId(o.has("equipoId") && !o.get("equipoId").isJsonNull() ? o.get("equipoId").getAsInt() : null);
        e.setNombreEquipo(getStr(o, "equipoNombre"));
        e.setTipo(getStr(o, "tipo"));
        e.setIntensidad(getStr(o, "intensidad"));
        e.setObjetivos(getStr(o, "objetivos"));
        e.setObservaciones(getStr(o, "observaciones"));
        if (o.has("duracionMin") && !o.get("duracionMin").isJsonNull()) e.setDuracionMin(o.get("duracionMin").getAsInt());
        if (o.has("fechaHora") && !o.get("fechaHora").isJsonNull()) {
            JsonElement fe = o.get("fechaHora");
            if (fe.isJsonArray()) {
                JsonArray a = fe.getAsJsonArray();
                e.setFechaHora(LocalDateTime.of(a.get(0).getAsInt(), a.get(1).getAsInt(), a.get(2).getAsInt(),
                    a.size() > 3 ? a.get(3).getAsInt() : 0, a.size() > 4 ? a.get(4).getAsInt() : 0));
            } else {
                e.setFechaHora(LocalDateTime.parse(fe.getAsString()));
            }
        }
        return e;
    }

    private Map<String, Object> toMap(Entrenamiento e) {
        Map<String, Object> m = new HashMap<>();
        if (e.getEquipoId() != null) m.put("equipoId", e.getEquipoId());
        if (e.getFechaHora() != null) m.put("fechaHora", e.getFechaHora().toString());
        if (e.getDuracionMin() != null) m.put("duracionMin", e.getDuracionMin());
        m.put("tipo", e.getTipo());
        m.put("intensidad", e.getIntensidad() != null ? e.getIntensidad() : "media");
        m.put("objetivos", e.getObjetivos());
        m.put("observaciones", e.getObservaciones());
        return m;
    }

    private String getStr(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : null;
    }
}

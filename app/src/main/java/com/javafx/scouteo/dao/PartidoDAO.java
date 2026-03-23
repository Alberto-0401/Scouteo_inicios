package com.javafx.scouteo.dao;

import com.google.gson.*;
import com.javafx.scouteo.model.Partido;
import com.javafx.scouteo.util.ApiClient;

import java.time.*;
import java.util.*;

public class PartidoDAO {

    private final ApiClient api = ApiClient.getInstance();

    public int insertar(Partido p) {
        String json = api.post("/partidos", toMap(p));
        if (json == null) return -1;
        return JsonParser.parseString(json).getAsJsonObject().get("id").getAsInt();
    }

    public List<Partido> obtenerTodos() {
        return fromList(api.get("/partidos"));
    }

    public List<Partido> obtenerPorEquipo(int equipoId) {
        return fromList(api.get("/partidos?equipoId=" + equipoId));
    }

    public Partido obtenerPorId(int id) {
        String json = api.get("/partidos/" + id);
        return json != null ? mapear(JsonParser.parseString(json).getAsJsonObject()) : null;
    }

    public List<Partido> obtenerPorClub(int clubId) {
        return fromList(api.get("/partidos"));
    }

    public int contarTotal() { return obtenerTodos().size(); }
    public int contarPorEquipo(int equipoId) { return obtenerPorEquipo(equipoId).size(); }
    public int contarPorClub(int clubId) { return obtenerPorClub(clubId).size(); }

    public int[] contarResultadosPorClub(int clubId) {
        return calcularResultados(obtenerPorClub(clubId));
    }

    public int[] contarResultadosPorEquipo(int equipoId) {
        return calcularResultados(obtenerPorEquipo(equipoId));
    }

    private int[] calcularResultados(List<Partido> lista) {
        int v = 0, e = 0, d = 0;
        for (Partido p : lista) {
            if (p.getGolesFavor() == null || p.getGolesContra() == null) continue;
            if (p.getGolesFavor() > p.getGolesContra()) v++;
            else if (p.getGolesFavor().equals(p.getGolesContra())) e++;
            else d++;
        }
        return new int[]{v, e, d};
    }

    public int contarGolesEquipo(int equipoId) {
        return obtenerPorEquipo(equipoId).stream()
            .mapToInt(p -> p.getGolesFavor() != null ? p.getGolesFavor() : 0).sum();
    }

    public boolean actualizar(Partido p) {
        return api.put("/partidos/" + p.getId(), toMap(p)) != null;
    }

    public boolean eliminar(int id) {
        return api.delete("/partidos/" + id);
    }

    private List<Partido> fromList(String json) {
        if (json == null) return List.of();
        return api.fromJsonList(json, JsonObject.class).stream().map(this::mapear).toList();
    }

    private Partido mapear(JsonObject o) {
        Partido p = new Partido();
        p.setId(o.get("id").getAsInt());
        p.setEquipoId(o.has("equipoId") && !o.get("equipoId").isJsonNull() ? o.get("equipoId").getAsInt() : 0);
        p.setRival(getStr(o, "rival"));
        p.setTipo(getStr(o, "tipo"));
        p.setCampo(getStr(o, "campo"));
        p.setSuperficie(getStr(o, "superficie"));
        p.setCompeticion(getStr(o, "competicion"));
        p.setTemporada(getStr(o, "temporada"));
        p.setFormacion(getStr(o, "formacion"));
        p.setNotas(getStr(o, "notas"));
        p.setNombreEquipo(getStr(o, "equipoNombre"));
        if (o.has("golesFavor") && !o.get("golesFavor").isJsonNull()) p.setGolesFavor(o.get("golesFavor").getAsInt());
        if (o.has("golesContra") && !o.get("golesContra").isJsonNull()) p.setGolesContra(o.get("golesContra").getAsInt());
        if (o.has("posesionPct") && !o.get("posesionPct").isJsonNull()) p.setPosesionPct(o.get("posesionPct").getAsInt());
        if (o.has("tirosTotales") && !o.get("tirosTotales").isJsonNull()) p.setTirosTotales(o.get("tirosTotales").getAsInt());
        if (o.has("tirosAPuerta") && !o.get("tirosAPuerta").isJsonNull()) p.setTirosAPuerta(o.get("tirosAPuerta").getAsInt());
        if (o.has("corners") && !o.get("corners").isJsonNull()) p.setCorners(o.get("corners").getAsInt());
        if (o.has("faltas") && !o.get("faltas").isJsonNull()) p.setFaltas(o.get("faltas").getAsInt());
        if (o.has("fechaHora") && !o.get("fechaHora").isJsonNull()) {
            JsonElement fe = o.get("fechaHora");
            if (fe.isJsonArray()) {
                JsonArray a = fe.getAsJsonArray();
                p.setFechaHora(LocalDateTime.of(
                    a.get(0).getAsInt(), a.get(1).getAsInt(), a.get(2).getAsInt(),
                    a.size() > 3 ? a.get(3).getAsInt() : 0, a.size() > 4 ? a.get(4).getAsInt() : 0));
            } else {
                p.setFechaHora(LocalDateTime.parse(fe.getAsString()));
            }
        }
        return p;
    }

    private Map<String, Object> toMap(Partido p) {
        Map<String, Object> m = new HashMap<>();
        if (p.getEquipoId() > 0) m.put("equipoId", p.getEquipoId());
        if (p.getFechaHora() != null) m.put("fechaHora", p.getFechaHora().toString());
        m.put("rival", p.getRival());
        m.put("tipo", p.getTipo() != null ? p.getTipo() : "local");
        m.put("campo", p.getCampo());
        m.put("superficie", p.getSuperficie() != null ? p.getSuperficie() : "cesped_artificial");
        m.put("competicion", p.getCompeticion());
        m.put("temporada", p.getTemporada());
        m.put("formacion", p.getFormacion());
        if (p.getGolesFavor() != null) m.put("golesFavor", p.getGolesFavor());
        if (p.getGolesContra() != null) m.put("golesContra", p.getGolesContra());
        if (p.getPosesionPct() != null) m.put("posesionPct", p.getPosesionPct());
        if (p.getTirosTotales() != null) m.put("tirosTotales", p.getTirosTotales());
        if (p.getTirosAPuerta() != null) m.put("tirosAPuerta", p.getTirosAPuerta());
        if (p.getCorners() != null) m.put("corners", p.getCorners());
        if (p.getFaltas() != null) m.put("faltas", p.getFaltas());
        m.put("notas", p.getNotas());
        return m;
    }

    private String getStr(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : null;
    }
}

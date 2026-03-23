package com.javafx.scouteo.dao;

import com.google.gson.*;
import com.javafx.scouteo.model.Jugador;
import com.javafx.scouteo.util.ApiClient;

import java.time.LocalDate;
import java.util.*;

public class JugadorDAO {

    private final ApiClient api = ApiClient.getInstance();

    public int insertar(Jugador j) {
        Map<String, Object> body = toMap(j);
        String json = api.post("/jugadores", body);
        if (json == null) return -1;
        JsonObject o = JsonParser.parseString(json).getAsJsonObject();
        return o.get("id").getAsInt();
    }

    public List<Jugador> obtenerTodos() {
        return api.fromJsonList(api.get("/jugadores"), JsonObject.class)
            .stream().map(this::mapear).toList();
    }

    public List<Jugador> obtenerPorEquipo(int equipoId) {
        return api.fromJsonList(api.get("/jugadores?equipoId=" + equipoId), JsonObject.class)
            .stream().map(this::mapear).toList();
    }

    public List<Jugador> obtenerActivosPorEquipo(int equipoId) {
        return obtenerPorEquipo(equipoId).stream()
            .filter(j -> "activo".equals(j.getEstado())).toList();
    }

    public Jugador obtenerPorId(int id) {
        String json = api.get("/jugadores/" + id);
        if (json == null) return null;
        return mapear(JsonParser.parseString(json).getAsJsonObject());
    }

    public List<Jugador> obtenerPorClub(int clubId) {
        return api.fromJsonList(api.get("/jugadores"), JsonObject.class)
            .stream().map(this::mapear).toList();
    }

    public int contarPorEquipo(int equipoId) {
        return obtenerPorEquipo(equipoId).size();
    }

    public int contarPorClub(int clubId) {
        return obtenerPorClub(clubId).size();
    }

    public int contarTotal() {
        return obtenerTodos().size();
    }

    public Map<String, Integer> obtenerDistribucionPorPosicionEquipo(int equipoId) {
        return calcularDistribucion(obtenerPorEquipo(equipoId));
    }

    public Map<String, Integer> obtenerDistribucionPorPosicionClub(int clubId) {
        return calcularDistribucion(obtenerPorClub(clubId));
    }

    public Map<String, Integer> obtenerDistribucionPorPosicion() {
        return calcularDistribucion(obtenerTodos());
    }

    private Map<String, Integer> calcularDistribucion(List<Jugador> lista) {
        Map<String, Integer> dist = new HashMap<>();
        dist.put("POR", 0); dist.put("DEF", 0); dist.put("MED", 0); dist.put("DEL", 0);
        for (Jugador j : lista) {
            String grupo = j.getGrupoPosicion();
            dist.put(grupo, dist.getOrDefault(grupo, 0) + 1);
        }
        return dist;
    }

    public boolean actualizar(Jugador j) {
        return api.put("/jugadores/" + j.getId(), toMap(j)) != null;
    }

    public boolean cambiarEstado(int id, String estado) {
        Jugador j = obtenerPorId(id);
        if (j == null) return false;
        j.setEstado(estado);
        return actualizar(j);
    }

    public boolean vincularUsuario(int jugadorId, int usuarioId) {
        Jugador j = obtenerPorId(jugadorId);
        if (j == null) return false;
        Map<String, Object> body = toMap(j);
        body.put("usuarioId", usuarioId);
        return api.put("/jugadores/" + jugadorId, body) != null;
    }

    public boolean eliminar(int id) {
        return api.delete("/jugadores/" + id);
    }

    public boolean dorsalDisponible(int equipoId, int dorsal, Integer excludeId) {
        return obtenerPorEquipo(equipoId).stream()
            .filter(j -> !j.getId().equals(excludeId))
            .noneMatch(j -> j.getDorsal() != null && j.getDorsal() == dorsal);
    }

    public String validar(Jugador j) {
        if (j.getNombre() == null || j.getNombre().isBlank()) return "El nombre es obligatorio.";
        if (j.getEquipoId() == null) return "El equipo es obligatorio.";
        return null;
    }

    private Jugador mapear(JsonObject o) {
        Jugador j = new Jugador();
        j.setId(o.has("id") ? o.get("id").getAsInt() : null);
        j.setEquipoId(o.has("equipoId") && !o.get("equipoId").isJsonNull() ? o.get("equipoId").getAsInt() : null);
        j.setUsuarioId(o.has("usuarioId") && !o.get("usuarioId").isJsonNull() ? o.get("usuarioId").getAsInt() : null);
        j.setNombre(getStr(o, "nombre"));
        j.setApellidos(getStr(o, "apellidos"));
        j.setDorsal(o.has("dorsal") && !o.get("dorsal").isJsonNull() ? o.get("dorsal").getAsInt() : null);
        j.setPosicion(getStr(o, "posicion"));
        j.setPiernaDominante(getStr(o, "piernaDominante"));
        j.setAlturaCm(o.has("alturaCm") && !o.get("alturaCm").isJsonNull() ? o.get("alturaCm").getAsInt() : null);
        j.setPesoKg(o.has("pesoKg") && !o.get("pesoKg").isJsonNull() ? o.get("pesoKg").getAsDouble() : null);
        j.setFotoUrl(getStr(o, "fotoUrl"));
        j.setContactoEmergencia(getStr(o, "contactoEmergencia"));
        j.setTelefonoEmergencia(getStr(o, "telefonoEmergencia"));
        j.setEstado(getStr(o, "estado"));
        j.setObservaciones(getStr(o, "observaciones"));
        j.setNombreEquipo(getStr(o, "equipoNombre"));
        if (o.has("fechaNacimiento") && !o.get("fechaNacimiento").isJsonNull())
            j.setFechaNacimiento(parseDate(o.get("fechaNacimiento")));
        if (o.has("fechaAlta") && !o.get("fechaAlta").isJsonNull())
            j.setFechaAlta(parseDate(o.get("fechaAlta")));
        return j;
    }

    private Map<String, Object> toMap(Jugador j) {
        Map<String, Object> m = new HashMap<>();
        if (j.getEquipoId() != null) m.put("equipoId", j.getEquipoId());
        if (j.getUsuarioId() != null) m.put("usuarioId", j.getUsuarioId());
        m.put("nombre", j.getNombre());
        m.put("apellidos", j.getApellidos());
        if (j.getFechaNacimiento() != null) m.put("fechaNacimiento", j.getFechaNacimiento().toString());
        if (j.getDorsal() != null) m.put("dorsal", j.getDorsal());
        m.put("posicion", j.getPosicion());
        m.put("piernaDominante", j.getPiernaDominante());
        if (j.getAlturaCm() != null) m.put("alturaCm", j.getAlturaCm());
        if (j.getPesoKg() != null) m.put("pesoKg", j.getPesoKg());
        m.put("fotoUrl", j.getFotoUrl());
        m.put("contactoEmergencia", j.getContactoEmergencia());
        m.put("telefonoEmergencia", j.getTelefonoEmergencia());
        if (j.getFechaAlta() != null) m.put("fechaAlta", j.getFechaAlta().toString());
        m.put("estado", j.getEstado() != null ? j.getEstado() : "activo");
        m.put("observaciones", j.getObservaciones());
        return m;
    }

    private String getStr(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : null;
    }

    private LocalDate parseDate(JsonElement el) {
        if (el.isJsonArray()) {
            JsonArray a = el.getAsJsonArray();
            return LocalDate.of(a.get(0).getAsInt(), a.get(1).getAsInt(), a.get(2).getAsInt());
        }
        return LocalDate.parse(el.getAsString());
    }
}

package com.javafx.scouteo.dao;

import com.google.gson.*;
import com.javafx.scouteo.model.Configuracion;
import com.javafx.scouteo.util.ApiClient;

import java.util.*;

public class ConfiguracionDAO {

    private final ApiClient api = ApiClient.getInstance();

    public Configuracion obtener(int clubId) {
        String json = api.get("/configuracion");
        if (json == null) return null;
        return mapear(JsonParser.parseString(json).getAsJsonObject());
    }

    public boolean actualizar(Configuracion c) {
        Map<String, Object> body = new HashMap<>();
        body.put("nombreClub", c.getNombreClub());
        body.put("localidad", c.getLocalidad());
        body.put("presidente", c.getPresidente());
        body.put("email", c.getEmail());
        body.put("telefono", c.getTelefono());
        body.put("temporadaActual", c.getTemporadaActual());
        return api.put("/configuracion", body) != null;
    }

    public boolean inicializar(Configuracion c) {
        return actualizar(c);
    }

    public String validar(Configuracion c) {
        if (c.getNombreClub() == null || c.getNombreClub().isBlank()) return "El nombre del club es obligatorio.";
        return null;
    }

    private Configuracion mapear(JsonObject o) {
        Configuracion c = new Configuracion();
        if (o.has("idConfig") && !o.get("idConfig").isJsonNull()) c.setIdConfig(o.get("idConfig").getAsInt());
        if (o.has("clubId") && !o.get("clubId").isJsonNull()) c.setClubId(o.get("clubId").getAsInt());
        c.setNombreClub(getStr(o, "nombreClub"));
        c.setLocalidad(getStr(o, "localidad"));
        c.setPresidente(getStr(o, "presidente"));
        c.setEmail(getStr(o, "email"));
        c.setTelefono(getStr(o, "telefono"));
        c.setTemporadaActual(getStr(o, "temporadaActual"));
        c.setEscudo(null); // API does not expose escudo blob
        return c;
    }

    private String getStr(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : null;
    }
}

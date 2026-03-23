package com.javafx.scouteo.dao;

import com.google.gson.*;
import com.javafx.scouteo.model.EstadisticaPartido;
import com.javafx.scouteo.util.ApiClient;

import java.util.*;

public class EstadisticaPartidoDAO {

    private final ApiClient api = ApiClient.getInstance();

    /** Not used directly — use JugadorPartidoDAO.insertarConEstadistica. */
    public int insertar(EstadisticaPartido estadistica) { return -1; }

    public EstadisticaPartido obtenerPorId(int idAlineacion) {
        return null; // Not directly mappable without context
    }

    public List<EstadisticaPartido> obtenerTodos() { return List.of(); }

    public int contarTotalGoles() { return 0; }
    public int contarTotalAsistencias() { return 0; }
    public double obtenerValoracionPromedio() { return 0.0; }

    public boolean actualizar(EstadisticaPartido est) {
        if (est.getIdEstadistica() == null) return false;
        Map<String, Object> body = new HashMap<>();
        if (est.getGoles() != null) body.put("goles", est.getGoles());
        if (est.getAsistencias() != null) body.put("asistencias", est.getAsistencias());
        if (est.getTarjetasAmarillas() != null) body.put("tarjetasAmarillas", est.getTarjetasAmarillas());
        if (est.getTarjetasRojas() != null) body.put("tarjetasRojas", est.getTarjetasRojas());
        if (est.getParadas() != null) body.put("paradas", est.getParadas());
        if (est.getValoracion() != null) body.put("valoracion", est.getValoracion());
        if (est.getObservaciones() != null) body.put("notaEntrenador", est.getObservaciones());
        return api.put("/alineaciones/" + est.getIdEstadistica(), body) != null;
    }

    public boolean eliminar(int id) { return api.delete("/alineaciones/" + id); }

    public String validar(EstadisticaPartido est, String posicion) {
        return null;
    }
}

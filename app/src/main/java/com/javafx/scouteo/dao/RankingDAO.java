package com.javafx.scouteo.dao;

import com.google.gson.*;
import com.javafx.scouteo.controller.RankingController;
import com.javafx.scouteo.util.ApiClient;

import java.util.*;

public class RankingDAO {

    private final ApiClient api = ApiClient.getInstance();

    public List<RankingController.JugadorRanking> obtenerRanking(String ordenarPor, String posicionFiltro, int clubId) {
        return obtenerRanking(ordenarPor, posicionFiltro, clubId, 0);
    }

    public List<RankingController.JugadorRanking> obtenerRanking(String ordenarPor, String posicionFiltro, int clubId, int equipoId) {
        String suffix = equipoId > 0 ? "?equipoId=" + equipoId : "";

        // Fetch all ranking lists and merge by jugadorId
        Map<Long, long[]> data = new LinkedHashMap<>(); // jugadorId -> [goles, asistencias, partidos, valoracion*100]
        Map<Long, String[]> info = new LinkedHashMap<>(); // jugadorId -> [nombre, apellidos, dorsal, posicion]

        parseRanking(api.get("/ranking/goleadores" + suffix), data, info, 0);
        parseRanking(api.get("/ranking/asistencias" + suffix), data, info, 1);

        // Apply position filter client-side
        String posFilter = switch (posicionFiltro != null ? posicionFiltro : "Todas") {
            case "POR" -> "portero";
            case "DEF" -> "defensa";
            case "MED" -> "medio";
            case "DEL" -> "delantero";
            default -> null;
        };

        List<Map.Entry<Long, long[]>> entries = new ArrayList<>(data.entrySet());

        // Sort by selected metric
        int sortIdx = switch (ordenarPor != null ? ordenarPor : "Goles") {
            case "Asistencias" -> 1;
            case "Partidos Jugados" -> 2;
            default -> 0; // Goles
        };
        entries.sort((a, b) -> Long.compare(b.getValue()[sortIdx], a.getValue()[sortIdx]));

        List<RankingController.JugadorRanking> ranking = new ArrayList<>();
        int pos = 1;
        for (Map.Entry<Long, long[]> entry : entries) {
            long id = entry.getKey();
            String[] inf = info.get(id);
            if (inf == null) continue;

            // Position filter
            if (posFilter != null && !inf[3].toLowerCase().contains(posFilter)) continue;

            long[] vals = entry.getValue();
            int goles = (int) vals[0];
            int asist = (int) vals[1];
            int partidos = (int) vals[2];
            double promedio = partidos > 0 ? (double) goles / partidos : 0.0;

            ranking.add(new RankingController.JugadorRanking(
                pos++,
                inf[0] + " " + inf[1],
                Integer.parseInt(inf[2].isEmpty() ? "0" : inf[2]),
                inf[3],
                partidos, goles, asist, promedio
            ));
        }
        return ranking;
    }

    private void parseRanking(String json, Map<Long, long[]> data, Map<Long, String[]> info, int metricIdx) {
        if (json == null) return;
        JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
        for (JsonElement el : arr) {
            JsonObject o = el.getAsJsonObject();
            long jugadorId = o.get("jugadorId").getAsLong();
            long total = o.has("total") && !o.get("total").isJsonNull() ? o.get("total").getAsLong() : 0;

            data.putIfAbsent(jugadorId, new long[]{0, 0, 0, 0});
            data.get(jugadorId)[metricIdx] = total;

            if (!info.containsKey(jugadorId)) {
                String nombre = o.has("nombre") && !o.get("nombre").isJsonNull() ? o.get("nombre").getAsString() : "";
                String apellidos = o.has("apellidos") && !o.get("apellidos").isJsonNull() ? o.get("apellidos").getAsString() : "";
                String dorsal = o.has("dorsal") && !o.get("dorsal").isJsonNull() ? String.valueOf(o.get("dorsal").getAsInt()) : "";
                String posicion = o.has("posicion") && !o.get("posicion").isJsonNull() ? o.get("posicion").getAsString() : "";
                info.put(jugadorId, new String[]{nombre, apellidos, dorsal, posicion});
            }
        }
    }
}

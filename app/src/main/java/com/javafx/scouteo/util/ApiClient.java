package com.javafx.scouteo.util;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.lang.reflect.Type;
import java.util.List;

public class ApiClient {

    private static final String BASE_URL = "https://scouteo-api-612681319622.europe-west1.run.app/api";
    private static ApiClient instancia;

    private final HttpClient httpClient;
    private final Gson gson;
    private String jwtToken;

    private ApiClient() {
        this.httpClient = HttpClient.newHttpClient();
        JsonDeserializer<LocalDate> dateDeserializer = (json, type, ctx) -> {
            if (json.isJsonArray()) {
                JsonArray a = json.getAsJsonArray();
                return LocalDate.of(a.get(0).getAsInt(), a.get(1).getAsInt(), a.get(2).getAsInt());
            }
            return LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE);
        };
        JsonSerializer<LocalDate> dateSerializer = (src, type, ctx) ->
            new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE));

        JsonDeserializer<LocalDateTime> dtDeserializer = (json, type, ctx) -> {
            if (json.isJsonArray()) {
                JsonArray a = json.getAsJsonArray();
                return LocalDateTime.of(
                    a.get(0).getAsInt(), a.get(1).getAsInt(), a.get(2).getAsInt(),
                    a.size() > 3 ? a.get(3).getAsInt() : 0,
                    a.size() > 4 ? a.get(4).getAsInt() : 0,
                    a.size() > 5 ? a.get(5).getAsInt() : 0
                );
            }
            return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        };
        JsonSerializer<LocalDateTime> dtSerializer = (src, type, ctx) ->
            new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, dateDeserializer)
            .registerTypeAdapter(LocalDate.class, dateSerializer)
            .registerTypeAdapter(LocalDateTime.class, dtDeserializer)
            .registerTypeAdapter(LocalDateTime.class, dtSerializer)
            .create();
    }

    public static ApiClient getInstance() {
        if (instancia == null) instancia = new ApiClient();
        return instancia;
    }

    public void setToken(String token) { this.jwtToken = token; }
    public String getToken() { return jwtToken; }
    public void clearToken() { this.jwtToken = null; }

    public boolean isDisponible() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/me"))
                .header("Authorization", "Bearer test")
                .GET().build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public String get(String path) {
        return request("GET", path, null);
    }

    public String post(String path, Object body) {
        return request("POST", path, gson.toJson(body));
    }

    public String put(String path, Object body) {
        return request("PUT", path, gson.toJson(body));
    }

    public boolean delete(String path) {
        String result = request("DELETE", path, null);
        return result != null;
    }

    private String request(String method, String path, String bodyJson) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json");
            if (jwtToken != null) builder.header("Authorization", "Bearer " + jwtToken);

            switch (method) {
                case "GET"    -> builder.GET();
                case "DELETE" -> builder.DELETE();
                default       -> builder.method(method, HttpRequest.BodyPublishers.ofString(bodyJson != null ? bodyJson : "{}"));
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));
            if (response.statusCode() >= 200 && response.statusCode() < 300) return response.body();
            System.err.println("API " + method + " " + path + " -> " + response.statusCode() + ": " + response.body());
            return null;
        } catch (Exception e) {
            System.err.println("HTTP error: " + e.getMessage());
            return null;
        }
    }

    public Gson getGson() { return gson; }

    public <T> T fromJson(String json, Class<T> cls) {
        if (json == null) return null;
        return gson.fromJson(json, cls);
    }

    public <T> List<T> fromJsonList(String json, Class<T> cls) {
        if (json == null) return List.of();
        Type listType = TypeToken.getParameterized(List.class, cls).getType();
        List<T> result = gson.fromJson(json, listType);
        return result != null ? result : List.of();
    }
}

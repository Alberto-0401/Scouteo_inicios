package com.javafx.scouteo.model;

import java.time.LocalDateTime;

public class Equipo {
    private Integer id;
    private String nombre;
    private String categoria; // 'benjamin','alevin','infantil','cadete','juvenil','senior','veteranos'
    private String temporada; // Ej: 2024-25
    private String campo;
    private String escudoUrl;
    private boolean activo;
    private Integer clubId;
    private LocalDateTime createdAt;

    public Equipo() {}

    public Equipo(Integer id, String nombre, String categoria, String temporada) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.temporada = temporada;
        this.activo = true;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getTemporada() { return temporada; }
    public void setTemporada(String temporada) { this.temporada = temporada; }

    public String getCampo() { return campo; }
    public void setCampo(String campo) { this.campo = campo; }

    public String getEscudoUrl() { return escudoUrl; }
    public void setEscudoUrl(String escudoUrl) { this.escudoUrl = escudoUrl; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Integer getClubId() { return clubId; }
    public void setClubId(Integer clubId) { this.clubId = clubId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCategoriaDisplay() {
        if (categoria == null) return "";
        switch (categoria) {
            case "benjamin":  return "Benjamín";
            case "alevin":    return "Alevín";
            case "infantil":  return "Infantil";
            case "cadete":    return "Cadete";
            case "juvenil":   return "Juvenil";
            case "senior":    return "Sénior";
            case "veteranos": return "Veteranos";
            default: return categoria;
        }
    }

    @Override
    public String toString() {
        return nombre + " (" + getCategoriaDisplay() + " - " + temporada + ")";
    }
}

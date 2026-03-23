package com.javafx.scouteo.model;

import java.time.LocalDateTime;

public class Entrenamiento {
    private Integer id;
    private Integer equipoId;
    private LocalDateTime fechaHora;
    private Integer duracionMin;
    private String tipo; // 'fisico','tactico','tecnico','mixto','partido_entrenamiento'
    private String intensidad; // 'baja','media','alta'
    private String objetivos;
    private String observaciones;
    // Campo join
    private String nombreEquipo;

    public Entrenamiento() {}

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getEquipoId() { return equipoId; }
    public void setEquipoId(Integer equipoId) { this.equipoId = equipoId; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public Integer getDuracionMin() { return duracionMin; }
    public void setDuracionMin(Integer duracionMin) { this.duracionMin = duracionMin; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getIntensidad() { return intensidad; }
    public void setIntensidad(String intensidad) { this.intensidad = intensidad; }

    public String getObjetivos() { return objetivos; }
    public void setObjetivos(String objetivos) { this.objetivos = objetivos; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getNombreEquipo() { return nombreEquipo; }
    public void setNombreEquipo(String nombreEquipo) { this.nombreEquipo = nombreEquipo; }

    public String getTipoDisplay() {
        if (tipo == null) return "";
        switch (tipo) {
            case "fisico":               return "Fisico";
            case "tactico":              return "Tactico";
            case "tecnico":              return "Tecnico";
            case "mixto":                return "Mixto";
            case "partido_entrenamiento": return "Partido de entrenamiento";
            default: return tipo;
        }
    }

    public String getIntensidadDisplay() {
        if (intensidad == null) return "";
        switch (intensidad) {
            case "baja":  return "Baja";
            case "media": return "Media";
            case "alta":  return "Alta";
            default: return intensidad;
        }
    }
}

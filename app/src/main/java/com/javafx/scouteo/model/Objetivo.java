package com.javafx.scouteo.model;

import java.time.LocalDate;

public class Objetivo {
    private Integer id;
    private Integer equipoId;
    private Integer jugadorId;
    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaLimite;
    private String prioridad; // 'baja','media','alta'
    private String estado;    // 'pendiente','en_progreso','completado','cancelado'
    private Integer progresoPct;
    private String observaciones;
    // Campos join
    private String nombreEquipo;
    private String nombreJugador;

    public Objetivo() {}

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getEquipoId() { return equipoId; }
    public void setEquipoId(Integer equipoId) { this.equipoId = equipoId; }

    public Integer getJugadorId() { return jugadorId; }
    public void setJugadorId(Integer jugadorId) { this.jugadorId = jugadorId; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(LocalDate fechaLimite) { this.fechaLimite = fechaLimite; }

    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Integer getProgresoPct() { return progresoPct; }
    public void setProgresoPct(Integer progresoPct) { this.progresoPct = progresoPct; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getNombreEquipo() { return nombreEquipo; }
    public void setNombreEquipo(String nombreEquipo) { this.nombreEquipo = nombreEquipo; }

    public String getNombreJugador() { return nombreJugador; }
    public void setNombreJugador(String nombreJugador) { this.nombreJugador = nombreJugador; }

    public String getTipo() {
        return jugadorId != null ? "Individual" : "Equipo";
    }

    public String getObjetivoLabel() {
        return jugadorId != null ? nombreJugador : nombreEquipo;
    }

    public String getEstadoDisplay() {
        if (estado == null) return "";
        switch (estado) {
            case "pendiente":    return "Pendiente";
            case "en_progreso":  return "En progreso";
            case "completado":   return "Completado";
            case "cancelado":    return "Cancelado";
            default: return estado;
        }
    }

    public String getPrioridadDisplay() {
        if (prioridad == null) return "";
        switch (prioridad) {
            case "baja":  return "Baja";
            case "media": return "Media";
            case "alta":  return "Alta";
            default: return prioridad;
        }
    }
}

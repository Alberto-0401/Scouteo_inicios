package com.javafx.scouteo.model;

import java.time.LocalDate;

public class HistorialMedico {
    private Integer id;
    private Integer jugadorId;
    private String tipoLesion;
    private String zonaAfectada;
    private LocalDate fechaLesion;
    private LocalDate fechaRecuperacionEst;
    private LocalDate fechaAlta;
    private String observaciones;
    // Campos calculados/join
    private String nombreJugador;

    public HistorialMedico() {}

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getJugadorId() { return jugadorId; }
    public void setJugadorId(Integer jugadorId) { this.jugadorId = jugadorId; }

    public String getTipoLesion() { return tipoLesion; }
    public void setTipoLesion(String tipoLesion) { this.tipoLesion = tipoLesion; }

    public String getZonaAfectada() { return zonaAfectada; }
    public void setZonaAfectada(String zonaAfectada) { this.zonaAfectada = zonaAfectada; }

    public LocalDate getFechaLesion() { return fechaLesion; }
    public void setFechaLesion(LocalDate fechaLesion) { this.fechaLesion = fechaLesion; }

    public LocalDate getFechaRecuperacionEst() { return fechaRecuperacionEst; }
    public void setFechaRecuperacionEst(LocalDate fechaRecuperacionEst) { this.fechaRecuperacionEst = fechaRecuperacionEst; }

    public LocalDate getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(LocalDate fechaAlta) { this.fechaAlta = fechaAlta; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getNombreJugador() { return nombreJugador; }
    public void setNombreJugador(String nombreJugador) { this.nombreJugador = nombreJugador; }

    public Integer getDiasFuera() {
        if (fechaLesion != null && fechaAlta != null) {
            return (int) (fechaAlta.toEpochDay() - fechaLesion.toEpochDay());
        }
        return null;
    }

    public String getEstadoLesion() {
        if (fechaAlta != null) return "Recuperado";
        if (fechaRecuperacionEst != null && LocalDate.now().isAfter(fechaRecuperacionEst)) return "Retrasado";
        return "En recuperacion";
    }
}

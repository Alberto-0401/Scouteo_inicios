package com.javafx.scouteo.model;

import java.time.LocalDate;

public class Partido {
    private Integer idPartido;
    private LocalDate fechaPartido;
    private String rival;
    private String resultado;
    private String localVisitante;
    private String observaciones;

    public Partido() {
    }

    public Partido(Integer idPartido, LocalDate fechaPartido, String rival, String resultado, String localVisitante, String observaciones) {
        this.idPartido = idPartido;
        this.fechaPartido = fechaPartido;
        this.rival = rival;
        this.resultado = resultado;
        this.localVisitante = localVisitante;
        this.observaciones = observaciones;
    }

    // Getters y Setters
    public Integer getIdPartido() {
        return idPartido;
    }

    public void setIdPartido(Integer idPartido) {
        this.idPartido = idPartido;
    }

    public LocalDate getFechaPartido() {
        return fechaPartido;
    }

    public void setFechaPartido(LocalDate fechaPartido) {
        this.fechaPartido = fechaPartido;
    }

    public String getRival() {
        return rival;
    }

    public void setRival(String rival) {
        this.rival = rival;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getLocalVisitante() {
        return localVisitante;
    }

    public void setLocalVisitante(String localVisitante) {
        this.localVisitante = localVisitante;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}

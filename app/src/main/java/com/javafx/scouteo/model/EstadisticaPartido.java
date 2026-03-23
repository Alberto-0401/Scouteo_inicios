package com.javafx.scouteo.model;

public class EstadisticaPartido {
    private Integer idEstadistica;
    private Integer goles;
    private Integer asistencias;
    private Integer paradas;  // Solo para porteros
    private Integer recuperaciones;  // Para defensas y medios
    private Integer tarjetasAmarillas;
    private Integer tarjetasRojas;
    private Integer minutosJugados;
    private Double valoracion;
    private String observaciones;

    public EstadisticaPartido() {
    }

    public EstadisticaPartido(Integer idEstadistica, Integer goles, Integer asistencias,
                            Integer paradas, Integer recuperaciones, Integer tarjetasAmarillas,
                            Integer tarjetasRojas, Integer minutosJugados, Double valoracion,
                            String observaciones) {
        this.idEstadistica = idEstadistica;
        this.goles = goles;
        this.asistencias = asistencias;
        this.paradas = paradas;
        this.recuperaciones = recuperaciones;
        this.tarjetasAmarillas = tarjetasAmarillas;
        this.tarjetasRojas = tarjetasRojas;
        this.minutosJugados = minutosJugados;
        this.valoracion = valoracion;
        this.observaciones = observaciones;
    }

    // Getters y Setters
    public Integer getIdEstadistica() {
        return idEstadistica;
    }

    public void setIdEstadistica(Integer idEstadistica) {
        this.idEstadistica = idEstadistica;
    }

    public Integer getGoles() {
        return goles;
    }

    public void setGoles(Integer goles) {
        this.goles = goles;
    }

    public Integer getAsistencias() {
        return asistencias;
    }

    public void setAsistencias(Integer asistencias) {
        this.asistencias = asistencias;
    }

    public Integer getParadas() {
        return paradas;
    }

    public void setParadas(Integer paradas) {
        this.paradas = paradas;
    }

    public Integer getRecuperaciones() {
        return recuperaciones;
    }

    public void setRecuperaciones(Integer recuperaciones) {
        this.recuperaciones = recuperaciones;
    }

    public Integer getTarjetasAmarillas() {
        return tarjetasAmarillas;
    }

    public void setTarjetasAmarillas(Integer tarjetasAmarillas) {
        this.tarjetasAmarillas = tarjetasAmarillas;
    }

    public Integer getTarjetasRojas() {
        return tarjetasRojas;
    }

    public void setTarjetasRojas(Integer tarjetasRojas) {
        this.tarjetasRojas = tarjetasRojas;
    }

    public Integer getMinutosJugados() {
        return minutosJugados;
    }

    public void setMinutosJugados(Integer minutosJugados) {
        this.minutosJugados = minutosJugados;
    }

    public Double getValoracion() {
        return valoracion;
    }

    public void setValoracion(Double valoracion) {
        this.valoracion = valoracion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}

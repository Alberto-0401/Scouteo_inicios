package com.javafx.scouteo.model;

public class JugadorPartido {
    private Integer idJugadorPartido;
    private Integer idJugador;
    private Integer idPartido;
    private Integer idEstadistica;
    private Boolean titular;
    private Boolean convocado;

    public JugadorPartido() {
    }

    public JugadorPartido(Integer idJugadorPartido, Integer idJugador, Integer idPartido,
                          Integer idEstadistica, Boolean titular, Boolean convocado) {
        this.idJugadorPartido = idJugadorPartido;
        this.idJugador = idJugador;
        this.idPartido = idPartido;
        this.idEstadistica = idEstadistica;
        this.titular = titular;
        this.convocado = convocado;
    }

    public Integer getIdJugadorPartido() {
        return idJugadorPartido;
    }

    public void setIdJugadorPartido(Integer idJugadorPartido) {
        this.idJugadorPartido = idJugadorPartido;
    }

    public Integer getIdJugador() {
        return idJugador;
    }

    public void setIdJugador(Integer idJugador) {
        this.idJugador = idJugador;
    }

    public Integer getIdPartido() {
        return idPartido;
    }

    public void setIdPartido(Integer idPartido) {
        this.idPartido = idPartido;
    }

    public Integer getIdEstadistica() {
        return idEstadistica;
    }

    public void setIdEstadistica(Integer idEstadistica) {
        this.idEstadistica = idEstadistica;
    }

    public Boolean getTitular() {
        return titular;
    }

    public void setTitular(Boolean titular) {
        this.titular = titular;
    }

    public Boolean getConvocado() {
        return convocado;
    }

    public void setConvocado(Boolean convocado) {
        this.convocado = convocado;
    }
}

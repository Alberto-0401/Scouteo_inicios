package com.javafx.scouteo.model;

public class Configuracion {
    private Integer idConfig;
    private String nombreClub;
    private String localidad;
    private String presidente;
    private String email;
    private String telefono;
    private String escudo;
    private String temporadaActual;

    public Configuracion() {
    }

    // Getters y Setters
    public Integer getIdConfig() {
        return idConfig;
    }

    public void setIdConfig(Integer idConfig) {
        this.idConfig = idConfig;
    }

    public String getNombreClub() {
        return nombreClub;
    }

    public void setNombreClub(String nombreClub) {
        this.nombreClub = nombreClub;
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    public String getPresidente() {
        return presidente;
    }

    public void setPresidente(String presidente) {
        this.presidente = presidente;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEscudo() {
        return escudo;
    }

    public void setEscudo(String escudo) {
        this.escudo = escudo;
    }

    public String getTemporadaActual() {
        return temporadaActual;
    }

    public void setTemporadaActual(String temporadaActual) {
        this.temporadaActual = temporadaActual;
    }
}

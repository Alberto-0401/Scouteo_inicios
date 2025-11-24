package com.javafx.scouteo.model;

import java.time.LocalDate;

public class Jugador {
    private Integer id;
    private String nombre;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private Integer dorsal;
    private String posicion;
    private Double altura;
    private Double peso;
    private String categoria;
    private byte[] foto;
    private String estado;
    private Integer edad; // Calculado

    public Jugador() {
    }

    public Jugador(Integer id, String nombre, String apellidos, LocalDate fechaNacimiento,
                   Integer dorsal, String posicion, Double altura, Double peso,
                   String categoria, byte[] foto, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.fechaNacimiento = fechaNacimiento;
        this.dorsal = dorsal;
        this.posicion = posicion;
        this.altura = altura;
        this.peso = peso;
        this.categoria = categoria;
        this.foto = foto;
        this.estado = estado;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public Integer getDorsal() {
        return dorsal;
    }

    public void setDorsal(Integer dorsal) {
        this.dorsal = dorsal;
    }

    public String getPosicion() {
        return posicion;
    }

    public void setPosicion(String posicion) {
        this.posicion = posicion;
    }

    public Double getAltura() {
        return altura;
    }

    public void setAltura(Double altura) {
        this.altura = altura;
    }

    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public byte[] getFoto() {
        return foto;
    }

    public void setFoto(byte[] foto) {
        this.foto = foto;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    // Método para obtener nombre completo
    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }
}

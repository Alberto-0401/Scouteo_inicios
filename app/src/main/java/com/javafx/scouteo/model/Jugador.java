package com.javafx.scouteo.model;

import java.time.LocalDate;
import java.time.Period;

public class Jugador {
    private Integer id;
    private Integer usuarioId;      // FK opcional a usuarios
    private Integer equipoId;       // FK a equipos (obligatorio)
    private String nombre;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private Integer dorsal;
    private String posicion;        // enum completo del nuevo esquema
    private String piernaDominante; // 'derecha','izquierda','ambidiestro'
    private Integer alturaCm;
    private Double pesoKg;
    private String fotoUrl;         // URL en lugar de blob
    private String contactoEmergencia;
    private String telefonoEmergencia;
    private LocalDate fechaAlta;
    private String estado;          // 'activo','lesionado','sancionado','baja'
    private String observaciones;
    // Campos calculados/join
    private Integer edad;
    private String nombreEquipo;

    public Jugador() {}

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public Integer getEquipoId() { return equipoId; }
    public void setEquipoId(Integer equipoId) { this.equipoId = equipoId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
        if (fechaNacimiento != null) {
            this.edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();
        }
    }

    public Integer getDorsal() { return dorsal; }
    public void setDorsal(Integer dorsal) { this.dorsal = dorsal; }

    public String getPosicion() { return posicion; }
    public void setPosicion(String posicion) { this.posicion = posicion; }

    public String getPiernaDominante() { return piernaDominante; }
    public void setPiernaDominante(String piernaDominante) { this.piernaDominante = piernaDominante; }

    public Integer getAlturaCm() { return alturaCm; }
    public void setAlturaCm(Integer alturaCm) { this.alturaCm = alturaCm; }

    public Double getPesoKg() { return pesoKg; }
    public void setPesoKg(Double pesoKg) { this.pesoKg = pesoKg; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public String getContactoEmergencia() { return contactoEmergencia; }
    public void setContactoEmergencia(String contactoEmergencia) { this.contactoEmergencia = contactoEmergencia; }

    public String getTelefonoEmergencia() { return telefonoEmergencia; }
    public void setTelefonoEmergencia(String telefonoEmergencia) { this.telefonoEmergencia = telefonoEmergencia; }

    public LocalDate getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(LocalDate fechaAlta) { this.fechaAlta = fechaAlta; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }

    public String getNombreEquipo() { return nombreEquipo; }
    public void setNombreEquipo(String nombreEquipo) { this.nombreEquipo = nombreEquipo; }

    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }

    public String getPosicionDisplay() {
        if (posicion == null) return "";
        switch (posicion) {
            case "portero":            return "Portero";
            case "defensa_central":    return "Defensa Central";
            case "lateral_derecho":    return "Lateral Derecho";
            case "lateral_izquierdo":  return "Lateral Izquierdo";
            case "mediocentro":        return "Mediocentro";
            case "medio_derecho":      return "Medio Derecho";
            case "medio_izquierdo":    return "Medio Izquierdo";
            case "mediapunta":         return "Mediapunta";
            case "extremo_derecho":    return "Extremo Derecho";
            case "extremo_izquierdo":  return "Extremo Izquierdo";
            case "delantero_centro":   return "Delantero Centro";
            default: return posicion;
        }
    }

    public String getGrupoPosicion() {
        if (posicion == null) return "DEL";
        switch (posicion) {
            case "portero": return "POR";
            case "defensa_central": case "lateral_derecho": case "lateral_izquierdo": return "DEF";
            case "mediocentro": case "medio_derecho": case "medio_izquierdo": case "mediapunta": return "MED";
            default: return "DEL";
        }
    }
}

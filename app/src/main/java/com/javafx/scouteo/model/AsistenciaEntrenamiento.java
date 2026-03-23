package com.javafx.scouteo.model;

public class AsistenciaEntrenamiento {
    private Integer id;
    private Integer entrenamientoId;
    private Integer jugadorId;
    private String jugadorNombre;
    private String nombreJugador; // alias for compatibility
    private int dorsal;
    private String estado;   // asistio | falta_justificada | falta_injustificada
    private String motivo;

    public AsistenciaEntrenamiento() {}

    public AsistenciaEntrenamiento(int jugadorId, String jugadorNombre, int dorsal, String estado, String motivo) {
        this.jugadorId = jugadorId;
        this.jugadorNombre = jugadorNombre;
        this.nombreJugador = jugadorNombre;
        this.dorsal = dorsal;
        this.estado = estado;
        this.motivo = motivo;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getEntrenamientoId() { return entrenamientoId; }
    public void setEntrenamientoId(Integer entrenamientoId) { this.entrenamientoId = entrenamientoId; }

    public Integer getJugadorId() { return jugadorId; }
    public void setJugadorId(Integer jugadorId) { this.jugadorId = jugadorId; }

    public String getJugadorNombre() { return jugadorNombre; }
    public void setJugadorNombre(String jugadorNombre) {
        this.jugadorNombre = jugadorNombre;
        this.nombreJugador = jugadorNombre;
    }

    public String getNombreJugador() { return nombreJugador; }
    public void setNombreJugador(String nombreJugador) {
        this.nombreJugador = nombreJugador;
        this.jugadorNombre = nombreJugador;
    }

    public int getDorsal() { return dorsal; }
    public void setDorsal(int dorsal) { this.dorsal = dorsal; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getEstadoDisplay() {
        if (estado == null) return "No registrado";
        return switch (estado) {
            case "asistio" -> "Asistio";
            case "falta_justificada" -> "Falta justificada";
            case "falta_injustificada" -> "Falta injustificada";
            default -> estado;
        };
    }
}

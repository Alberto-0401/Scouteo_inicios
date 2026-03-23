package com.javafx.scouteo.model;

public class EventoPartido {
    private int id;
    private int partidoId;
    private int minuto;
    private String tipo;
    private Integer jugadorId;
    private String jugadorNombre;
    private Integer jugador2Id;
    private String jugador2Nombre;
    private String descripcion;

    public EventoPartido() {}

    public EventoPartido(int partidoId, int minuto, String tipo, Integer jugadorId, Integer jugador2Id, String descripcion) {
        this.partidoId = partidoId;
        this.minuto = minuto;
        this.tipo = tipo;
        this.jugadorId = jugadorId;
        this.jugador2Id = jugador2Id;
        this.descripcion = descripcion;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPartidoId() { return partidoId; }
    public void setPartidoId(int partidoId) { this.partidoId = partidoId; }

    public int getMinuto() { return minuto; }
    public void setMinuto(int minuto) { this.minuto = minuto; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Integer getJugadorId() { return jugadorId; }
    public void setJugadorId(Integer jugadorId) { this.jugadorId = jugadorId; }

    public String getJugadorNombre() { return jugadorNombre; }
    public void setJugadorNombre(String jugadorNombre) { this.jugadorNombre = jugadorNombre; }

    public Integer getJugador2Id() { return jugador2Id; }
    public void setJugador2Id(Integer jugador2Id) { this.jugador2Id = jugador2Id; }

    public String getJugador2Nombre() { return jugador2Nombre; }
    public void setJugador2Nombre(String jugador2Nombre) { this.jugador2Nombre = jugador2Nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTipoDisplay() {
        return switch (tipo) {
            case "gol" -> "Gol";
            case "gol_penal" -> "Gol de Penal";
            case "gol_propio" -> "Gol en Propia";
            case "tarjeta_amarilla" -> "Tarjeta Amarilla";
            case "tarjeta_roja" -> "Tarjeta Roja";
            case "doble_amarilla" -> "Doble Amarilla";
            case "cambio" -> "Cambio";
            case "lesion" -> "Lesion";
            case "penalti_fallado" -> "Penalti Fallado";
            default -> "Otro";
        };
    }
}

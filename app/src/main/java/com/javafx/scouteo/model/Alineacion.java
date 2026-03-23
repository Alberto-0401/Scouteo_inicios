package com.javafx.scouteo.model;

public class Alineacion {
    private Integer id;
    private Integer partidoId;
    private Integer jugadorId;
    private Integer minutoEntrada;  // default 0
    private Integer minutoSalida;   // default 90
    private String posicionPartido;
    private Integer goles;
    private Integer asistencias;
    private Integer tarjetasAmarillas;
    private Integer tarjetasRojas;
    // Estadisticas portero
    private Integer paradas;
    private Integer golesEncajados;
    private Boolean porteriaCero;
    // Valoracion
    private Double valoracion;
    private String notaEntrenador;
    // Campos join
    private String nombreJugador;
    private String rival;

    public Alineacion() {
        this.minutoEntrada = 0;
        this.minutoSalida = 90;
        this.goles = 0;
        this.asistencias = 0;
        this.tarjetasAmarillas = 0;
        this.tarjetasRojas = 0;
        this.paradas = 0;
        this.golesEncajados = 0;
        this.porteriaCero = false;
    }

    public Integer getMinutosJugados() {
        if (minutoEntrada != null && minutoSalida != null) {
            return minutoSalida - minutoEntrada;
        }
        return 0;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getPartidoId() { return partidoId; }
    public void setPartidoId(Integer partidoId) { this.partidoId = partidoId; }

    public Integer getJugadorId() { return jugadorId; }
    public void setJugadorId(Integer jugadorId) { this.jugadorId = jugadorId; }

    public Integer getMinutoEntrada() { return minutoEntrada; }
    public void setMinutoEntrada(Integer minutoEntrada) { this.minutoEntrada = minutoEntrada; }

    public Integer getMinutoSalida() { return minutoSalida; }
    public void setMinutoSalida(Integer minutoSalida) { this.minutoSalida = minutoSalida; }

    public String getPosicionPartido() { return posicionPartido; }
    public void setPosicionPartido(String posicionPartido) { this.posicionPartido = posicionPartido; }

    public Integer getGoles() { return goles; }
    public void setGoles(Integer goles) { this.goles = goles; }

    public Integer getAsistencias() { return asistencias; }
    public void setAsistencias(Integer asistencias) { this.asistencias = asistencias; }

    public Integer getTarjetasAmarillas() { return tarjetasAmarillas; }
    public void setTarjetasAmarillas(Integer tarjetasAmarillas) { this.tarjetasAmarillas = tarjetasAmarillas; }

    public Integer getTarjetasRojas() { return tarjetasRojas; }
    public void setTarjetasRojas(Integer tarjetasRojas) { this.tarjetasRojas = tarjetasRojas; }

    public Integer getParadas() { return paradas; }
    public void setParadas(Integer paradas) { this.paradas = paradas; }

    public Integer getGolesEncajados() { return golesEncajados; }
    public void setGolesEncajados(Integer golesEncajados) { this.golesEncajados = golesEncajados; }

    public Boolean getPorteriaCero() { return porteriaCero; }
    public void setPorteriaCero(Boolean porteriaCero) { this.porteriaCero = porteriaCero; }

    public Double getValoracion() { return valoracion; }
    public void setValoracion(Double valoracion) { this.valoracion = valoracion; }

    public String getNotaEntrenador() { return notaEntrenador; }
    public void setNotaEntrenador(String notaEntrenador) { this.notaEntrenador = notaEntrenador; }

    public String getNombreJugador() { return nombreJugador; }
    public void setNombreJugador(String nombreJugador) { this.nombreJugador = nombreJugador; }

    public String getRival() { return rival; }
    public void setRival(String rival) { this.rival = rival; }
}

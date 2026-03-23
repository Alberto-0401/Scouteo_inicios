package com.javafx.scouteo.model;

import java.time.LocalDateTime;

public class Partido {
    private Integer id;
    private Integer equipoId;
    private LocalDateTime fechaHora;
    private String rival;
    private String tipo;            // 'local','visitante','neutral'
    private String campo;
    private String superficie;      // 'cesped_natural','cesped_artificial','tierra','sala'
    private String competicion;
    private String temporada;
    private String formacion;
    private Integer golesFavor;
    private Integer golesContra;
    private Integer posesionPct;
    private Integer tirosTotales;
    private Integer tirosAPuerta;
    private Integer corners;
    private Integer faltas;
    private String notas;
    // Campo join
    private String nombreEquipo;

    public Partido() {}

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getEquipoId() { return equipoId; }
    public void setEquipoId(Integer equipoId) { this.equipoId = equipoId; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getRival() { return rival; }
    public void setRival(String rival) { this.rival = rival; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getCampo() { return campo; }
    public void setCampo(String campo) { this.campo = campo; }

    public String getSuperficie() { return superficie; }
    public void setSuperficie(String superficie) { this.superficie = superficie; }

    public String getCompeticion() { return competicion; }
    public void setCompeticion(String competicion) { this.competicion = competicion; }

    public String getTemporada() { return temporada; }
    public void setTemporada(String temporada) { this.temporada = temporada; }

    public String getFormacion() { return formacion; }
    public void setFormacion(String formacion) { this.formacion = formacion; }

    public Integer getGolesFavor() { return golesFavor; }
    public void setGolesFavor(Integer golesFavor) { this.golesFavor = golesFavor; }

    public Integer getGolesContra() { return golesContra; }
    public void setGolesContra(Integer golesContra) { this.golesContra = golesContra; }

    public Integer getPosesionPct() { return posesionPct; }
    public void setPosesionPct(Integer posesionPct) { this.posesionPct = posesionPct; }

    public Integer getTirosTotales() { return tirosTotales; }
    public void setTirosTotales(Integer tirosTotales) { this.tirosTotales = tirosTotales; }

    public Integer getTirosAPuerta() { return tirosAPuerta; }
    public void setTirosAPuerta(Integer tirosAPuerta) { this.tirosAPuerta = tirosAPuerta; }

    public Integer getCorners() { return corners; }
    public void setCorners(Integer corners) { this.corners = corners; }

    public Integer getFaltas() { return faltas; }
    public void setFaltas(Integer faltas) { this.faltas = faltas; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public String getNombreEquipo() { return nombreEquipo; }
    public void setNombreEquipo(String nombreEquipo) { this.nombreEquipo = nombreEquipo; }

    /** Resultado calculado como cadena "X-Y" */
    public String getResultado() {
        if (golesFavor == null || golesContra == null) return "-";
        return golesFavor + "-" + golesContra;
    }

    /** Resultado: 'victoria', 'empate', 'derrota' */
    public String getResultadoTipo() {
        if (golesFavor == null || golesContra == null) return "pendiente";
        if (golesFavor > golesContra) return "victoria";
        if (golesFavor.equals(golesContra)) return "empate";
        return "derrota";
    }

    public String getTipoDisplay() {
        if (tipo == null) return "";
        switch (tipo) {
            case "local":     return "Local";
            case "visitante": return "Visitante";
            case "neutral":   return "Neutral";
            default: return tipo;
        }
    }
}

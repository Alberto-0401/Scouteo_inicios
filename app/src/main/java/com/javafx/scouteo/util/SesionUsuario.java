package com.javafx.scouteo.util;

import com.javafx.scouteo.model.Usuario;

public class SesionUsuario {

    private static SesionUsuario instancia;
    private Usuario usuarioActual;
    private String jwtToken;
    private Integer equipoIdSesion;
    private Integer jugadorIdSesion;

    private SesionUsuario() {}

    public static SesionUsuario getInstance() {
        if (instancia == null) {
            instancia = new SesionUsuario();
        }
        return instancia;
    }

    public void iniciarSesion(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    public void setJwtToken(String token) {
        this.jwtToken = token;
        ApiClient.getInstance().setToken(token);
    }
    public String getJwtToken() { return jwtToken; }

    public Integer getEquipoId() { return equipoIdSesion; }
    public void setEquipoId(Integer id) { this.equipoIdSesion = id; }

    public Integer getJugadorId() { return jugadorIdSesion; }
    public void setJugadorId(Integer id) { this.jugadorIdSesion = id; }

    public void cerrarSesion() {
        this.usuarioActual = null;
        ApiClient.getInstance().clearToken();
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public boolean haySesionActiva() {
        return usuarioActual != null;
    }

    public boolean esDirectiva() {
        return usuarioActual != null && "directiva".equals(usuarioActual.getRol());
    }

    public boolean esEntrenador() {
        return usuarioActual != null && "entrenador".equals(usuarioActual.getRol());
    }

    public boolean esJugador() {
        return usuarioActual != null && "jugador".equals(usuarioActual.getRol());
    }
}

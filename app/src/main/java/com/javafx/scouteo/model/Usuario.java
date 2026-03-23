package com.javafx.scouteo.model;

import java.time.LocalDateTime;

public class Usuario {
    private Integer id;
    private String email;
    private String passwordHash;
    private Integer clubId;
    private String rol; // 'directiva', 'entrenador', 'jugador'
    private String nombre;
    private String apellidos;
    private String telefono;
    private String fotoUrl;
    private boolean activo;
    private LocalDateTime ultimoAcceso;

    public Usuario() {}

    public Usuario(Integer id, String email, String rol, String nombre, String apellidos) {
        this.id = id;
        this.email = email;
        this.rol = rol;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.activo = true;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getClubId() { return clubId; }
    public void setClubId(Integer clubId) { this.clubId = clubId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(LocalDateTime ultimoAcceso) { this.ultimoAcceso = ultimoAcceso; }

    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }

    public String getRolDisplay() {
        switch (rol) {
            case "directiva": return "Directiva";
            case "entrenador": return "Entrenador";
            case "jugador": return "Jugador";
            default: return rol;
        }
    }
}

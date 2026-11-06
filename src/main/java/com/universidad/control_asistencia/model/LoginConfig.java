package com.universidad.control_asistencia.model;

import jakarta.persistence.*;

@Entity
@Table(name = "login_config")
public class LoginConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rol", unique = true, nullable = false)
    private String rol; // Ej: "estudiante" o "profesor"

    @Column(name = "login_habilitado")
    private boolean loginHabilitado;

    public LoginConfig() {
    }

    public LoginConfig(String rol, boolean loginHabilitado) {
        this.rol = rol;
        this.loginHabilitado = loginHabilitado;
    }

    public Long getId() {
        return id;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isLoginHabilitado() {
        return loginHabilitado;
    }

    public void setLoginHabilitado(boolean loginHabilitado) {
        this.loginHabilitado = loginHabilitado;
    }
}

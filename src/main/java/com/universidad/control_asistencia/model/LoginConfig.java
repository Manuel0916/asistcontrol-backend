package com.universidad.control_asistencia.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/** Si el login está habilitado para un rol ("estudiante", "profesor"). Colección "login_config". */
@Document(collection = "login_config")
public class LoginConfig implements ConIdNumerico {

    @Id
    private Long id;

    @Indexed(unique = true)
    private String rol; // Ej: "estudiante" o "profesor"

    @Field("login_habilitado")
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

    public void setId(Long id) {
        this.id = id;
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

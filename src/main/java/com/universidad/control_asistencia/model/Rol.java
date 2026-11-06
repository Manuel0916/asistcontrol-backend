package com.universidad.control_asistencia.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

/** Rol (ROLE_ADMIN, ROLE_COORDINADOR, ROLE_ESTUDIANTE...). Colección "roles". */
@Document(collection = "roles")
public class Rol implements ConIdNumerico {

    @Id
    private Long id;

    private String nombre;

    // Constructor vacío
    public Rol() {}

    // Constructor con nombre
    public Rol(String nombre) {
        this.nombre = nombre;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    // Dos roles son iguales si tienen el mismo nombre (evita repetidos en el Set del usuario)
    @Override
    public boolean equals(Object o) {
        return o instanceof Rol r && Objects.equals(nombre, r.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(nombre);
    }
}

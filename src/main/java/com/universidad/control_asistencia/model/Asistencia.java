package com.universidad.control_asistencia.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * Registro de asistencia (colección "asistencias" de MongoDB).
 * Es la misma colección que lee el servicio de IA para entrenar el modelo:
 * historial + todo lo que se registra desde la app y la web.
 */
@Document(collection = "asistencias")
public class Asistencia implements ConIdNumerico {

    @Id
    private Long id;

    @Field("usuario_id")
    private Long usuarioId;

    @Indexed
    private String nombre;

    @Indexed
    private String clase;

    private String estado;

    // Mismo nombre de campo que el historial ("fechahora")
    @Field("fechahora")
    @Indexed
    private LocalDateTime fechaHora = LocalDateTime.now();

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getClase() {
        return clase;
    }

    public void setClase(String clase) {
        this.clase = clase;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
}

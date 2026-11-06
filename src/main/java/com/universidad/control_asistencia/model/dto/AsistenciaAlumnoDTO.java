package com.universidad.control_asistencia.model.dto;

public class AsistenciaAlumnoDTO {
    private String nombre;      // Alumno
    private String clase;       // Materia
    private String diaSemana;   // Día de la semana
    private String hora;        // Hora nominal (ej: "07:30 a. m.")
    private String mes;         // Mes
    private String estado;      // Resultado (predicción): Ausente / Presente
    private Double probabilidad; // Probabilidad de faltar (0 a 1), del modelo Random Forest
    private String nivel;        // BAJA / MEDIA / ALTA

    public AsistenciaAlumnoDTO() {
    }

    public AsistenciaAlumnoDTO(String nombre, String clase, String diaSemana, String hora, String mes, String estado) {
        this.nombre = nombre;
        this.clase = clase;
        this.diaSemana = diaSemana;
        this.hora = hora;
        this.mes = mes;
        this.estado = estado;
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

    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getMes() {
        return mes;
    }

    public void setMes(String mes) {
        this.mes = mes;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Double getProbabilidad() {
        return probabilidad;
    }

    public void setProbabilidad(Double probabilidad) {
        this.probabilidad = probabilidad;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }
}

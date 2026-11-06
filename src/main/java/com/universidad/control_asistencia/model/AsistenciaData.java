package com.universidad.control_asistencia.model;

public class AsistenciaData {
    private String nombre;      // Alumno
    private String clase;       // Materia
    private String diaSemana;   // Día de la semana
    private String hora;        // Hora nominal (ej: "07:30 a. m.")
    private String mes;         // Mes
    private String estado;      // Resultado (Presente, Tarde, Ausente)

    public AsistenciaData() {
    }

    public AsistenciaData(String nombre, String clase, String diaSemana, String hora, String mes, String estado) {
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
}

package com.universidad.control_asistencia.model.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

/**
 * Documento de la colección "asistencias" de MongoDB (historial que usa el modelo de IA).
 * Mismos campos que los datos históricos; "mysql_id" enlaza con la fila de MySQL
 * para poder borrarla en ambos lados.
 *
 * Getters y setters escritos a mano (antes dependían de Lombok y no se generaban).
 */
@Document(collection = "asistencias")
public class Asistenciamongo {

    @Id
    private String id;

    private String nombre;
    private String clase;
    private Date fechahora;

    @Field("dia_semana")
    private String diaSemana;

    private String hora;

    @Field("hora_num")
    private Double horaNum;

    private String mes;
    private String estado;

    @Field("mysql_id")
    private Long mysqlId;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getClase() { return clase; }
    public void setClase(String clase) { this.clase = clase; }

    public Date getFechahora() { return fechahora; }
    public void setFechahora(Date fechahora) { this.fechahora = fechahora; }

    public String getDiaSemana() { return diaSemana; }
    public void setDiaSemana(String diaSemana) { this.diaSemana = diaSemana; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public Double getHoraNum() { return horaNum; }
    public void setHoraNum(Double horaNum) { this.horaNum = horaNum; }

    public String getMes() { return mes; }
    public void setMes(String mes) { this.mes = mes; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Long getMysqlId() { return mysqlId; }
    public void setMysqlId(Long mysqlId) { this.mysqlId = mysqlId; }
}

package com.universidad.control_asistencia.interfaces;

import com.universidad.control_asistencia.model.Asistencia;

import java.time.LocalDateTime;
import java.util.List;

public interface AsistenciaInterface {
    Asistencia registrarAsistencia(Asistencia asistencia);
    List<Asistencia> obtenerTodas();
    List<Asistencia> obtenerPorClase(String clase);
    List<Asistencia> obtenerPorNombre(String nombre);
    List<Asistencia> obtenerPorRangoDeFechas(LocalDateTime inicio, LocalDateTime fin);
}
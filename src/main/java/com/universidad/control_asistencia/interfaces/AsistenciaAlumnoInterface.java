package com.universidad.control_asistencia.interfaces;

import com.universidad.control_asistencia.model.dto.AsistenciaAlumnoDTO;

import java.util.List;
import java.util.Map;

public interface AsistenciaAlumnoInterface {
    /** Opciones de los selectores (vienen del servicio de IA). */
    Map<String, List<String>> cargarOpciones();

    /** Predice si el alumno faltará; llena estado, probabilidad y nivel del DTO. */
    String predecir(AsistenciaAlumnoDTO dto);
}

package com.universidad.control_asistencia.interfaces;

import com.universidad.control_asistencia.model.dto.AsistenciaAlumnoDTO;

import java.util.List;
import java.util.Map;

public interface AsistenciaAlumnoInterface {
    /** Opciones de los selectores (alumnos y clases de MongoDB; días y meses fijos). */
    Map<String, List<String>> cargarOpciones();

    /** Predice si el alumno faltará; llena estado, probabilidad y nivel del DTO. */
    String predecir(AsistenciaAlumnoDTO dto);
}

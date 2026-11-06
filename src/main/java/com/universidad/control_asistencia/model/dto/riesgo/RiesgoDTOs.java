package com.universidad.control_asistencia.model.dto.riesgo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** DTOs de solo lectura del módulo de riesgo de inasistencia. */
public final class RiesgoDTOs {

    private RiesgoDTOs() {
    }

    public enum NivelRiesgo { VERDE, AMARILLO, ROJO }

    public enum NivelProbabilidad { BAJA, MEDIA, ALTA }

    /** Estado de un alumno en una materia (regla de inasistencia). */
    public record AlumnoRiesgoDTO(
            String alumno,
            String clase,
            int clasesRegistradas,
            int ausencias,
            int tardanzas,
            double porcentajeInasistencia,
            double porcentajeProyectado,
            int clasesSemestre,
            int faltasRestantesPermitidas,
            NivelRiesgo nivel
    ) {
    }

    public record ResumenRiesgoDTO(
            int totalAlumnoMateria,
            int rojo,
            int amarillo,
            int verde,
            double umbralPerdida,
            double umbralAlerta
    ) {
    }

    /** Serie mensual para el gráfico de líneas. */
    public record TendenciaDTO(List<String> meses, List<Double> porcentajeAusencia, List<Integer> registros) {
    }

    /** Matriz materia x día con % de ausencia (mapa de calor). */
    public record MapaCalorDTO(List<String> clases, List<String> dias, List<List<Double>> valores) {
    }

    public record PrediccionFaltaDTO(
            String alumno,
            String clase,
            LocalDate fecha,
            double probabilidadFalta,
            NivelProbabilidad nivel,
            double tasaHistorica,
            int ausenciasUltimas5
    ) {
    }

    public record ModeloMetricasDTO(
            String algoritmo,
            int instanciasEntrenamiento,
            double tasaBaseAusencia,
            double accuracy,
            double aucAusencia,
            double precisionAusencia,
            double recallAusencia,
            LocalDateTime entrenadoEn,
            String advertencia
    ) {
    }
}

package com.universidad.control_asistencia.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Parámetros del módulo de riesgo de inasistencia (prefijo app.riesgo en application.properties).
 */
@ConfigurationProperties(prefix = "app.riesgo")
public record RiesgoProperties(
        /** % de inasistencia a partir del cual se pierde la materia. */
        @DefaultValue("25") double umbralPerdida,
        /** % de inasistencia a partir del cual se muestra nivel amarillo (aviso temprano). */
        @DefaultValue("18") double umbralAlerta,
        /** Semanas del semestre; clases esperadas = días por semana del horario x semanas. */
        @DefaultValue("16") int semanasSemestre,
        /** Clases del semestre si la materia no tiene horario registrado. */
        @DefaultValue("48") int clasesSemestreDefecto,
        /** Probabilidad de falta desde la cual el nivel es ALTA. */
        @DefaultValue("0.35") double probAlta,
        /** Probabilidad de falta desde la cual el nivel es MEDIA. */
        @DefaultValue("0.28") double probMedia,
        /** Minutos que el modelo entrenado se reutiliza antes de reentrenar con datos nuevos. */
        @DefaultValue("30") int cacheModeloMinutos
) {
}

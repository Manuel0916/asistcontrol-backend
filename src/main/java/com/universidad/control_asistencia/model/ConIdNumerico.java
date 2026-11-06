package com.universidad.control_asistencia.model;

/**
 * Documentos de MongoDB que usan un id numérico (1, 2, 3...) en vez del ObjectId de Mongo.
 * Así la app Flutter y la web siguen usando los mismos ids que tenían con MySQL.
 * El número lo asigna {@link com.universidad.control_asistencia.config.IdNumericoCallback}.
 */
public interface ConIdNumerico {
    Long getId();

    void setId(Long id);
}

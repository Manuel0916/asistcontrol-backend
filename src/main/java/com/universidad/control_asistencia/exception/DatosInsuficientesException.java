package com.universidad.control_asistencia.exception;

/** No hay suficientes registros de asistencia para calcular o entrenar. */
public class DatosInsuficientesException extends RuntimeException {
    public DatosInsuficientesException(String mensaje) {
        super(mensaje);
    }
}

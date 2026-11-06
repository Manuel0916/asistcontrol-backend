package com.universidad.control_asistencia.exception;

/** Error al comunicarse con el servicio de IA en Python. */
public class IaNoDisponibleException extends RuntimeException {

    private final int status;

    public IaNoDisponibleException(String mensaje, int status) {
        super(mensaje);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}

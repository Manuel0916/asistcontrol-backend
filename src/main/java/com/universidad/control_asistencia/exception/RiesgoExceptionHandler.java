package com.universidad.control_asistencia.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

import com.universidad.control_asistencia.controller.RiesgoApiController;
import com.universidad.control_asistencia.exception.IaNoDisponibleException;

/** Manejo de errores del módulo de riesgo (no afecta a los demás controladores). */
@RestControllerAdvice(assignableTypes = RiesgoApiController.class)
public class RiesgoExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RiesgoExceptionHandler.class);

    public record ApiError(int status, String error, String mensaje, LocalDateTime fecha) {
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> solicitudInvalida(Exception ex) {
        String msg = ex instanceof MethodArgumentTypeMismatchException m
                ? "Parámetro inválido: " + m.getName() + " (formato esperado de fecha: yyyy-MM-dd)"
                : ex.getMessage();
        return build(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(DatosInsuficientesException.class)
    public ResponseEntity<ApiError> datosInsuficientes(DatosInsuficientesException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(IaNoDisponibleException.class)
    public ResponseEntity<ApiError> iaNoDisponible(IaNoDisponibleException ex) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> inesperado(Exception ex) {
        log.error("Error en módulo de riesgo", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al procesar la solicitud");
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status)
                .body(new ApiError(status.value(), status.getReasonPhrase(), mensaje, LocalDateTime.now()));
    }
}

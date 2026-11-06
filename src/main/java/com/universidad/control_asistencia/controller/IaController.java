package com.universidad.control_asistencia.controller;

import com.universidad.control_asistencia.exception.IaNoDisponibleException;
import com.universidad.control_asistencia.service.IaClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Puerta de entrada de la app móvil al modelo de IA (solo COORDINADOR, ver SecurityConfig).
 * La app nunca habla directo con Python: pasa por aquí, con su token JWT.
 */
@RestController
@RequestMapping("/api/ia")
public class IaController {

    private final IaClient ia;

    public IaController(IaClient ia) {
        this.ia = ia;
    }

    /** Métricas, matriz de confusión e importancia de variables. */
    @GetMapping("/modelo")
    public Map<String, Object> modelo() {
        return ia.get("/modelo");
    }

    /** Reentrena con todos los datos de MongoDB (incluidas las asistencias nuevas). */
    @PostMapping("/modelo/entrenar")
    public Map<String, Object> entrenar() {
        return ia.post("/modelo/entrenar", null);
    }

    /** Alumnos y materias para los selectores. */
    @GetMapping("/opciones")
    public Map<String, Object> opciones() {
        return ia.get("/opciones");
    }

    /** { alumno, clase, fecha } → probabilidad de que falte, nivel y factores. */
    @PostMapping("/predecir")
    public Map<String, Object> predecir(@RequestBody Map<String, Object> solicitud) {
        return ia.post("/predecir", solicitud);
    }

    @ExceptionHandler(IaNoDisponibleException.class)
    public ResponseEntity<Map<String, Object>> error(IaNoDisponibleException e) {
        int status = e.getStatus() >= 500 || e.getStatus() == 401 ? 503 : e.getStatus();
        return ResponseEntity.status(status).body(Map.of("error", e.getMessage()));
    }
}

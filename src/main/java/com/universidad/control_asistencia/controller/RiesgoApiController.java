package com.universidad.control_asistencia.controller;

import com.universidad.control_asistencia.model.dto.riesgo.RiesgoDTOs.*;
import com.universidad.control_asistencia.service.PrediccionFaltaService;
import com.universidad.control_asistencia.service.RiesgoAsistenciaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** API JSON del módulo de riesgo de inasistencia (protegida por la regla /coordinador/** de SecurityConfig: solo COORDINADOR). */
@RestController
@RequestMapping("/coordinador/riesgo/api")
public class RiesgoApiController {

    private final RiesgoAsistenciaService riesgoService;
    private final PrediccionFaltaService prediccionService;

    public RiesgoApiController(RiesgoAsistenciaService riesgoService, PrediccionFaltaService prediccionService) {
        this.riesgoService = riesgoService;
        this.prediccionService = prediccionService;
    }

    @GetMapping("/resumen")
    public ResumenRiesgoDTO resumen() {
        return riesgoService.resumen();
    }

    @GetMapping("/alumnos")
    public List<AlumnoRiesgoDTO> alumnos(@RequestParam(required = false) String clase) {
        return riesgoService.calcularRiesgo(clase);
    }

    @GetMapping("/clases")
    public List<String> clases() {
        return riesgoService.clasesDisponibles();
    }

    @GetMapping("/tendencia")
    public TendenciaDTO tendencia() {
        return riesgoService.tendenciaMensual();
    }

    @GetMapping("/mapa-calor")
    public MapaCalorDTO mapaCalor() {
        return riesgoService.mapaCalor();
    }

    /** Probabilidad de falta por alumno para una fecha (por defecto hoy). */
    @GetMapping("/prediccion")
    public List<PrediccionFaltaDTO> prediccion(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) String clase) {
        return prediccionService.predecir(fecha != null ? fecha : LocalDate.now(), clase);
    }

    @GetMapping("/modelo")
    public ModeloMetricasDTO modelo() {
        return prediccionService.metricas();
    }

    @PostMapping("/modelo/reentrenar")
    public ModeloMetricasDTO reentrenar() {
        return prediccionService.reentrenar();
    }
}

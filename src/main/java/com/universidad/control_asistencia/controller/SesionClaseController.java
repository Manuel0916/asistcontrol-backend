package com.universidad.control_asistencia.controller;

import com.universidad.control_asistencia.service.SesionClaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/** El profesor abre una sesión de clase con la ubicación del salón (solo ADMIN/COORDINADOR). */
@RestController
@RequestMapping("/api/sesiones")
public class SesionClaseController {

    private final SesionClaseService sesiones;

    public SesionClaseController(SesionClaseService sesiones) {
        this.sesiones = sesiones;
    }

    public record CrearSesionRequest(String clase, Double lat, Double lng) {
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody CrearSesionRequest req) {
        if (req.clase() == null || req.clase().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "La clase es requerida"));
        }
        var s = sesiones.crear(req.clase().trim(), req.lat(), req.lng());
        Map<String, Object> resp = new HashMap<>();
        resp.put("id", s.id());
        resp.put("clase", s.clase());
        resp.put("conUbicacion", s.tieneUbicacion());
        resp.put("radioMetros", sesiones.getRadioMetros());
        return ResponseEntity.ok(resp);
    }
}

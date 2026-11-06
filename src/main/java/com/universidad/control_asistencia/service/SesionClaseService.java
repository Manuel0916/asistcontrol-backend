package com.universidad.control_asistencia.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sesiones de clase con ubicación (GEOLOCALIZACIÓN ANTI-FRAUDE).
 *
 * Cuando el profesor genera el QR, su teléfono envía las coordenadas GPS del salón y
 * aquí se crea una "sesión". El QR solo lleva el id de la sesión (no las coordenadas),
 * así nadie puede falsificar la ubicación del salón editando el QR.
 * Cuando el estudiante escanea, el servidor compara su ubicación con la del salón.
 *
 * Se guardan en memoria porque solo duran lo que dura la clase.
 */
@Service
public class SesionClaseService {

    public record SesionClase(String id, String clase, Double lat, Double lng,
                              Instant creada, Set<String> registrados) {
        public boolean tieneUbicacion() {
            return lat != null && lng != null;
        }
    }

    /** Resultado de validar la ubicación del estudiante. */
    public record Validacion(boolean ok, String mensaje, Double distanciaMetros) {
    }

    private static final Duration DURACION = Duration.ofHours(4);
    private static final double RADIO_TIERRA_M = 6_371_000;

    private final Map<String, SesionClase> sesiones = new ConcurrentHashMap<>();
    private final double radioMetros;

    public SesionClaseService(@Value("${app.geo.radio-metros:100}") double radioMetros) {
        this.radioMetros = radioMetros;
    }

    public double getRadioMetros() {
        return radioMetros;
    }

    public SesionClase crear(String clase, Double lat, Double lng) {
        limpiarVencidas();
        SesionClase s = new SesionClase(UUID.randomUUID().toString(), clase, lat, lng,
                Instant.now(), ConcurrentHashMap.newKeySet());
        sesiones.put(s.id(), s);
        return s;
    }

    public Optional<SesionClase> buscar(String id) {
        SesionClase s = id == null ? null : sesiones.get(id);
        if (s == null || s.creada().plus(DURACION).isBefore(Instant.now())) {
            return Optional.empty();
        }
        return Optional.of(s);
    }

    /**
     * Compara la ubicación del estudiante con la del salón.
     * Se tolera el margen de error del GPS (máximo 50 m) porque dentro de edificios es impreciso.
     */
    public Validacion validarUbicacion(SesionClase s, Double lat, Double lng, Double precision) {
        if (!s.tieneUbicacion()) {
            return new Validacion(true, "Sesión sin ubicación", null);
        }
        if (lat == null || lng == null) {
            return new Validacion(false,
                    "Activa la ubicación del teléfono: esta clase exige estar en el salón.", null);
        }
        double distancia = distanciaMetros(s.lat(), s.lng(), lat, lng);
        double margen = Math.min(precision == null ? 0 : Math.max(precision, 0), 50);
        if (distancia > radioMetros + margen) {
            return new Validacion(false, String.format(
                    "Estás a %.0f m del salón. Debes estar a menos de %.0f m para registrar tu asistencia.",
                    distancia, radioMetros), distancia);
        }
        return new Validacion(true, "Ubicación verificada", distancia);
    }

    /** Fórmula de Haversine: distancia en metros entre dos puntos (lat, lng) de la Tierra. */
    public static double distanciaMetros(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * RADIO_TIERRA_M * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private void limpiarVencidas() {
        Instant limite = Instant.now().minus(DURACION);
        sesiones.values().removeIf(s -> s.creada().isBefore(limite));
    }
}

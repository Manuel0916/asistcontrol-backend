package com.universidad.control_asistencia;

import com.universidad.control_asistencia.service.SesionClaseService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas de la geolocalización anti-fraude (no necesitan base de datos). */
class SesionClaseServiceTest {

    // Plaza de Bolívar, Bogotá (punto de referencia)
    private static final double LAT = 4.598056, LNG = -74.075833;

    private final SesionClaseService servicio = new SesionClaseService(100);

    @Test
    void haversineCalculaDistanciaConocida() {
        // ~0.001° de latitud ≈ 111 m
        double d = SesionClaseService.distanciaMetros(LAT, LNG, LAT + 0.001, LNG);
        assertEquals(111, d, 1.5);
    }

    @Test
    void estudianteEnElSalonEsAceptado() {
        var s = servicio.crear("historia-B", LAT, LNG);
        var v = servicio.validarUbicacion(s, LAT + 0.0002, LNG, 10.0); // ~22 m
        assertTrue(v.ok());
    }

    @Test
    void estudianteLejosEsRechazado() {
        var s = servicio.crear("historia-B", LAT, LNG);
        var v = servicio.validarUbicacion(s, LAT + 0.01, LNG, 10.0); // ~1.1 km
        assertFalse(v.ok());
        assertTrue(v.mensaje().contains("del salón"));
    }

    @Test
    void precisionExageradaNoSirveParaHacerTrampa() {
        var s = servicio.crear("historia-B", LAT, LNG);
        // 300 m de distancia declarando ±5000 m de error: el margen se limita a 50 m
        var v = servicio.validarUbicacion(s, LAT + 0.0027, LNG, 5000.0);
        assertFalse(v.ok());
    }

    @Test
    void sinUbicacionDelEstudianteSeRechaza() {
        var s = servicio.crear("historia-B", LAT, LNG);
        assertFalse(servicio.validarUbicacion(s, null, null, null).ok());
    }

    @Test
    void sesionSinUbicacionNoExigeGps() {
        var s = servicio.crear("historia-B", null, null);
        assertTrue(servicio.validarUbicacion(s, null, null, null).ok());
    }

    @Test
    void sesionInexistenteNoSeEncuentra() {
        assertTrue(servicio.buscar("no-existe").isEmpty());
    }
}

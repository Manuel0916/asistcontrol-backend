package com.universidad.control_asistencia;

import com.universidad.control_asistencia.model.Asistencia;
import com.universidad.control_asistencia.model.dto.Asistenciamongo;
import com.universidad.control_asistencia.repository.AsistenciaMongoRepository;
import com.universidad.control_asistencia.service.HistorialMongoService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** La asistencia nueva se copia a Mongo con el mismo formato del historial (sin usar la base real). */
class HistorialMongoServiceTest {

    @Test
    void copiaConElFormatoDelHistorial() {
        AsistenciaMongoRepository repo = mock(AsistenciaMongoRepository.class);
        HistorialMongoService servicio = new HistorialMongoService(repo);

        Asistencia a = new Asistencia();
        a.setId(42L);
        a.setNombre("Ana Torres");
        a.setClase("Matematicas-A");
        a.setEstado("Presente");
        a.setFechaHora(LocalDateTime.of(2026, 11, 4, 10, 15)); // miércoles

        servicio.copiar(a);

        ArgumentCaptor<Asistenciamongo> doc = ArgumentCaptor.forClass(Asistenciamongo.class);
        verify(repo).save(doc.capture());
        Asistenciamongo m = doc.getValue();
        assertEquals("Ana Torres", m.getNombre());
        assertEquals("miercoles", m.getDiaSemana());
        assertEquals("noviembre", m.getMes());
        assertEquals(10.25, m.getHoraNum(), 0.001);
        assertEquals(42L, m.getMysqlId());
        assertEquals(LocalDateTime.of(2026, 11, 4, 10, 15),
                LocalDateTime.ofInstant(m.getFechahora().toInstant(), ZoneId.of("America/Bogota")));
    }

    @Test
    void siMongoFallaNoRompeElRegistro() {
        AsistenciaMongoRepository repo = mock(AsistenciaMongoRepository.class);
        when(repo.save(any())).thenThrow(new RuntimeException("Mongo caído"));
        Asistencia a = new Asistencia();
        a.setFechaHora(LocalDateTime.now());
        assertDoesNotThrow(() -> new HistorialMongoService(repo).copiar(a));
    }
}

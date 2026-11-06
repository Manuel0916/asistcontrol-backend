package com.universidad.control_asistencia.service;

import com.universidad.control_asistencia.model.Asistencia;
import com.universidad.control_asistencia.model.dto.Asistenciamongo;
import com.universidad.control_asistencia.repository.AsistenciaMongoRepository;
import com.universidad.control_asistencia.utils.TextoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

/**
 * Copia cada asistencia nueva a MongoDB, que es el historial con el que se entrena
 * el modelo de IA. Así, al pulsar "Reentrenar", el modelo aprende también de lo que
 * se registra desde la app y la web.
 *
 * Si MongoDB falla, la asistencia igual queda guardada en MySQL (no se bloquea al usuario).
 */
@Service
public class HistorialMongoService {

    private static final Logger log = LoggerFactory.getLogger(HistorialMongoService.class);
    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final String[] MESES = {"enero", "febrero", "marzo", "abril", "mayo", "junio", "julio",
            "agosto", "septiembre", "octubre", "noviembre", "diciembre"};
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("hh:mm a", Locale.of("es", "CO"));

    private final AsistenciaMongoRepository repo;

    public HistorialMongoService(AsistenciaMongoRepository repo) {
        this.repo = repo;
    }

    public void copiar(Asistencia a) {
        try {
            LocalDateTime f = a.getFechaHora();
            Asistenciamongo doc = new Asistenciamongo();
            doc.setNombre(a.getNombre());
            doc.setClase(a.getClase());
            doc.setEstado(a.getEstado());
            doc.setFechahora(Date.from(f.atZone(ZONA).toInstant()));
            doc.setDiaSemana(TextoUtils.nombreDia(f.getDayOfWeek()));
            doc.setMes(MESES[f.getMonthValue() - 1]);
            doc.setHora(f.format(HORA));
            doc.setHoraNum(f.getHour() + f.getMinute() / 60.0);
            doc.setMysqlId(a.getId());
            repo.save(doc);
        } catch (Exception e) {
            log.warn("No se pudo copiar la asistencia {} a MongoDB: {}", a.getId(), e.getMessage());
        }
    }

    public void eliminar(Long mysqlId) {
        try {
            repo.deleteByMysqlId(mysqlId);
        } catch (Exception e) {
            log.warn("No se pudo borrar la asistencia {} de MongoDB: {}", mysqlId, e.getMessage());
        }
    }
}

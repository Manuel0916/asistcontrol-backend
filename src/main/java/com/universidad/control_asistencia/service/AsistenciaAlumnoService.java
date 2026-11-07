package com.universidad.control_asistencia.service;

import com.universidad.control_asistencia.interfaces.AsistenciaAlumnoInterface;
import com.universidad.control_asistencia.model.Asistencia;
import com.universidad.control_asistencia.model.dto.AsistenciaAlumnoDTO;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Predicción por alumno del formulario web (/asistencia/form).
 * Las listas del formulario salen directo de MongoDB (abre al instante aunque la IA esté
 * dormida); solo la predicción consulta el servicio de IA en Python.
 */
@Service
public class AsistenciaAlumnoService implements AsistenciaAlumnoInterface {

    private static final List<String> DIAS = List.of("lunes", "martes", "miercoles", "jueves", "viernes");
    private static final List<String> MESES = List.of("enero", "febrero", "marzo", "abril", "mayo", "junio",
            "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre");

    private final IaClient ia;
    private final MongoTemplate mongo;

    public AsistenciaAlumnoService(IaClient ia, MongoTemplate mongo) {
        this.ia = ia;
        this.mongo = mongo;
    }

    /** Listas para los selectores: alumnos y clases (de MongoDB), días y meses (fijos). */
    @Override
    public Map<String, List<String>> cargarOpciones() {
        // Mientras el usuario llena el formulario, la IA se va despertando
        ia.despertarEnSegundoPlano();

        List<String> nombres = mongo.findDistinct("nombre", Asistencia.class, String.class)
                .stream().filter(Objects::nonNull).map(String::trim).filter(s -> !s.isEmpty())
                .distinct().sorted(String.CASE_INSENSITIVE_ORDER).toList();

        // "matematicas-A" y "Matematicas-A" son la misma materia: se muestra una sola vez
        Map<String, String> clases = new LinkedHashMap<>();
        mongo.findDistinct("clase", Asistencia.class, String.class)
                .stream().filter(Objects::nonNull).map(String::trim).filter(s -> !s.isEmpty())
                // se prefiere la escritura con mayúscula inicial ("Matematicas-A")
                .forEach(c -> clases.merge(c.toLowerCase(), c,
                        (actual, nueva) -> Character.isUpperCase(actual.charAt(0)) ? actual : nueva));

        Map<String, List<String>> r = new HashMap<>();
        r.put("nombre", nombres);
        r.put("clase", clases.values().stream().sorted().toList());
        r.put("dia_semana", DIAS);
        r.put("mes", MESES);
        return r;
    }

    /** Llena estado ("Ausente"/"Presente"), probabilidad y nivel en el DTO. */
    @Override
    public String predecir(AsistenciaAlumnoDTO dto) {
        Map<String, Object> r = ia.post("/predecir", Map.of(
                "alumno", dto.getNombre(),
                "clase", dto.getClase(),
                "dia_semana", dto.getDiaSemana(),
                "mes", dto.getMes()));
        String estado = "Falta".equals(r.get("prediccion")) ? "Ausente" : "Presente";
        dto.setEstado(estado);
        dto.setProbabilidad(r.get("probabilidad_falta") instanceof Number n ? n.doubleValue() : null);
        dto.setNivel((String) r.get("nivel"));
        return estado;
    }
}

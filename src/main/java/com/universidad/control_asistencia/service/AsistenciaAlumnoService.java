package com.universidad.control_asistencia.service;

import com.universidad.control_asistencia.interfaces.AsistenciaAlumnoInterface;
import com.universidad.control_asistencia.model.dto.AsistenciaAlumnoDTO;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Predicción por alumno del formulario web (/asistencia/form).
 * Antes cargaba un modelo .model de Weka; ahora consulta el servicio de IA en Python.
 */
@Service
public class AsistenciaAlumnoService implements AsistenciaAlumnoInterface {

    private final IaClient ia;

    public AsistenciaAlumnoService(IaClient ia) {
        this.ia = ia;
    }

    /** Listas para los selectores: alumnos, clases, días y meses. */
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, List<String>> cargarOpciones() {
        Map<String, Object> o = ia.get("/opciones");
        Map<String, List<String>> r = new HashMap<>();
        r.put("nombre", (List<String>) o.get("alumnos"));
        r.put("clase", (List<String>) o.get("clases"));
        r.put("dia_semana", ((List<String>) o.get("dias")).subList(0, 5)); // lunes a viernes
        r.put("mes", (List<String>) o.get("meses"));
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

package com.universidad.control_asistencia.service;

import com.universidad.control_asistencia.config.RiesgoProperties;
import com.universidad.control_asistencia.model.dto.riesgo.RiesgoDTOs.ModeloMetricasDTO;
import com.universidad.control_asistencia.model.dto.riesgo.RiesgoDTOs.NivelProbabilidad;
import com.universidad.control_asistencia.model.dto.riesgo.RiesgoDTOs.PrediccionFaltaDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Probabilidad de que cada alumno falte a una clase en una fecha.
 * <p>
 * Antes usaba Weka; ahora el modelo (Random Forest) vive en el servicio de IA en Python
 * y aquí solo se traduce su respuesta a los DTOs que ya usan la web y la app.
 */
@Service
public class PrediccionFaltaService {

    private final IaClient ia;
    private final RiesgoProperties props;

    public PrediccionFaltaService(IaClient ia, RiesgoProperties props) {
        this.ia = ia;
        this.props = props;
    }

    public List<PrediccionFaltaDTO> predecir(LocalDate fecha, String clase) {
        List<Map<String, Object>> datos = ia.getLista(u -> {
            u.path("/prediccion").queryParam("fecha", fecha.toString());
            if (clase != null && !clase.isBlank()) u.queryParam("clase", clase);
            return u.build();
        });
        return datos.stream().map(d -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> f = (Map<String, Object>) d.get("factores");
            double p = numero(d.get("probabilidad_falta"));
            return new PrediccionFaltaDTO((String) d.get("alumno"), (String) d.get("clase"), fecha, p, nivel(p),
                    numero(f.get("tasa_historica")), (int) numero(f.get("faltas_ultimas5")));
        }).toList();
    }

    public ModeloMetricasDTO metricas() {
        return aDto(ia.get("/modelo"));
    }

    public ModeloMetricasDTO reentrenar() {
        return aDto(ia.post("/modelo/entrenar", null));
    }

    private ModeloMetricasDTO aDto(Map<String, Object> m) {
        return new ModeloMetricasDTO(
                (String) m.get("algoritmo"),
                (int) numero(m.get("registros")),
                numero(m.get("tasa_base_ausencia")),
                numero(m.get("accuracy")),
                numero(m.get("auc")),
                numero(m.get("precision")),
                numero(m.get("recall")),
                LocalDateTime.parse((String) m.get("entrenado_en")),
                (String) m.get("advertencia"));
    }

    private NivelProbabilidad nivel(double p) {
        if (p >= props.probAlta()) return NivelProbabilidad.ALTA;
        if (p >= props.probMedia()) return NivelProbabilidad.MEDIA;
        return NivelProbabilidad.BAJA;
    }

    private static double numero(Object o) {
        return o instanceof Number n ? n.doubleValue() : 0;
    }
}

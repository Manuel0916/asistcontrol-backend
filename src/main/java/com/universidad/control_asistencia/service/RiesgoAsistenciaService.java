package com.universidad.control_asistencia.service;

import com.universidad.control_asistencia.config.RiesgoProperties;
import com.universidad.control_asistencia.model.Asistencia;
import com.universidad.control_asistencia.model.dto.riesgo.RiesgoDTOs.*;
import com.universidad.control_asistencia.repository.AsistenciaRepository;
import com.universidad.control_asistencia.utils.TextoUtils;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Opción A: riesgo de perder una materia por inasistencia (regla + proyección)
 * y agregados para las gráficas.
 */
@Service
public class RiesgoAsistenciaService {

    private static final List<DayOfWeek> DIAS = List.of(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    private static final DateTimeFormatter MES = DateTimeFormatter.ofPattern("yyyy-MM");

    private final AsistenciaRepository asistenciaRepository;
    private final RiesgoProperties props;

    public RiesgoAsistenciaService(AsistenciaRepository asistenciaRepository, RiesgoProperties props) {
        this.asistenciaRepository = asistenciaRepository;
        this.props = props;
    }

    public List<AlumnoRiesgoDTO> calcularRiesgo(String clase) {
        List<Asistencia> registros = asistenciaRepository.findAll();
        return calcularRiesgo(registros, clase);
    }

    /** Lógica pura (sin acceso a BD) para poder probarla con datos en memoria. */
    List<AlumnoRiesgoDTO> calcularRiesgo(List<Asistencia> registros, String clase) {
        Map<String, List<Asistencia>> grupos = registros.stream()
                .filter(a -> a.getNombre() != null && a.getClase() != null)
                .filter(a -> clase == null || clase.isBlank() || a.getClase().equalsIgnoreCase(clase))
                .collect(Collectors.groupingBy(a -> a.getNombre() + "\u0000" + a.getClase()));

        List<AlumnoRiesgoDTO> resultado = new ArrayList<>();
        for (List<Asistencia> grupo : grupos.values()) {
            Asistencia ref = grupo.get(0);
            int n = grupo.size();
            int ausentes = (int) grupo.stream().filter(a -> esEstado(a, "Ausente")).count();
            int tardes = (int) grupo.stream().filter(a -> esEstado(a, "Tarde")).count();

            int total = Math.max(props.clasesSemestreDefecto(), n);
            double pctActual = 100.0 * ausentes / total;
            double tasa = (double) ausentes / n;
            int restantes = Math.max(0, total - n);
            double pctProyectado = 100.0 * (ausentes + tasa * restantes) / total;

            double limite = props.umbralPerdida() / 100.0 * total;
            int faltasPermitidas = Math.max(0, (int) Math.ceil(limite) - 1 - ausentes);

            NivelRiesgo nivel;
            if (pctActual >= props.umbralPerdida()) {
                nivel = NivelRiesgo.ROJO;
            } else if (pctActual >= props.umbralAlerta() || pctProyectado >= props.umbralPerdida()) {
                nivel = NivelRiesgo.AMARILLO;
            } else {
                nivel = NivelRiesgo.VERDE;
            }
            resultado.add(new AlumnoRiesgoDTO(ref.getNombre(), ref.getClase(), n, ausentes, tardes,
                    redondear(pctActual), redondear(pctProyectado), total, faltasPermitidas, nivel));
        }
        resultado.sort(Comparator.comparingDouble(AlumnoRiesgoDTO::porcentajeInasistencia).reversed()
                .thenComparing(AlumnoRiesgoDTO::alumno));
        return resultado;
    }

    public ResumenRiesgoDTO resumen() {
        List<AlumnoRiesgoDTO> lista = calcularRiesgo(null);
        int rojo = (int) lista.stream().filter(a -> a.nivel() == NivelRiesgo.ROJO).count();
        int amarillo = (int) lista.stream().filter(a -> a.nivel() == NivelRiesgo.AMARILLO).count();
        return new ResumenRiesgoDTO(lista.size(), rojo, amarillo, lista.size() - rojo - amarillo,
                props.umbralPerdida(), props.umbralAlerta());
    }

    public TendenciaDTO tendenciaMensual() {
        Map<String, int[]> meses = new TreeMap<>(); // [ausentes, total]
        for (Asistencia a : asistenciaRepository.findAll()) {
            if (a.getFechaHora() == null) continue;
            int[] c = meses.computeIfAbsent(a.getFechaHora().format(MES), k -> new int[2]);
            c[1]++;
            if (esEstado(a, "Ausente")) c[0]++;
        }
        List<String> etiquetas = new ArrayList<>(meses.keySet());
        List<Double> pct = new ArrayList<>();
        List<Integer> totales = new ArrayList<>();
        meses.values().forEach(c -> {
            pct.add(redondear(100.0 * c[0] / c[1]));
            totales.add(c[1]);
        });
        return new TendenciaDTO(etiquetas, pct, totales);
    }

    public MapaCalorDTO mapaCalor() {
        Map<String, Map<DayOfWeek, int[]>> datos = new TreeMap<>();
        Set<DayOfWeek> diasPresentes = new TreeSet<>();
        for (Asistencia a : asistenciaRepository.findAll()) {
            if (a.getFechaHora() == null || a.getClase() == null) continue;
            DayOfWeek d = a.getFechaHora().getDayOfWeek();
            diasPresentes.add(d);
            int[] c = datos.computeIfAbsent(a.getClase(), k -> new EnumMap<>(DayOfWeek.class))
                    .computeIfAbsent(d, k -> new int[2]);
            c[1]++;
            if (esEstado(a, "Ausente")) c[0]++;
        }
        List<DayOfWeek> dias = DIAS.stream().filter(diasPresentes::contains).toList();
        List<List<Double>> matriz = new ArrayList<>();
        for (Map<DayOfWeek, int[]> fila : datos.values()) {
            List<Double> valores = new ArrayList<>();
            for (DayOfWeek d : dias) {
                int[] c = fila.get(d);
                valores.add(c == null || c[1] == 0 ? null : redondear(100.0 * c[0] / c[1]));
            }
            matriz.add(valores);
        }
        return new MapaCalorDTO(new ArrayList<>(datos.keySet()),
                dias.stream().map(TextoUtils::nombreDia).toList(), matriz);
    }

    public List<String> clasesDisponibles() {
        return asistenciaRepository.findAll().stream().map(Asistencia::getClase)
                .filter(Objects::nonNull).distinct().sorted().toList();
    }

    static boolean esEstado(Asistencia a, String estado) {
        return a.getEstado() != null && a.getEstado().equalsIgnoreCase(estado);
    }

    private static double redondear(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}

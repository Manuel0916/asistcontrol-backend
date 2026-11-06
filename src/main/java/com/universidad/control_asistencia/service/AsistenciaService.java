package com.universidad.control_asistencia.service;

import com.universidad.control_asistencia.interfaces.AsistenciaInterface;
import com.universidad.control_asistencia.model.Asistencia;
import com.universidad.control_asistencia.repository.AsistenciaRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class AsistenciaService implements AsistenciaInterface {

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Override
    public Asistencia registrarAsistencia(Asistencia asistencia) {
        if (asistencia.getFechaHora() == null) {
            asistencia.setFechaHora(LocalDateTime.now());
        }
        return asistenciaRepository.save(asistencia);
    }

    @Override
    public List<Asistencia> obtenerTodas() {
        return asistenciaRepository.findAll();
    }

    @Override
    public List<Asistencia> obtenerPorClase(String clase) {
        return asistenciaRepository.findByClase(clase);
    }

    @Override
    public List<Asistencia> obtenerPorNombre(String nombre) {
        return asistenciaRepository.findByNombre(nombre);
    }

    @Override
    public List<Asistencia> obtenerPorRangoDeFechas(LocalDateTime inicio, LocalDateTime fin) {
        return asistenciaRepository.findByFechaHoraBetween(inicio, fin);
    }

}
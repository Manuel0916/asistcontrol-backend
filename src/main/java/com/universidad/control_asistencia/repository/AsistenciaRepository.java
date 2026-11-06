package com.universidad.control_asistencia.repository;

import com.universidad.control_asistencia.model.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    List<Asistencia> findByClase(String clase);

    List<Asistencia> findByNombre(String nombre);

    List<Asistencia> findByFechaHoraBetween(LocalDateTime start, LocalDateTime end);

    List<Asistencia> findByClaseAndEstado(String clase, String estado);
}
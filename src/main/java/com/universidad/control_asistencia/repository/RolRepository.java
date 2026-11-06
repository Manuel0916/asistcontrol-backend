package com.universidad.control_asistencia.repository;

import com.universidad.control_asistencia.model.Rol;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends MongoRepository<Rol, Long> {
    Optional<Rol> findByNombre(String nombre);
}

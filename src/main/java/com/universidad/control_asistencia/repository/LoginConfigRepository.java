package com.universidad.control_asistencia.repository;

import com.universidad.control_asistencia.model.LoginConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface LoginConfigRepository extends MongoRepository<LoginConfig, Long> {
    Optional<LoginConfig> findByRol(String rol);
}

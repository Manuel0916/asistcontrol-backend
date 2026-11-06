package com.universidad.control_asistencia.repository;

import com.universidad.control_asistencia.model.dto.Asistenciamongo;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AsistenciaMongoRepository extends MongoRepository<Asistenciamongo, String> {

    void deleteByMysqlId(Long mysqlId);
}

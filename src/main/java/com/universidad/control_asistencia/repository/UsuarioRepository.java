package com.universidad.control_asistencia.repository;

import com.universidad.control_asistencia.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UsuarioRepository extends MongoRepository<Usuario, Long> {

    // Busca el usuario por nombre de usuario
    Optional<Usuario> findByUsername(String username);

    // Busca el usuario por correo electrónico
    Optional<Usuario> findByEmail(String email);

    //esto aqui Verifica si un nombre de usuario ya existe
    boolean existsByUsername(String username);

    // aca Verifica si un correo electrónico ya está registrado
    boolean existsByEmail(String email);
}

package com.universidad.control_asistencia.interfaces;

import com.universidad.control_asistencia.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioInterface {
    List<Usuario> obtenerTodos();
    Optional<Usuario> obtenerPorId(Long id);
    Usuario registrarUsuario(Usuario usuario);
    void eliminarUsuario(Long id);
}
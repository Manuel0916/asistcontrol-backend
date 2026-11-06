package com.universidad.control_asistencia.service;

import com.universidad.control_asistencia.interfaces.UsuarioInterface;
import com.universidad.control_asistencia.model.Usuario;
import com.universidad.control_asistencia.repository.UsuarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UsuarioService implements UserDetailsService, UsuarioInterface {

    @Autowired
    private UsuarioRepository usuarioRepository;
    private PasswordEncoder passwordEncoder;


    @Override
    // Obtener todos los usuarios
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    @Override
    // Obtener usuario por ID
    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    // Registrar usuario con contraseña encriptada
    public Usuario registrarUsuario(Usuario usuario) {
        // Verificar si el correo ya está registrado
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("El correo ya está registrado");
        }

        // Encriptar la contraseña antes de guardar
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        return usuarioRepository.save(usuario);
    }

    @Override
    // Eliminar usuario por ID
    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }


    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        System.out.println("========== BUSCANDO USUARIO ==========");
        System.out.println("Valor recibido: '" + usernameOrEmail + "'");

        // Buscar primero por username
        Usuario usuario = usuarioRepository.findByUsername(usernameOrEmail)
                .orElse(null);

        // Si no encuentra por username, buscar por email
        if (usuario == null) {
            System.out.println("No encontrado por username, buscando por email: " + usernameOrEmail);
            usuario = usuarioRepository.findByEmail(usernameOrEmail)
                    .orElseThrow(() -> {
                        System.out.println("❌ USUARIO NO ENCONTRADO: " + usernameOrEmail);
                        return new UsernameNotFoundException("Usuario no encontrado: " + usernameOrEmail);
                    });
        }

        System.out.println("✅ USUARIO ENCONTRADO:");
        System.out.println("   Username: " + usuario.getUsername());
        System.out.println("   Email: " + usuario.getEmail());
        System.out.println("   Password (hash): " + usuario.getPassword());
        System.out.println("   Roles: " + usuario.getRoles().stream()
                .map(rol -> rol.getNombre())
                .collect(Collectors.toList()));

        List<SimpleGrantedAuthority> authorities = usuario.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getNombre()))
                .collect(Collectors.toList());

        return new User(usuario.getUsername(), usuario.getPassword(), authorities);
    }

}
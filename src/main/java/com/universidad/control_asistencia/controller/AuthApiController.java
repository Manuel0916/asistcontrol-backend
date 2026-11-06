package com.universidad.control_asistencia.controller;

import com.universidad.control_asistencia.interfaces.LoginConfigInterface;
import com.universidad.control_asistencia.model.Rol;
import com.universidad.control_asistencia.model.Usuario;
import com.universidad.control_asistencia.repository.RolRepository;
import com.universidad.control_asistencia.repository.UsuarioRepository;
import com.universidad.control_asistencia.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * API de autenticación para la app móvil (JWT).
 * La web sigue usando su login de formulario; esto no la afecta.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginConfigInterface loginConfig;

    public AuthApiController(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                             PasswordEncoder passwordEncoder, JwtService jwtService,
                             LoginConfigInterface loginConfig) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loginConfig = loginConfig;
    }

    public record LoginRequest(String usuario, String password) {
    }

    public record RegistroRequest(String username, String email, String password, String rol) {
    }

    /** Inicia sesión con usuario o correo + contraseña y devuelve un token JWT. */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        if (req.usuario() == null || req.password() == null
                || req.usuario().isBlank() || req.password().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Usuario y contraseña son obligatorios"));
        }

        Optional<Usuario> opt = buscarUsuario(req.usuario().trim());
        if (opt.isEmpty() || !passwordEncoder.matches(req.password(), opt.get().getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Usuario o contraseña incorrectos"));
        }

        Usuario usuario = opt.get();
        String rol = rolPrincipal(usuario);

        // El coordinador puede deshabilitar el acceso de estudiantes o profesores
        String claveConfig = claveLoginConfig(rol);
        if (claveConfig != null && !loginConfig.isLoginHabilitado(claveConfig)) {
            return ResponseEntity.status(403).body(Map.of(
                    "error", "El acceso para tu rol está deshabilitado por el coordinador"));
        }

        Map<String, Object> resp = datosUsuario(usuario, rol);
        resp.put("token", jwtService.generarToken(usuario.getUsername(), rol));
        resp.put("expiraEnMs", jwtService.getExpiracionMs());
        return ResponseEntity.ok(resp);
    }

    /** Datos del usuario dueño del token (sirve para validar la sesión al abrir la app). */
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        }
        return buscarUsuario(authentication.getName())
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(datosUsuario(u, rolPrincipal(u))))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado")));
    }

    /** Registro de usuarios desde la app (solo COORDINADOR, regla en SecurityConfig). */
    @PostMapping("/registro")
    public ResponseEntity<?> registro(@RequestBody RegistroRequest req) {
        if (req.username() == null || req.username().isBlank()
                || req.email() == null || req.email().isBlank()
                || req.password() == null || req.password().length() < 6
                || req.rol() == null || req.rol().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Completa todos los campos (contraseña de mínimo 6 caracteres)"));
        }
        if (usuarioRepository.existsByUsername(req.username().trim())) {
            return ResponseEntity.badRequest().body(Map.of("error", "El nombre de usuario ya está en uso"));
        }
        if (usuarioRepository.existsByEmail(req.email().trim())) {
            return ResponseEntity.badRequest().body(Map.of("error", "El correo ya está en uso"));
        }

        Optional<Rol> rol = rolRepository.findByNombre("ROLE_" + req.rol().trim().toUpperCase());
        if (rol.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El rol seleccionado no es válido"));
        }

        Usuario nuevo = new Usuario(req.username().trim(), req.email().trim(),
                passwordEncoder.encode(req.password()));
        nuevo.setRoles(Set.of(rol.get()));
        usuarioRepository.save(nuevo);

        return ResponseEntity.ok(Map.of("mensaje", "Usuario registrado correctamente"));
    }

    // ---------- utilidades ----------

    private Optional<Usuario> buscarUsuario(String usuarioOEmail) {
        Optional<Usuario> u = usuarioRepository.findByUsername(usuarioOEmail);
        return u.isPresent() ? u : usuarioRepository.findByEmail(usuarioOEmail);
    }

    /** ADMIN, COORDINADOR o ESTUDIANTE (sin el prefijo ROLE_). */
    private String rolPrincipal(Usuario u) {
        return u.getRoles().stream()
                .map(r -> r.getNombre().replace("ROLE_", ""))
                .findFirst()
                .orElse("ESTUDIANTE");
    }

    /** Clave usada por el panel "Control de Acceso" del coordinador. */
    private String claveLoginConfig(String rol) {
        return switch (rol) {
            case "ESTUDIANTE" -> "estudiante";
            case "ADMIN" -> "profesor";
            default -> null; // el coordinador siempre puede entrar
        };
    }

    private Map<String, Object> datosUsuario(Usuario u, String rol) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", u.getId());
        m.put("username", u.getUsername());
        m.put("email", u.getEmail());
        m.put("rol", rol);
        return m;
    }
}

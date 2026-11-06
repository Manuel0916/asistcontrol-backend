package com.universidad.control_asistencia.controller;

import com.universidad.control_asistencia.model.Rol;
import com.universidad.control_asistencia.model.Usuario;
import com.universidad.control_asistencia.repository.RolRepository;
import com.universidad.control_asistencia.repository.UsuarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Controller
@AllArgsConstructor
public class AuthController {

    private UsuarioRepository usuarioRepository;
    private RolRepository rolRepository;
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "registroExitoso", required = false) String registroExitoso,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "Usuario o contraseña incorrectos.");
        }
        if (logout != null) {
            model.addAttribute("logout", "Has cerrado sesión correctamente.");
        }
        if (registroExitoso != null) {
            model.addAttribute("success", "Registro exitoso. Ahora puedes iniciar sesión.");
        }
        return "login";
    }

    // 🔐 Solo los coordinadores pueden acceder a esta vista
    @GetMapping("/registro")
    public String registroPage(Model model, Authentication authentication) {
        if (authentication == null || !authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_COORDINADOR"))) {
            return "redirect:/login?error=acceso-denegado";
        }
        return "registro";
    }

    @PostMapping("/registro")
    public String registrarUsuario(@RequestParam String username,
                                   @RequestParam String email,
                                   @RequestParam String password,
                                   @RequestParam String rolNombre,
                                   Authentication authentication,
                                   Model model) {

        // Verifica si quien intenta registrar tiene permisos
        if (authentication == null || !authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_COORDINADOR"))) {
            return "redirect:/login?error=acceso-denegado";
        }

        if (usuarioRepository.existsByUsername(username)) {
            model.addAttribute("error", "El nombre de usuario ya está en uso.");
            return "registro";
        }

        if (usuarioRepository.existsByEmail(email)) {
            model.addAttribute("error", "El correo ya está en uso.");
            return "registro";
        }

        Optional<Rol> rolExistente = rolRepository.findByNombre("ROLE_" + rolNombre.toUpperCase());
        if (rolExistente.isEmpty()) {
            model.addAttribute("error", "El rol seleccionado no es válido.");
            return "registro";
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername(username);
        nuevoUsuario.setEmail(email);
        nuevoUsuario.setPassword(passwordEncoder.encode(password));

        Set<Rol> roles = new HashSet<>();
        roles.add(rolExistente.get());
        nuevoUsuario.setRoles(roles);

        try {
            usuarioRepository.save(nuevoUsuario);
            model.addAttribute("success", "Usuario registrado correctamente.");
            return "registro";
        } catch (Exception e) {
            model.addAttribute("error", "Hubo un error al registrar el usuario. Inténtalo de nuevo.");
            return "registro";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "redirect:/login";
    }

    @PostMapping("/forgot-password")
    public String recuperarPassword(@RequestParam String email, Model model) {
        if (!usuarioRepository.existsByEmail(email)) {
            model.addAttribute("error", "No se encontró un usuario con ese correo.");
            return "redirect:/login";
        }

        model.addAttribute("success", "Si el correo es válido, recibirás instrucciones para restablecer tu contraseña.");
        return "redirect:/login";
    }

    /**
     * ✅ ENDPOINT CORREGIDO: Devuelve información del usuario autenticado en formato JSON
     */
    @GetMapping("/api/usuario-actual")
    @ResponseBody
    public ResponseEntity<Map<String, String>> obtenerUsuarioActual(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        }

        String username = authentication.getName();

        // Buscar por username primero
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

        // Si no encuentra por username, buscar por email
        if (usuarioOpt.isEmpty()) {
            usuarioOpt = usuarioRepository.findByEmail(username);
        }

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            // Obtener el primer rol (los usuarios pueden tener múltiples roles)
            String rol = usuario.getRoles().stream()
                    .findFirst()
                    .map(r -> r.getNombre().replace("ROLE_", ""))
                    .orElse("ESTUDIANTE");

            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("username", usuario.getUsername());
            userInfo.put("email", usuario.getEmail());
            userInfo.put("rol", rol);

            return ResponseEntity.ok(userInfo);
        }

        return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
    }
}
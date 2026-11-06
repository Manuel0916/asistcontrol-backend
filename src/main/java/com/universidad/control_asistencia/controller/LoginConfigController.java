package com.universidad.control_asistencia.controller;

import com.universidad.control_asistencia.interfaces.LoginConfigInterface;
import com.universidad.control_asistencia.model.LoginConfig;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/login-config")
@AllArgsConstructor
public class LoginConfigController {

    @Autowired
    private LoginConfigInterface loginConfigInterface;

    // aqui en este Endpoint es para verificar si el login está habilitado para un rol
    @GetMapping("/{rol}/habilitado")
    public boolean isLoginEnabled(@PathVariable String rol) {
        return loginConfigInterface.isLoginHabilitado(rol);
    }

    // aca en el Endpoint para cambiar el estado del login (habilitado/deshabilitado)
    @PutMapping("/{rol}/toggle")
    public ResponseEntity<LoginConfig> toggleLoginState(@PathVariable String rol) {
        try {
            LoginConfig updatedConfig = loginConfigInterface.cambiarEstadoLogin(rol);
            return ResponseEntity.ok(updatedConfig);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null); // Error en el cambio de estado
        }
    }
}

package com.universidad.control_asistencia.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EstudianteController {

    @GetMapping("/estudiante/dashboard")
    public String estudianteDashboard() {
        return "estudiante_dashboard"; //
    }
}

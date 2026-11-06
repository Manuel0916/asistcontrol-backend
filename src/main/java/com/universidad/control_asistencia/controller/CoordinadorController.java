package com.universidad.control_asistencia.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CoordinadorController {

    @GetMapping("/coordinador/dashboard")
    public String coordinadorDashboard() {
        return "coordinador_dashboard";
    }
}

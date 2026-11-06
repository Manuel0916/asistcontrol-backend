package com.universidad.control_asistencia.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    @GetMapping("/")
    public String home() {
        return "home/index"; // Ahora buscará en templates/home/index.html
    }
}

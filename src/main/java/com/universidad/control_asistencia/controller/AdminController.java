package com.universidad.control_asistencia.controller;

import com.universidad.control_asistencia.model.Asistencia;
import com.universidad.control_asistencia.repository.AsistenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.List;

@Controller
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    // Vista del dashboard del administrador
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        List<Asistencia> asistencias = asistenciaRepository.findAll();
        model.addAttribute("asistencias", asistencias);
        return "admin_dashboard";
    }

    // API REST para obtener todas las asistencias
    @ResponseBody
    @GetMapping("/api/asistencias")
    public List<Asistencia> obtenerTodasLasAsistencias() {
        return asistenciaRepository.findAll();
    }

    // API REST para obtener asistencias por clase
    @ResponseBody
    @GetMapping("/api/asistencias/clase/{nombreClase}")
    public List<Asistencia> obtenerAsistenciasPorClase(@PathVariable String nombreClase) {
        return asistenciaRepository.findByClase(nombreClase);
    }


}

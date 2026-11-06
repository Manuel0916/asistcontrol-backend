package com.universidad.control_asistencia.controller;

import com.universidad.control_asistencia.interfaces.AsistenciaAlumnoInterface;
import com.universidad.control_asistencia.model.dto.AsistenciaAlumnoDTO;
import com.universidad.control_asistencia.service.AsistenciaAlumnoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/asistencia")
public class AAsistenciaAlumnoController {

    @Autowired
    private AsistenciaAlumnoInterface asistenciaAlumnoInterface;

    // ================== ✅ FORMULARIO HTML ==================
    @GetMapping("/form")
    public String mostrarFormulario(Model model) {

        // Opciones de los selectores (alumnos, clases, días y meses) desde el servicio de IA
        try {
            Map<String, java.util.List<String>> opciones = asistenciaAlumnoInterface.cargarOpciones();
            model.addAttribute("nombres", opciones.get("nombre"));
            model.addAttribute("clases", opciones.get("clase"));
            model.addAttribute("dias", opciones.get("dia_semana"));
            model.addAttribute("meses", opciones.get("mes"));
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("asistenciaDto", new AsistenciaAlumnoDTO());

        return "formulario_alumno"; // archivo HTML (templates/formulario_alumno.html)
    }

    // ================== ✅ RESULTADO HTML ==================
    @PostMapping("/predict")
    public String predecirHtml(@ModelAttribute AsistenciaAlumnoDTO dto, Model model) {
        try {
            String estado = asistenciaAlumnoInterface.predecir(dto);

            model.addAttribute("resultado", estado);
            model.addAttribute("asistencia", dto);

            return "resultado_alumno";

        } catch (Exception e) {
            model.addAttribute("error", "Error al predecir: " + e.getMessage());
            return "resultado_alumno";
        }
    }

    // ================== ✅ API JSON (Postman) ==================
    @PostMapping("/api/predict")
    @ResponseBody
    public ResponseEntity<?> predecirApi(@RequestBody AsistenciaAlumnoDTO dto) {
        try {
            asistenciaAlumnoInterface.predecir(dto);
            return ResponseEntity.ok(dto);

        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "ok", false,
                            "error", e.getMessage()
                    ));
        }
    }
}

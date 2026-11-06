package com.universidad.control_asistencia.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ia")
@CrossOrigin(origins = "*")
public class IA_Controller {

    // Inyectamos el ChatClient que ya viene configurado desde IAConfig
    // con el system prompt completo y las herramientas MCP conectadas
    private final ChatClient chatClient;

    public IA_Controller(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        String mensaje = request.get("message");
        if (mensaje == null) mensaje = request.get("mensaje");

        if (mensaje == null || mensaje.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("response", "❌ Por favor, escribe una pregunta o instrucción."));
        }

        try {
            String respuesta = chatClient.prompt()
                    .user(mensaje)
                    .call()
                    .content();

            return ResponseEntity.ok(Map.of("response", respuesta));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("response", "⚠️ Error en el sistema: " + e.getMessage()));
        }
    }
}
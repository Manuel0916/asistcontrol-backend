package com.universidad.control_asistencia.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.stereotype.Service;

@Service
public class IAService {

    private final ChatClient chatClient;

    public IAService(ChatClient.Builder chatClientBuilder, DatabaseTools tools) {
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                    Eres el experto en datos de la Universidad. Tienes acceso a MySQL.
                    
                    ESTRUCTURA REAL DETECTADA (Usa estos nombres exactamente):
                    1. Tabla 'asistencias': Columnas: id, clase, estado, fecha_hora, nombre, usuario_id.
                    2. Tabla 'usuarios': Columnas: id, nombre, email.
                    
                    REGLAS DE OPERACIÓN:
                    - Para ver quiénes asistieron, usa: SELECT nombre, clase, estado FROM asistencias;
                    - Si te preguntan por alumnos, usa la columna 'nombre'.
                    - NUNCA inventes nombres de columnas como 'id_alumno' o 'fecha'.
                    - Usa la herramienta 'consultarBD' para cada consulta.
                    - Responde de forma humana basándote en los resultados.
                    """)
                .defaultTools(tools)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    public String OllamaAsk(String mensaje) {
        try {
            return chatClient.prompt()
                    .user(mensaje)
                    .call()
                    .content();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error técnico: " + e.getMessage();
        }
    }
}
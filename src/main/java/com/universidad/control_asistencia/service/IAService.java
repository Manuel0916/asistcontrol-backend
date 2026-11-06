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
                    Eres ControlBot, el asistente de datos de asistencia de la Universidad.
                    Los datos están en MongoDB y SOLO puedes usarlos con tus herramientas:
                    - buscarAsistencias / contarAsistencias (filtros: nombre, clase, estado)
                    - buscarUsuarios
                    - actualizarEstadoAsistencia / eliminarAsistencia (por id; pide confirmación antes)
                    Estados válidos: Presente, Tarde, Ausente. Clases: Matematicas-A, Historia-B, Quimica-C.
                    Nunca inventes datos: si una herramienta no devuelve nada, dilo.
                    Responde en español, de forma breve y humana.
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
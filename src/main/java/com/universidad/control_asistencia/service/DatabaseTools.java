package com.universidad.control_asistencia.service;

import com.universidad.control_asistencia.model.Asistencia;
import com.universidad.control_asistencia.model.Usuario;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Herramientas que ControlBot (Ollama) puede usar sobre MongoDB.
 * Antes recibían SQL libre; ahora cada herramienta hace una operación concreta,
 * lo que además es más seguro (el modelo no puede ejecutar cualquier cosa).
 */
@Component
public class DatabaseTools {

    private static final int MAX_RESULTADOS = 50;

    private final MongoTemplate mongo;

    public DatabaseTools(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    @Tool(description = """
            Busca asistencias. Todos los filtros son opcionales: nombre del alumno (o parte),
            clase (p. ej. Matematicas-A) y estado (Presente, Tarde o Ausente).
            Devuelve las más recientes primero (máximo 50).
            """)
    public String buscarAsistencias(
            @ToolParam(required = false, description = "Nombre del alumno o parte de él") String nombre,
            @ToolParam(required = false, description = "Clase, p. ej. Matematicas-A") String clase,
            @ToolParam(required = false, description = "Presente, Tarde o Ausente") String estado) {
        Query q = filtro(nombre, clase, estado).with(Sort.by(Sort.Direction.DESC, "fechaHora")).limit(MAX_RESULTADOS);
        List<Asistencia> lista = mongo.find(q, Asistencia.class);
        if (lista.isEmpty()) return "No se encontraron asistencias con esos filtros.";
        List<String> filas = new ArrayList<>();
        for (Asistencia a : lista) {
            filas.add("id=" + a.getId() + " | " + a.getNombre() + " | " + a.getClase() + " | "
                    + a.getEstado() + " | " + a.getFechaHora());
        }
        return "Resultados (" + filas.size() + "):\n" + String.join("\n", filas);
    }

    @Tool(description = """
            Cuenta asistencias con filtros opcionales (nombre, clase, estado).
            Úsala para preguntas como "¿cuántas faltas tiene Ana?" o "¿cuántos llegaron tarde en Historia-B?".
            """)
    public String contarAsistencias(
            @ToolParam(required = false, description = "Nombre del alumno o parte de él") String nombre,
            @ToolParam(required = false, description = "Clase, p. ej. Matematicas-A") String clase,
            @ToolParam(required = false, description = "Presente, Tarde o Ausente") String estado) {
        long n = mongo.count(filtro(nombre, clase, estado), Asistencia.class);
        return "Total: " + n + " asistencia(s).";
    }

    @Tool(description = "Lista usuarios del sistema (username, email y roles). Filtro opcional por username o parte de él.")
    public String buscarUsuarios(
            @ToolParam(required = false, description = "Username o parte de él") String username) {
        Query q = new Query().limit(MAX_RESULTADOS);
        if (tiene(username)) q.addCriteria(Criteria.where("username").regex(contiene(username)));
        List<Usuario> lista = mongo.find(q, Usuario.class);
        if (lista.isEmpty()) return "No se encontraron usuarios.";
        List<String> filas = new ArrayList<>();
        for (Usuario u : lista) {
            filas.add("id=" + u.getId() + " | " + u.getUsername() + " | " + u.getEmail() + " | "
                    + u.getRoles().stream().map(r -> r.getNombre()).toList());
        }
        return String.join("\n", filas);
    }

    @Tool(description = "Cambia el estado de UNA asistencia por su id. Estado: Presente, Tarde o Ausente.")
    public String actualizarEstadoAsistencia(
            @ToolParam(description = "id numérico de la asistencia") Long id,
            @ToolParam(description = "Presente, Tarde o Ausente") String estado) {
        var r = mongo.updateFirst(Query.query(Criteria.where("id").is(id)),
                new Update().set("estado", estado), Asistencia.class);
        return r.getMatchedCount() == 0 ? "No existe una asistencia con id " + id + "."
                : "Asistencia " + id + " actualizada a " + estado + ".";
    }

    @Tool(description = "Elimina UNA asistencia por su id.")
    public String eliminarAsistencia(@ToolParam(description = "id numérico de la asistencia") Long id) {
        var r = mongo.remove(Query.query(Criteria.where("id").is(id)), Asistencia.class);
        return r.getDeletedCount() == 0 ? "No existe una asistencia con id " + id + "."
                : "Asistencia " + id + " eliminada.";
    }

    // ------------------------------------------------------------------ utilidades

    private static Query filtro(String nombre, String clase, String estado) {
        Query q = new Query();
        if (tiene(nombre)) q.addCriteria(Criteria.where("nombre").regex(contiene(nombre)));
        if (tiene(clase)) q.addCriteria(Criteria.where("clase").regex("^" + Pattern.quote(clase.trim()) + "$", "i"));
        if (tiene(estado)) q.addCriteria(Criteria.where("estado").regex("^" + Pattern.quote(estado.trim()) + "$", "i"));
        return q;
    }

    private static Pattern contiene(String texto) {
        return Pattern.compile(Pattern.quote(texto.trim()), Pattern.CASE_INSENSITIVE);
    }

    private static boolean tiene(String s) {
        return s != null && !s.isBlank();
    }
}

package com.universidad.control_asistencia.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@Component
public class DatabaseTools {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseTools(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public record SqlRequest(String sql) {}

    // ─────────────────────────────────────────────
    //  SELECT — Consultar datos
    // ─────────────────────────────────────────────
    @Tool(description = """
        Ejecuta consultas SQL de tipo SELECT en la base de datos control_asistencia.
        Úsala para leer, buscar, contar o listar datos de las tablas 'asistencias' y 'usuarios'.
        Ejemplos: contar registros, buscar por nombre, listar asistencias por fecha.
        """)
    public String consultarBD(SqlRequest request) {
        String sql = limpiarSQL(request.sql());
        sql = corregirTabla(sql);
        System.out.println("🔍 SELECT: " + sql);
        try {
            List<Map<String, Object>> resultados = jdbcTemplate.queryForList(sql);
            if (resultados.isEmpty()) {
                return "✅ Consulta ejecutada correctamente, pero no se encontraron registros.";
            }
            return "✅ Resultados (" + resultados.size() + " filas): " + resultados.toString();
        } catch (Exception e) {
            return "❌ Error en la consulta: " + e.getMessage();
        }
    }

    // ─────────────────────────────────────────────
    //  UPDATE — Actualizar datos
    // ─────────────────────────────────────────────
    @Tool(description = """
        Ejecuta sentencias SQL de tipo UPDATE en la base de datos control_asistencia.
        Úsala cuando el usuario pida modificar, cambiar, actualizar o editar
        cualquier dato de las tablas 'asistencias' o 'usuarios'.
        Ejemplos: cambiar nombre de usuario, actualizar estado de asistencia,
        modificar email, cambiar contraseña.
        IMPORTANTE: siempre incluye una cláusula WHERE para no afectar todos los registros.
        """)
    public String actualizarBD(SqlRequest request) {
        String sql = limpiarSQL(request.sql());
        sql = corregirTabla(sql);
        System.out.println("✏️ UPDATE: " + sql);
        try {
            int filasAfectadas = jdbcTemplate.update(sql);
            if (filasAfectadas == 0) {
                return "⚠️ El UPDATE se ejecutó pero no encontró registros que coincidan con la condición. Verifica el id o el criterio usado.";
            }
            return "✅ Actualización exitosa. Se modificaron " + filasAfectadas + " registro(s) en la base de datos.";
        } catch (Exception e) {
            return "❌ Error al actualizar: " + e.getMessage();
        }
    }

    // ─────────────────────────────────────────────
    //  DELETE — Eliminar datos
    // ─────────────────────────────────────────────
    @Tool(description = """
        Ejecuta sentencias SQL de tipo DELETE en la base de datos control_asistencia.
        Úsala cuando el usuario pida eliminar, borrar o remover un registro
        de las tablas 'asistencias' o 'usuarios'.
        Ejemplos: eliminar usuario por id, borrar asistencias de una fecha,
        remover registros duplicados.
        IMPORTANTE: siempre incluye una cláusula WHERE para no borrar toda la tabla.
        """)
    public String eliminarBD(SqlRequest request) {
        String sql = limpiarSQL(request.sql());
        sql = corregirTabla(sql);
        System.out.println("🗑️ DELETE: " + sql);
        try {
            int filasAfectadas = jdbcTemplate.update(sql);
            if (filasAfectadas == 0) {
                return "⚠️ El DELETE se ejecutó pero no encontró registros que coincidan. Verifica el id o el criterio usado.";
            }
            return "✅ Eliminación exitosa. Se borraron " + filasAfectadas + " registro(s) de la base de datos.";
        } catch (Exception e) {
            return "❌ Error al eliminar: " + e.getMessage();
        }
    }

    // ─────────────────────────────────────────────
    //  INSERT — Insertar datos
    // ─────────────────────────────────────────────
    @Tool(description = """
        Ejecuta sentencias SQL de tipo INSERT en la base de datos control_asistencia.
        Úsala cuando el usuario pida agregar, crear, registrar o insertar
        un nuevo registro en las tablas 'asistencias' o 'usuarios'.
        Ejemplos: registrar nueva asistencia, agregar nuevo usuario,
        crear registro con nombre y fecha.
        """)
    public String insertarBD(SqlRequest request) {
        String sql = limpiarSQL(request.sql());
        sql = corregirTabla(sql);
        System.out.println("➕ INSERT: " + sql);
        try {
            jdbcTemplate.update(sql);
            return "✅ Registro insertado exitosamente en la base de datos.";
        } catch (Exception e) {
            return "❌ Error al insertar: " + e.getMessage();
        }
    }

    // ─────────────────────────────────────────────
    //  Utilidades privadas
    // ─────────────────────────────────────────────
    private String limpiarSQL(String sql) {
        return sql.replace("\"", "").replace("`", "").trim();
    }

    private String corregirTabla(String sql) {
        String lower = sql.toLowerCase();
        if (lower.contains("asistencia") && !lower.contains("asistencias")) {
            sql = sql.replaceAll("(?i)asistencia(?!s)", "asistencias");
        }
        return sql;
    }
}
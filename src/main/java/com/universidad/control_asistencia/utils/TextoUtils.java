package com.universidad.control_asistencia.utils;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.util.Locale;

public final class TextoUtils {

    private TextoUtils() {
    }

    /** minúsculas, sin tildes y sin espacios sobrantes: "Miércoles " -> "miercoles". */
    public static String normalizar(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    /** DayOfWeek -> nombre en español normalizado ("lunes", "miercoles"...). */
    public static String nombreDia(DayOfWeek d) {
        return switch (d) {
            case MONDAY -> "lunes";
            case TUESDAY -> "martes";
            case WEDNESDAY -> "miercoles";
            case THURSDAY -> "jueves";
            case FRIDAY -> "viernes";
            case SATURDAY -> "sabado";
            case SUNDAY -> "domingo";
        };
    }
}

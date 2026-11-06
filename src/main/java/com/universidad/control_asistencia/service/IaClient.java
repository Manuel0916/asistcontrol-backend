package com.universidad.control_asistencia.service;

import com.universidad.control_asistencia.exception.IaNoDisponibleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Cliente HTTP hacia el servicio de IA en Python (FastAPI).
 * Spring Boot es el único que habla con él, usando una clave compartida (X-API-Key).
 */
@Service
public class IaClient {

    private static final ParameterizedTypeReference<Map<String, Object>> MAPA = new ParameterizedTypeReference<>() {
    };
    private static final ParameterizedTypeReference<List<Map<String, Object>>> LISTA = new ParameterizedTypeReference<>() {
    };

    /** Tiempo máximo esperando a que la IA despierte (la app espera 90 s en total). */
    private static final long ESPERA_MAXIMA_MS = 70_000;
    private static final long PAUSA_MS = 5_000;
    private static final String DESPERTANDO =
            "El servicio de IA está despertando (en la nube gratis se duerme sin uso). Intenta de nuevo en unos segundos.";

    private final RestClient http;

    public IaClient(@Value("${app.ia.url}") String url, @Value("${app.ia.api-key}") String apiKey) {
        SimpleClientHttpRequestFactory tiempos = new SimpleClientHttpRequestFactory();
        tiempos.setConnectTimeout(15_000);
        tiempos.setReadTimeout(90_000); // en la nube gratis el servicio puede tardar en "despertar"
        this.http = RestClient.builder()
                .baseUrl(url)
                .requestFactory(tiempos)
                .defaultHeader("X-API-Key", apiKey)
                .build();
    }

    public Map<String, Object> get(String ruta) {
        return ejecutar(() -> http.get().uri(ruta).retrieve().body(MAPA));
    }

    public List<Map<String, Object>> getLista(Function<UriBuilder, URI> uri) {
        return ejecutar(() -> http.get().uri(uri).retrieve().body(LISTA));
    }

    public Map<String, Object> post(String ruta, Object cuerpo) {
        return ejecutar(() -> http.post().uri(ruta)
                .contentType(MediaType.APPLICATION_JSON)
                .body(cuerpo == null ? Map.of() : cuerpo)
                .retrieve().body(MAPA));
    }

    /**
     * Al arrancar el backend se "despierta" al servicio de IA en segundo plano: en la nube gratis
     * ambos se duermen tras 15 min sin uso, y así la IA ya está lista cuando el usuario la necesita.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void despertarIa() {
        Thread.ofVirtual().start(() -> {
            try {
                get("/salud");
            } catch (Exception ignorada) {
                // Si no responde no pasa nada: la primera consulta volverá a intentarlo
            }
        });
    }

    private <T> T ejecutar(Supplier<T> llamada) {
        long limite = System.currentTimeMillis() + ESPERA_MAXIMA_MS;
        while (true) {
            try {
                return llamada.get();
            } catch (RestClientResponseException e) {
                int status = e.getStatusCode().value();
                // 502/503/504: la nube responde esto mientras la IA "despierta" -> esperar y reintentar
                if (status >= 502 && status <= 504 && esperar(limite)) {
                    continue;
                }
                throw new IaNoDisponibleException(mensaje(e), status >= 500 ? 503 : status);
            } catch (ResourceAccessException e) {
                // No se pudo conectar (servicio arrancando): esperar y reintentar
                if (esperar(limite)) {
                    continue;
                }
                throw new IaNoDisponibleException(DESPERTANDO, 503);
            }
        }
    }

    /** Espera unos segundos antes de reintentar; false si ya se acabó el tiempo. */
    private static boolean esperar(long limite) {
        if (System.currentTimeMillis() + PAUSA_MS > limite) {
            return false;
        }
        try {
            Thread.sleep(PAUSA_MS);
            return true;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /** FastAPI responde {"detail": "..."}: se reenvía ese mensaje. Si es una página HTML, un mensaje claro. */
    private static String mensaje(RestClientResponseException e) {
        String cuerpo = e.getResponseBodyAsString();
        int i = cuerpo.indexOf("\"detail\":\"");
        if (i >= 0) {
            int fin = cuerpo.indexOf('"', i + 10);
            return "Servicio de IA: " + cuerpo.substring(i + 10, Math.max(i + 10, fin));
        }
        return DESPERTANDO;
    }
}

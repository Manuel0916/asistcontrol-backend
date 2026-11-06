package com.universidad.control_asistencia.service;

import com.universidad.control_asistencia.exception.IaNoDisponibleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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

    private <T> T ejecutar(java.util.function.Supplier<T> llamada) {
        try {
            return llamada.get();
        } catch (RestClientResponseException e) {
            // FastAPI responde {"detail": "..."}: se reenvía ese mensaje
            String detalle = e.getResponseBodyAsString();
            int i = detalle.indexOf("\"detail\":\"");
            String msg = i >= 0 ? detalle.substring(i + 10, Math.max(i + 10, detalle.indexOf('"', i + 10))) : detalle;
            throw new IaNoDisponibleException("Servicio de IA: " + msg, e.getStatusCode().value());
        } catch (Exception e) {
            throw new IaNoDisponibleException(
                    "El servicio de IA no responde. Verifica que esté encendido (puede tardar si estaba dormido).", 503);
        }
    }
}

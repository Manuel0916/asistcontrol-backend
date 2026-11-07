package com.universidad.control_asistencia.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URI;

/**
 * Al correr el proyecto en el PC (Run en IntelliJ), prende solo el servicio de IA en Python
 * (carpeta ../control_asistencias_ia) y lo apaga al detener el backend.
 * <p>
 * No hace nada si la IA ya está prendida, si la IA no es local (nube / Docker)
 * o si se desactiva con IA_AUTO_INICIAR=false.
 */
@Component
public class IaLocalLauncher {

    private static final Logger log = LoggerFactory.getLogger(IaLocalLauncher.class);

    private final String url;
    private final String apiKey;
    private final String carpeta;
    private final boolean activo;
    private Process proceso;

    public IaLocalLauncher(@Value("${app.ia.url}") String url,
                           @Value("${app.ia.api-key}") String apiKey,
                           @Value("${app.ia.carpeta:../control_asistencias_ia}") String carpeta,
                           @Value("${app.ia.auto-iniciar:true}") boolean activo) {
        this.url = url;
        this.apiKey = apiKey;
        this.carpeta = carpeta;
        this.activo = activo;
    }

    @PostConstruct
    void iniciar() {
        URI uri = URI.create(url);
        boolean esLocal = "localhost".equals(uri.getHost()) || "127.0.0.1".equals(uri.getHost());
        if (!activo || !esLocal) {
            return;
        }
        if (estaPrendida()) {
            log.info("IA: el servicio de Python ya está prendido en {}", url);
            return;
        }

        File dir = new File(carpeta).getAbsoluteFile();
        File python = new File(dir, ".venv/Scripts/python.exe");       // Windows
        if (!python.exists()) {
            python = new File(dir, ".venv/bin/python");                  // Linux / Mac
        }
        if (!python.exists()) {
            log.warn("IA: no se encontró {}. Prepara la IA una vez (ver README de control_asistencias_ia).",
                    new File(dir, ".venv"));
            return;
        }

        int puerto = uri.getPort() > 0 ? uri.getPort() : 8000;
        try {
            ProcessBuilder pb = new ProcessBuilder(python.getPath(), "-m", "uvicorn", "app.main:app",
                    "--host", "127.0.0.1", "--port", String.valueOf(puerto))
                    .directory(dir)
                    .redirectErrorStream(true)
                    .redirectOutput(new File(dir, "ia.log"));
            pb.environment().put("IA_API_KEY", apiKey);
            proceso = pb.start();
            log.info("IA: prendiendo el servicio de Python en {} (registro en {})", url, new File(dir, "ia.log"));
        } catch (Exception e) {
            log.warn("IA: no se pudo prender el servicio de Python: {}", e.getMessage());
        }
    }

    @PreDestroy
    void detener() {
        if (proceso != null && proceso.isAlive()) {
            proceso.descendants().forEach(ProcessHandle::destroy);
            proceso.destroy();
            log.info("IA: servicio de Python apagado");
        }
    }

    private boolean estaPrendida() {
        try {
            HttpURLConnection c = (HttpURLConnection) URI.create(url + "/salud").toURL().openConnection();
            c.setConnectTimeout(1500);
            c.setReadTimeout(3000);
            return c.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}

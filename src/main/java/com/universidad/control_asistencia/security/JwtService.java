package com.universidad.control_asistencia.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Crea y valida los tokens JWT que usa la app móvil.
 *
 * Un JWT es un texto firmado que dice "este usuario es X y tiene el rol Y, válido hasta Z".
 * El servidor lo firma con una clave secreta; si alguien lo modifica, la firma deja de coincidir.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expiracionMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiracion-horas:8}") long expiracionHoras) {
        // HS256 exige una clave de al menos 32 bytes
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = expiracionHoras * 60 * 60 * 1000;
    }

    public String generarToken(String username, String rol) {
        Date ahora = new Date();
        return Jwts.builder()
                .setSubject(username)
                .claim("rol", rol)
                .setIssuedAt(ahora)
                .setExpiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public long getExpiracionMs() {
        return expiracionMs;
    }

    /** Devuelve el username si el token es válido y no ha expirado; si no, null. */
    public String validarYObtenerUsuario(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}

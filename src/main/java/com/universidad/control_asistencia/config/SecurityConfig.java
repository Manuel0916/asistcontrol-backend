package com.universidad.control_asistencia.config;

import com.universidad.control_asistencia.security.JwtAuthFilter;
import com.universidad.control_asistencia.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("#{'${app.cors.origenes}'.split(',')}")
    private List<String> origenesCors;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtService jwtService,
                                                   UserDetailsService userDetailsService) throws Exception {

        // APP MÓVIL: si la petición trae "Authorization: Bearer <token>", este filtro la autentica.
        // La web sigue usando su sesión normal (cookie JSESSIONID).
        http.addFilterBefore(new JwtAuthFilter(jwtService, userDetailsService),
                UsernamePasswordAuthenticationFilter.class);

        // Las APIs responden 401 (en vez de redirigir al HTML del login) cuando falta autenticación
        RequestMatcher peticionApi = request -> JwtAuthFilter.tieneBearer(request)
                || request.getRequestURI().startsWith("/api/")
                || request.getRequestURI().startsWith("/coordinador/riesgo/api/");
        http.exceptionHandling(ex -> ex.defaultAuthenticationEntryPointFor(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), peticionApi));

        // CORS: permite que la app Flutter web (Vercel o localhost) llame a este backend.
        // En la app instalada (APK) no aplica: CORS es una regla de los navegadores.
        // Los orígenes se configuran con la variable CORS_ORIGINS (ver application.properties).
        http.cors(cors -> cors.configurationSource(request -> {
            CorsConfiguration c = new CorsConfiguration();
            c.setAllowedOriginPatterns(origenesCors);
            c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            c.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
            return c;
        }));

        // FIX: usar CookieCsrfTokenRepository para evitar el error
        // "Cannot create a session after the response has been committed"
        // que ocurría porque HttpSessionCsrfTokenRepository intentaba crear
        // sesión después de que la respuesta ya había sido enviada.
        CookieCsrfTokenRepository csrfRepo = CookieCsrfTokenRepository.withHttpOnlyFalse();
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();

        http
                // CSRF con cookies en lugar de sesión
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfRepo)
                        .csrfTokenRequestHandler(requestHandler)
                        .ignoringRequestMatchers(
                                "/ia/chat",
                                "/ia/**",
                                "/api/**"
                        )
                        // Peticiones con token JWT (app móvil): el navegador nunca envía este
                        // header por sí solo, así que no hay riesgo de CSRF
                        .ignoringRequestMatchers(JwtAuthFilter::tieneBearer)
                )

                // AUTORIZACIONES
                .authorizeHttpRequests(auth -> auth

                        // RUTAS PUBLICAS
                        .requestMatchers(
                                "/",
                                "/login",
                                "/forgot-password",
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/imagenes/**",   // FIX: archivos en static/imagenes
                                "/static/**",     // FIX: todo lo que esté en static
                                "/webjars/**"
                        ).permitAll()

                        // IA
                        .requestMatchers(
                                "/ia/chat",
                                "/ia/**"
                        ).permitAll()

                        // IA: el modelo de predicción (servicio en Python) solo lo usa el coordinador
                        .requestMatchers("/api/ia/**", "/asistencia/api/**").hasRole("COORDINADOR")

                        // APP MÓVIL: login con JWT
                        .requestMatchers("/api/auth/login").permitAll()

                        // ANTES todo /api/** era público (cualquiera podía leer o borrar datos).
                        // Ahora cada API exige sesión o token y el rol adecuado.
                        .requestMatchers(HttpMethod.GET, "/api/login-config/*/habilitado").permitAll()
                        .requestMatchers("/api/login-config/**").hasRole("COORDINADOR")
                        .requestMatchers("/api/auth/registro").hasRole("COORDINADOR")
                        .requestMatchers("/api/usuarios/**").hasRole("COORDINADOR")
                        .requestMatchers(HttpMethod.POST, "/api/asistencias/manual").hasAnyRole("ADMIN", "COORDINADOR")
                        .requestMatchers("/api/sesiones/**").hasAnyRole("ADMIN", "COORDINADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/asistencias/**").hasAnyRole("ADMIN", "COORDINADOR")
                        .requestMatchers(HttpMethod.GET, "/api/asistencias", "/api/asistencias/clase/**",
                                "/api/asistencias/rango").hasAnyRole("ADMIN", "COORDINADOR")
                        .requestMatchers("/api/**").authenticated()

                        // ROLES
                        .requestMatchers("/registro").hasRole("COORDINADOR")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/coordinador/**").hasRole("COORDINADOR")
                        .requestMatchers("/estudiante/**").hasRole("ESTUDIANTE")

                        // CUALQUIER OTRA RUTA
                        .anyRequest().authenticated()
                )

                // LOGIN
                .formLogin(login -> login
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(customAuthenticationSuccessHandler())
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                // LOGOUT
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                // SESIONES
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .expiredUrl("/login?expired=true")
                );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {

        return new AuthenticationSuccessHandler() {

            @Override
            public void onAuthenticationSuccess(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    Authentication authentication
            ) throws IOException, ServletException {

                if (authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {

                    response.sendRedirect("/admin/dashboard");

                } else if (authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_COORDINADOR"))) {

                    response.sendRedirect("/coordinador/dashboard");

                } else if (authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ESTUDIANTE"))) {

                    response.sendRedirect("/estudiante/dashboard");

                } else {

                    response.sendRedirect("/login?error=sin-rol");
                }
            }
        };
    }
}
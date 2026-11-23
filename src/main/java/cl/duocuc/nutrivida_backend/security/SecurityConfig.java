package cl.duocuc.nutrivida_backend.security;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Seguridad del API: stateless con JWT en cookies httpOnly, CORS para el
 * dev server de Vite y autorización por método (GET público para catálogo;
 * administrador para mantenimiento; recepcionista o administrador para stock;
 * cliente autenticado para su carrito/historial).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filtroSeguridad(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(gestiona -> gestiona.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(autoriza -> autoriza
                        // Documentación y errores
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/error").permitAll()
                        // Autenticación: registro/login/refresh/logout son públicos
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/auth/login", "/api/v1/auth/registro",
                                "/api/v1/auth/refresh", "/api/v1/auth/logout").permitAll()
                        // Catálogo público
                        .requestMatchers(HttpMethod.GET, "/api/v1/productos/**", "/api/v1/categorias/**").permitAll()
                        // Gestión de usuarios: crear/activar/desactivar (recepción o admin)
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/**")
                                .hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA")
                        // Actualización de stock del catálogo (recepción o admin)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/productos/*/stock")
                                .hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA")
                        // Mantenimiento: todo lo demás de usuarios/categorías/productos es admin
                        .requestMatchers("/api/v1/usuarios/**", "/api/v1/categorias/**", "/api/v1/productos/**")
                                .hasRole("ADMINISTRADOR")
                        // Órdenes: listado general (admin o recepcionista) y cancelación (solo admin)
                        .requestMatchers(HttpMethod.GET, "/api/v1/ordenes").hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/ordenes/*/cancelar").hasRole("ADMINISTRADOR")
                        // Todo lo demás requiere sesión (crear órdenes, mías, perfil, me)
                        .anyRequest().authenticated())
                .exceptionHandling(excepciones -> excepciones
                        .authenticationEntryPoint(JsonSecurityHandlers.puntoEntrada())
                        .accessDeniedHandler(JsonSecurityHandlers.accesoDenegado()))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** Solo el dev server de Vite (localhost:5173) puede llamar al API con credenciales. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOrigins(List.of("http://localhost:5173"));
        configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("*"));
        configuracion.setAllowCredentials(true);
        configuracion.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", configuracion);
        return fuente;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

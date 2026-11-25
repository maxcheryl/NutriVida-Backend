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

/** Define qué rutas son públicas y qué roles pueden usar las demás. */
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
                        // Swagger y la ruta de error se pueden consultar sin iniciar sesión.
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/error").permitAll()
                        // Estas rutas permiten iniciar, renovar y cerrar la sesión.
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/auth/login", "/api/v1/auth/registro",
                                "/api/v1/auth/refresh", "/api/v1/auth/logout").permitAll()
                        // Cualquier visitante puede consultar el catálogo.
                        .requestMatchers(HttpMethod.GET, "/api/v1/productos/**", "/api/v1/categorias/**").permitAll()
                        // Administración y recepción pueden crear usuarios.
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/**")
                                .hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA")
                        // Administración y recepción pueden cambiar los cupos disponibles.
                        .requestMatchers(HttpMethod.PUT, "/api/v1/productos/*/stock")
                                .hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA")
                        // El resto de los cambios en usuarios, categorías y productos es solo para administración.
                        .requestMatchers("/api/v1/usuarios/**", "/api/v1/categorias/**", "/api/v1/productos/**")
                                .hasRole("ADMINISTRADOR")
                        // Recepción puede ver todas las órdenes; solo administración puede cancelarlas.
                        .requestMatchers(HttpMethod.GET, "/api/v1/ordenes").hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/ordenes/*/cancelar").hasRole("ADMINISTRADOR")
                        // Cualquier ruta no indicada arriba requiere una sesión válida.
                        .anyRequest().authenticated())
                .exceptionHandling(excepciones -> excepciones
                        .authenticationEntryPoint(JsonSecurityHandlers.puntoEntrada())
                        .accessDeniedHandler(JsonSecurityHandlers.accesoDenegado()))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** Permite al frontend local llamar a la API y enviar las cookies de sesión. */
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

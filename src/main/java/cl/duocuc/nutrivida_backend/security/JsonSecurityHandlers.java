package cl.duocuc.nutrivida_backend.security;

import cl.duocuc.nutrivida_backend.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

/** Devuelve 401/403 en JSON (mismo formato que el resto de la API). */
public final class JsonSecurityHandlers {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonSecurityHandlers() {
    }

    /** 401: no hay sesión o el token no es válido. */
    public static AuthenticationEntryPoint puntoEntrada() {
        return (solicitud, respuesta, excepcion) ->
                escribir(respuesta, HttpServletResponse.SC_UNAUTHORIZED,
                        "Debes iniciar sesión para acceder a este recurso.");
    }

    /** 403: la sesión existe pero el rol no permite la operación. */
    public static AccessDeniedHandler accesoDenegado() {
        return (solicitud, respuesta, excepcion) ->
                escribir(respuesta, HttpServletResponse.SC_FORBIDDEN,
                        "No tienes permisos para realizar esta operación.");
    }

    private static void escribir(HttpServletResponse respuesta, int estado, String mensaje) throws IOException {
        respuesta.setStatus(estado);
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        respuesta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        respuesta.getWriter().write(MAPPER.writeValueAsString(new ErrorResponse(mensaje)));
    }
}

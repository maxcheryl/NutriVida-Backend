package cl.duocuc.nutrivida_backend.security;

import cl.duocuc.nutrivida_backend.model.Usuario;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

/**
 * Generación y validación de JWT (HMAC-SHA256) y manejo de las cookies
 * httpOnly que guardan el access token (30 min) y el refresh token (7 días).
 */
@Service
public class JwtService {

    public static final String COOKIE_ACCESO = "nutrivida_token";
    public static final String COOKIE_REFRESH = "nutrivida_refresh";

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final long segundosAcceso;
    private final long segundosRefresh;

    public JwtService(
            @Value("${app.jwt.secret}") String secreto,
            @Value("${app.jwt.access-expiration-minutes}") long minutosAcceso,
            @Value("${app.jwt.refresh-expiration-days}") long diasRefresh) {
        SecretKey clave = new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.jwtEncoder = NimbusJwtEncoder.withSecretKey(clave)
                .algorithm(MacAlgorithm.HS256)
                .build();
        this.jwtDecoder = NimbusJwtDecoder.withSecretKey(clave)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        this.segundosAcceso = minutosAcceso * 60;
        this.segundosRefresh = diasRefresh * 24 * 60 * 60;
    }

    /** Access token: identifica al usuario en cada petición. */
    public String generarAcceso(Usuario usuario) {
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(String.valueOf(usuario.getId()))
                .issuedAt(ahora)
                .expiresAt(ahora.plusSeconds(segundosAcceso))
                .claim("correo", usuario.getCorreo())
                .claim("rol", usuario.getRol().getNombre())
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    /** Refresh token: permite renovar el access token sin volver a pedir la contraseña. */
    public String generarRefresh(Usuario usuario) {
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(String.valueOf(usuario.getId()))
                .issuedAt(ahora)
                .expiresAt(ahora.plusSeconds(segundosRefresh))
                .claim("tipo", "refresh")
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    /** Decodifica y valida firma/vigencia. Lanza JwtException si no es válido. */
    public Jwt decodificar(String token) throws JwtException {
        return jwtDecoder.decode(token);
    }

    /** true si el token es un refresh token válido (firma + vigencia + tipo). */
    public boolean esRefreshValido(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return "refresh".equals(jwt.getClaimAsString("tipo"));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getSegundosAcceso() {
        return segundosAcceso;
    }

    // ==================== COOKIES HTTPONLY ====================

    /** Escribe la cookie de acceso (httpOnly) en la respuesta. */
    public void agregarCookieAcceso(HttpServletResponse respuesta, String token) {
        agregarCookie(respuesta, COOKIE_ACCESO, token, (int) segundosAcceso);
    }

    /** Escribe la cookie de refresh (httpOnly) en la respuesta. */
    public void agregarCookieRefresh(HttpServletResponse respuesta, String token) {
        agregarCookie(respuesta, COOKIE_REFRESH, token, (int) segundosRefresh);
    }

    /** Borra ambas cookies (cierre de sesión). */
    public void limpiarCookies(HttpServletResponse respuesta) {
        agregarCookie(respuesta, COOKIE_ACCESO, "", 0);
        agregarCookie(respuesta, COOKIE_REFRESH, "", 0);
    }

    /** Lee el valor de una cookie por nombre; null si no existe. */
    public String leerCookie(HttpServletRequest solicitud, String nombre) {
        if (solicitud.getCookies() == null) return null;
        for (Cookie cookie : solicitud.getCookies()) {
            if (nombre.equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }

    private void agregarCookie(HttpServletResponse respuesta, String nombre, String valor, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(nombre, valor)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofSeconds(maxAge))
                .sameSite("Lax")
                .build();
        respuesta.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /** Id de usuario desde el subject del access token; -1 si es inválido. */
    public long idDesdeToken(String token) {
        try {
            return Long.parseLong(decodificar(token).getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return -1;
        }
    }
}

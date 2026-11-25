package cl.duocuc.nutrivida_backend.security;

import cl.duocuc.nutrivida_backend.model.Usuario;
import cl.duocuc.nutrivida_backend.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Lee el token de acceso de la cookie y reconoce al usuario de cada solicitud. */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest solicitud, HttpServletResponse respuesta, FilterChain cadena)
            throws ServletException, IOException {

        String token = jwtService.leerCookie(solicitud, JwtService.COOKIE_ACCESO);
        if (token != null && !token.isBlank()
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                long id = Long.parseLong(jwtService.decodificar(token).getSubject());
                Usuario usuario = usuarioRepository.findById(id).orElse(null);
                if (usuario != null && usuario.isActivo()) {
                    // El rol se usa después para decidir qué rutas puede usar esta cuenta.
                    var autoridad = new SimpleGrantedAuthority(
                            "ROLE_" + usuario.getRol().getNombre().toUpperCase());
                    var autenticacion = new UsernamePasswordAuthenticationToken(
                            usuario, null, java.util.List.of(autoridad));
                    autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(solicitud));
                    SecurityContextHolder.getContext().setAuthentication(autenticacion);
                }
            } catch (Exception e) {
                // Si el token no sirve, la solicitud sigue como visitante.
                // Las rutas protegidas responderán que falta iniciar sesión.
            }
        }
        cadena.doFilter(solicitud, respuesta);
    }
}

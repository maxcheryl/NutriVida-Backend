package cl.duocuc.nutrivida_backend.service;

import cl.duocuc.nutrivida_backend.dto.LoginRequest;
import cl.duocuc.nutrivida_backend.dto.RegistroRequest;
import cl.duocuc.nutrivida_backend.dto.UsuarioResponse;
import cl.duocuc.nutrivida_backend.exception.CorreoDuplicadoException;
import cl.duocuc.nutrivida_backend.exception.CredencialesInvalidasException;
import cl.duocuc.nutrivida_backend.exception.RecursoNoEncontradoException;
import cl.duocuc.nutrivida_backend.model.Rol;
import cl.duocuc.nutrivida_backend.model.Usuario;
import cl.duocuc.nutrivida_backend.repository.RolRepository;
import cl.duocuc.nutrivida_backend.repository.UsuarioRepository;
import cl.duocuc.nutrivida_backend.security.JwtService;
import cl.duocuc.nutrivida_backend.validation.ReglasNegocio;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Autenticación: login con bloqueo a los 3 intentos, registro público de
 * clientes, renovación de sesión por refresh token y cierre de sesión.
 * La sesión viaja en cookies httpOnly (access 30 min / refresh 7 días).
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String MENSAJE_INVALIDOS = "Correo o contraseña inválidos";
    private static final String MENSAJE_BLOQUEADO =
            "Cuenta bloqueada por múltiples intentos fallidos. Contacte al administrador.";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /** Login: valida credenciales, maneja intentos fallidos y emite cookies. */
    @Transactional(noRollbackFor = CredencialesInvalidasException.class)
    public UsuarioResponse login(LoginRequest datos, HttpServletResponse respuesta) {
        String correo = datos.correo().trim().toLowerCase();
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new CredencialesInvalidasException(MENSAJE_INVALIDOS));

        if (!usuario.isActivo()) {
            throw new CredencialesInvalidasException("La cuenta está deshabilitada. Contacte al administrador.");
        }
        if (usuario.getIntentosFallidos() >= ReglasNegocio.MAX_INTENTOS_FALLIDOS) {
            throw new CredencialesInvalidasException(MENSAJE_BLOQUEADO);
        }

        if (!passwordEncoder.matches(datos.contrasena(), usuario.getContrasena())) {
            usuario.setIntentosFallidos(usuario.getIntentosFallidos() + 1);
            usuarioRepository.save(usuario);
            if (usuario.getIntentosFallidos() >= ReglasNegocio.MAX_INTENTOS_FALLIDOS) {
                throw new CredencialesInvalidasException(MENSAJE_BLOQUEADO);
            }
            int restantes = ReglasNegocio.MAX_INTENTOS_FALLIDOS - usuario.getIntentosFallidos();
            throw new CredencialesInvalidasException(
                    "Correo o contraseña incorrectos. Intentos restantes: " + restantes);
        }

        usuario.setIntentosFallidos(0);
        usuarioRepository.save(usuario);
        emitirCookies(respuesta, usuario);
        return UsuarioResponse.desde(usuario);
    }

    /** Registro público: siempre crea un cliente con el dominio @duocuc.cl. */
    @Transactional
    public UsuarioResponse registro(RegistroRequest datos, HttpServletResponse respuesta) {
        String correo = datos.correo().trim().toLowerCase();

        ReglasNegocio.validarContrasena(datos.contrasena());
        ReglasNegocio.validarDominioCorreo(correo);
        ReglasNegocio.validarEdad(datos.fechaNacimiento(), "El registro");
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new CorreoDuplicadoException("El correo ya se encuentra registrado");
        }

        Rol rolCliente = rolRepository.findByNombre("cliente")
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol cliente no encontrado"));

        Usuario nuevo = new Usuario();
        nuevo.setNombre(datos.nombre().trim());
        nuevo.setApellido(datos.apellido().trim());
        nuevo.setCorreo(correo);
        nuevo.setFechaNacimiento(datos.fechaNacimiento());
        nuevo.setContrasena(passwordEncoder.encode(datos.contrasena()));
        nuevo.setGenero(datos.genero());
        nuevo.setRegion(datos.region());
        nuevo.setDireccion(datos.direccion());
        nuevo.setRol(rolCliente);
        nuevo.setActivo(true);
        nuevo.setIntentosFallidos(0);
        Usuario guardado = usuarioRepository.save(nuevo);

        emitirCookies(respuesta, guardado);
        return UsuarioResponse.desde(guardado);
    }

    /**
     * Renovación de sesión: valida la cookie de refresh y emite un nuevo
     * par de cookies. Limpia las cookies si el refresh token no es válido.
     */
    @Transactional(readOnly = true)
    public UsuarioResponse refresh(HttpServletRequest solicitud, HttpServletResponse respuesta) {
        String token = jwtService.leerCookie(solicitud, JwtService.COOKIE_REFRESH);
        if (token == null || !jwtService.esRefreshValido(token)) {
            jwtService.limpiarCookies(respuesta);
            throw new CredencialesInvalidasException("Sesión expirada. Inicia sesión nuevamente.");
        }

        long id;
        try {
            id = Long.parseLong(jwtService.decodificar(token).getSubject());
        } catch (Exception e) {
            jwtService.limpiarCookies(respuesta);
            throw new CredencialesInvalidasException("Sesión expirada. Inicia sesión nuevamente.");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .filter(Usuario::isActivo)
                .orElseThrow(() -> {
                    jwtService.limpiarCookies(respuesta);
                    return new CredencialesInvalidasException("Sesión expirada. Inicia sesión nuevamente.");
                });

        emitirCookies(respuesta, usuario);
        return UsuarioResponse.desde(usuario);
    }

    /** Cierra la sesión borrando ambas cookies. */
    public void logout(HttpServletResponse respuesta) {
        jwtService.limpiarCookies(respuesta);
    }

    /** Usuario autenticado actual (cookie de acceso ya validada por el filtro). */
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerActual(Usuario usuario) {
        return UsuarioResponse.desde(usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado")));
    }

    /** Escribe access + refresh cookies httpOnly. */
    private void emitirCookies(HttpServletResponse respuesta, Usuario usuario) {
        jwtService.agregarCookieAcceso(respuesta, jwtService.generarAcceso(usuario));
        jwtService.agregarCookieRefresh(respuesta, jwtService.generarRefresh(usuario));
    }
}

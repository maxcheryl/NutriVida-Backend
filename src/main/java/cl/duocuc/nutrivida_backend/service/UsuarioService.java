package cl.duocuc.nutrivida_backend.service;

import cl.duocuc.nutrivida_backend.dto.PerfilRequest;
import cl.duocuc.nutrivida_backend.dto.UsuarioRequest;
import cl.duocuc.nutrivida_backend.dto.UsuarioResponse;
import cl.duocuc.nutrivida_backend.exception.CorreoDuplicadoException;
import cl.duocuc.nutrivida_backend.exception.OperacionNoPermitidaException;
import cl.duocuc.nutrivida_backend.exception.RecursoNoEncontradoException;
import cl.duocuc.nutrivida_backend.model.Rol;
import cl.duocuc.nutrivida_backend.model.Usuario;
import cl.duocuc.nutrivida_backend.repository.RolRepository;
import cl.duocuc.nutrivida_backend.repository.UsuarioRepository;
import cl.duocuc.nutrivida_backend.validation.ReglasNegocio;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestión administrativa de usuarios (alta, edición, baja lógica y cambio
 * de rol) y edición del perfil propio. La eliminación es lógica para no
 * perder el historial de órdenes.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar(String busqueda) {
        List<Usuario> usuarios = usuarioRepository.findAll();
        String filtro = busqueda == null ? "" : busqueda.trim().toLowerCase();
        return usuarios.stream()
                .filter(u -> filtro.isEmpty()
                        || u.getNombre().toLowerCase().contains(filtro)
                        || u.getApellido().toLowerCase().contains(filtro)
                        || u.getCorreo().toLowerCase().contains(filtro))
                .map(UsuarioResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.desde(buscar(id));
    }

    /** Alta de usuario (cliente, recepcionista o administrador). */
    @Transactional
    public UsuarioResponse crear(UsuarioRequest datos) {
        String correo = datos.correo().trim().toLowerCase();
        ReglasNegocio.validarDominioCorreo(correo);
        ReglasNegocio.validarEdad(datos.fechaNacimiento(), "El registro");
        if (datos.contrasena() == null || datos.contrasena().isBlank()) {
            throw new OperacionNoPermitidaException("La contraseña es obligatoria para crear un usuario");
        }
        ReglasNegocio.validarContrasena(datos.contrasena());
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new CorreoDuplicadoException("El correo ya se encuentra registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(datos.nombre().trim());
        usuario.setApellido(datos.apellido().trim());
        usuario.setCorreo(correo);
        usuario.setFechaNacimiento(datos.fechaNacimiento());
        usuario.setContrasena(passwordEncoder.encode(datos.contrasena()));
        usuario.setGenero(datos.genero());
        usuario.setRegion(datos.region());
        usuario.setDireccion(datos.direccion());
        usuario.setRol(obtenerRol(datos.rol() == null || datos.rol().isBlank() ? "cliente" : datos.rol()));
        usuario.setActivo(true);
        usuario.setIntentosFallidos(0);
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    /** Edición completa desde el panel (la contraseña solo si se envía). */
    @Transactional
    public UsuarioResponse editar(Long id, UsuarioRequest datos) {
        Usuario usuario = buscar(id);
        String correo = datos.correo().trim().toLowerCase();

        ReglasNegocio.validarDominioCorreo(correo);
        ReglasNegocio.validarEdad(datos.fechaNacimiento(), "El perfil");
        if (usuarioRepository.existsByCorreoAndIdNot(correo, id)) {
            throw new CorreoDuplicadoException("El correo ya se encuentra registrado");
        }

        usuario.setNombre(datos.nombre().trim());
        usuario.setApellido(datos.apellido().trim());
        usuario.setCorreo(correo);
        usuario.setFechaNacimiento(datos.fechaNacimiento());
        usuario.setGenero(datos.genero());
        usuario.setRegion(datos.region());
        usuario.setDireccion(datos.direccion());

        if (datos.contrasena() != null && !datos.contrasena().isBlank()) {
            ReglasNegocio.validarContrasena(datos.contrasena());
            usuario.setContrasena(passwordEncoder.encode(datos.contrasena()));
        }
        if (datos.rol() != null && !datos.rol().isBlank()) {
            usuario.setRol(obtenerRol(datos.rol()));
        }
        // Reabrir la cuenta al editarla (los intentos previos ya no aplican)
        usuario.setIntentosFallidos(0);
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    /** Baja lógica: conserva usuarios y órdenes, solo cambia el estado. */
    @Transactional
    public UsuarioResponse desactivar(Long id) {
        Usuario usuario = buscar(id);
        usuario.setActivo(false);
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    /** Reactivación de una cuenta desactivada. */
    @Transactional
    public UsuarioResponse activar(Long id) {
        Usuario usuario = buscar(id);
        usuario.setActivo(true);
        usuario.setIntentosFallidos(0);
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    /** Perfil del usuario autenticado: datos personales + cambio de contraseña opcional. */
    @Transactional
    public UsuarioResponse actualizarPerfil(Usuario autenticado, PerfilRequest datos) {
        Usuario usuario = buscar(autenticado.getId());
        String correo = datos.correo().trim().toLowerCase();

        ReglasNegocio.validarDominioCorreo(correo);
        ReglasNegocio.validarEdad(datos.fechaNacimiento(), "El perfil");
        if (usuarioRepository.existsByCorreoAndIdNot(correo, usuario.getId())) {
            throw new CorreoDuplicadoException("El correo ya se encuentra registrado");
        }

        usuario.setNombre(datos.nombre().trim());
        usuario.setApellido(datos.apellido().trim());
        usuario.setCorreo(correo);
        usuario.setFechaNacimiento(datos.fechaNacimiento());
        usuario.setGenero(datos.genero());
        usuario.setRegion(datos.region());
        usuario.setDireccion(datos.direccion());

        boolean quiereCambiarContrasena = (datos.contrasenaActual() != null && !datos.contrasenaActual().isBlank())
                || (datos.contrasenaNueva() != null && !datos.contrasenaNueva().isBlank());
        if (quiereCambiarContrasena) {
            if (datos.contrasenaActual() == null || datos.contrasenaActual().isBlank()
                    || datos.contrasenaNueva() == null || datos.contrasenaNueva().isBlank()) {
                throw new OperacionNoPermitidaException(
                        "Para cambiar la contraseña completa: contraseña actual y nueva contraseña");
            }
            if (!passwordEncoder.matches(datos.contrasenaActual(), usuario.getContrasena())) {
                throw new OperacionNoPermitidaException("La contraseña actual es incorrecta");
            }
            ReglasNegocio.validarContrasena(datos.contrasenaNueva());
            usuario.setContrasena(passwordEncoder.encode(datos.contrasenaNueva()));
        }

        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    private Rol obtenerRol(String nombre) {
        return rolRepository.findByNombre(nombre.toLowerCase())
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado: " + nombre));
    }
}

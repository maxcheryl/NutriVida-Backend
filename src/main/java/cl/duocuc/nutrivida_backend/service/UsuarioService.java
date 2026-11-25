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

/** Administra las cuentas y permite que cada persona actualice su perfil. */
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

    /** Crea una cuenta nueva con los datos validados y la contraseña protegida. */
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

    /** Actualiza una cuenta; la contraseña y el rol cambian solo si se envían. */
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
        // Al guardar los cambios, se restablece el contador de intentos fallidos.
        usuario.setIntentosFallidos(0);
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    /** Desactiva la cuenta sin borrar sus datos ni su historial. */
    @Transactional
    public UsuarioResponse desactivar(Long id) {
        Usuario usuario = buscar(id);
        usuario.setActivo(false);
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    /** Reactiva la cuenta y reinicia sus intentos fallidos. */
    @Transactional
    public UsuarioResponse activar(Long id) {
        Usuario usuario = buscar(id);
        usuario.setActivo(true);
        usuario.setIntentosFallidos(0);
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    /** Actualiza el perfil y cambia la contraseña solo si se solicitan ambos campos. */
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
            // Para cambiarla, la persona debe enviar y validar ambas contraseñas.
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

package cl.duocuc.nutrivida_backend.dto;

import cl.duocuc.nutrivida_backend.model.Usuario;
import java.time.LocalDate;

/** Datos públicos de un usuario: nunca incluye la contraseña. */
public record UsuarioResponse(
        Long id,
        String nombre,
        String apellido,
        String correo,
        LocalDate fechaNacimiento,
        String genero,
        String region,
        String direccion,
        String rol,
        boolean activo) {

    /** Proyecta la entidad a la forma pública que consume el frontend. */
    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getCorreo(),
                usuario.getFechaNacimiento(),
                usuario.getGenero(),
                usuario.getRegion(),
                usuario.getDireccion(),
                usuario.getRol().getNombre(),
                usuario.isActivo());
    }
}

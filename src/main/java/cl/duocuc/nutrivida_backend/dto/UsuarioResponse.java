package cl.duocuc.nutrivida_backend.dto;

import cl.duocuc.nutrivida_backend.model.Usuario;
import java.time.LocalDate;

/** Datos de la cuenta que pueden mostrarse en la aplicación. */
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

    /** Copia a la respuesta los datos que se pueden compartir. */
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

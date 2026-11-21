package cl.duocuc.nutrivida_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Creación/edición de usuarios desde el panel administrativo o la
 * recepción. `contrasena` es obligatoria al crear; opcional al editar
 * (si se envía, debe acompañarse de la contraseña actual en el perfil).
 */
public record UsuarioRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 50)
        String nombre,

        @NotBlank(message = "El apellido es obligatorio.")
        @Size(max = 50)
        String apellido,

        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo no tiene un formato válido.")
        @Size(max = 100)
        String correo,

        @NotNull(message = "La fecha de nacimiento es obligatoria.")
        @Past(message = "La fecha de nacimiento debe ser pasada.")
        LocalDate fechaNacimiento,

        String contrasena,

        @Size(max = 20)
        String genero,

        @Size(max = 50)
        String region,

        @Size(max = 150)
        String direccion,

        /** Solo el administrador puede asignar roles distintos de cliente. */
        String rol) {
}

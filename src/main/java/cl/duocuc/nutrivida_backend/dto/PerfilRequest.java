package cl.duocuc.nutrivida_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Edición del perfil del usuario autenticado. El rol no se modifica aquí.
 * Para cambiar la contraseña se envían `contrasenaActual` y `contrasenaNueva`.
 */
public record PerfilRequest(
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

        @Size(max = 20)
        String genero,

        @Size(max = 50)
        String region,

        @Size(max = 150)
        String direccion,

        @Size(max = 100)
        String contrasenaActual,

        @Size(max = 100)
        String contrasenaNueva) {
}

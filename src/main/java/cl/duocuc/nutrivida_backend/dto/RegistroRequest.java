package cl.duocuc.nutrivida_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** Solicitud de registro público: siempre crea un cliente. */
public record RegistroRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 50, message = "El nombre no puede superar 50 caracteres.")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio.")
        @Size(max = 50, message = "El apellido no puede superar 50 caracteres.")
        String apellido,

        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo no tiene un formato válido.")
        @Size(max = 100, message = "El correo no puede superar 100 caracteres.")
        String correo,

        @NotNull(message = "La fecha de nacimiento es obligatoria.")
        @Past(message = "La fecha de nacimiento debe ser pasada.")
        LocalDate fechaNacimiento,

        @NotBlank(message = "La contraseña es obligatoria.")
        String contrasena,

        @Size(max = 20, message = "El género no puede superar 20 caracteres.")
        String genero,

        @Size(max = 50, message = "La región no puede superar 50 caracteres.")
        String region,

        @Size(max = 150, message = "La dirección no puede superar 150 caracteres.")
        String direccion) {
}

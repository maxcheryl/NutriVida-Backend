package cl.duocuc.nutrivida_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Creación/edición de categoría. */
public record CategoriaRequest(
        @NotBlank(message = "El nombre de la categoría es obligatorio.")
        @Size(max = 60, message = "El nombre no puede superar 60 caracteres.")
        String nombre) {
}

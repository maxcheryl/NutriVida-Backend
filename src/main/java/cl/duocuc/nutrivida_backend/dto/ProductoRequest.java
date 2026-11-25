package cl.duocuc.nutrivida_backend.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** Datos que envía el panel para crear o actualizar un servicio. */
public record ProductoRequest(
        @NotBlank(message = "El código es obligatorio.")
        @Size(max = 10, message = "El código no puede superar 10 caracteres.")
        String codigo,

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 120, message = "El nombre no puede superar 120 caracteres.")
        String nombre,

        @NotBlank(message = "La categoría es obligatoria.")
        String tipo,

        @Size(max = 20)
        String duracion,

        @Size(max = 40)
        String modalidad,

        @NotNull(message = "El precio es obligatorio.")
        @DecimalMin(value = "1000", message = "El precio mínimo es 1.000.")
        @DecimalMax(value = "10000000", message = "El precio máximo es 10.000.000.")
        BigDecimal precio,

        @NotNull(message = "Los cupos son obligatorios.")
        Integer cuposIniciales,

        @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres.")
        String descripcion,

        @Size(max = 500, message = "La URL de la imagen no puede superar 500 caracteres.")
        String imagenUrl) {
}

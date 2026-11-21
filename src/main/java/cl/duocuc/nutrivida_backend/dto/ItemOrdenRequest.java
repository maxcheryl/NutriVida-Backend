package cl.duocuc.nutrivida_backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Línea del carrito a confirmar: producto por código + cantidad. */
public record ItemOrdenRequest(
        @NotBlank(message = "El código del producto es obligatorio.")
        String codigo,

        @NotNull(message = "La cantidad es obligatoria.")
        @Min(value = 1, message = "La cantidad mínima es 1.")
        Integer cantidad) {
}

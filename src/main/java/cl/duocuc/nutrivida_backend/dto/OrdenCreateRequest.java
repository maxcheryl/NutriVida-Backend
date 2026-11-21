package cl.duocuc.nutrivida_backend.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/** Confirmación del carrito: lista de productos con sus cantidades. */
public record OrdenCreateRequest(
        @NotEmpty(message = "El carrito no puede estar vacío.")
        List<ItemOrdenRequest> items) {
}

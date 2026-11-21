package cl.duocuc.nutrivida_backend.dto;

import jakarta.validation.constraints.NotNull;

/** Reemplazo de los cupos LIBRES disponibles de un producto (0 a 1000). */
public record StockRequest(
        @NotNull(message = "Los cupos son obligatorios.")
        Integer cupos) {
}

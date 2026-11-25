package cl.duocuc.nutrivida_backend.dto;

import jakarta.validation.constraints.NotNull;

/** Nueva cantidad de cupos disponibles para un servicio. */
public record StockRequest(
        @NotNull(message = "Los cupos son obligatorios.")
        Integer cupos) {
}

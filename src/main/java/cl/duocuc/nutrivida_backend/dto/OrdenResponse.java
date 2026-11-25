package cl.duocuc.nutrivida_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Datos de una reserva que la API devuelve al frontend. */
public record OrdenResponse(
        Long id,
        LocalDateTime fecha,
        BigDecimal total,
        String estado,
        String cliente,
        List<DetalleOrdenResponse> detalles) {
}

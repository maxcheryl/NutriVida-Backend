package cl.duocuc.nutrivida_backend.dto;

import java.math.BigDecimal;

/** Línea del historial con el precio histórico al momento de la compra. */
public record DetalleOrdenResponse(
        Long id,
        String productoCodigo,
        String productoNombre,
        int cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal) {
}

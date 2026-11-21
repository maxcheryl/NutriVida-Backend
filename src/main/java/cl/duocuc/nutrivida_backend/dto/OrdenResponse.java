package cl.duocuc.nutrivida_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Orden (compra/reserva): identificador, fecha, estado, total,
 * cliente (en el listado administrativo) y detalle de productos.
 */
public record OrdenResponse(
        Long id,
        LocalDateTime fecha,
        BigDecimal total,
        String estado,
        String cliente,
        List<DetalleOrdenResponse> detalles) {
}

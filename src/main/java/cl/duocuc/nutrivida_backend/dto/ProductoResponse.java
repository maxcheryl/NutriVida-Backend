package cl.duocuc.nutrivida_backend.dto;

import java.math.BigDecimal;

/** Producto del catálogo tal como lo consume el frontend (`tipo` = nombre de categoría). */
public record ProductoResponse(
        Long id,
        String codigo,
        String tipo,
        String nombre,
        String duracion,
        String modalidad,
        BigDecimal precio,
        int cuposIniciales,
        String descripcion,
        String imagenUrl) {
}

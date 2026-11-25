package cl.duocuc.nutrivida_backend.dto;

import java.math.BigDecimal;

/** Datos de un servicio que el backend envía al frontend. */
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

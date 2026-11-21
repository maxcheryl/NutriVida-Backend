package cl.duocuc.nutrivida_backend.dto;

/** Categoría con el conteo de productos asociados (para el panel admin). */
public record CategoriaResponse(Long id, String nombre, long totalProductos) {
}

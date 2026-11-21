package cl.duocuc.nutrivida_backend.repository;

import cl.duocuc.nutrivida_backend.model.Producto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    /** Catálogo: solo productos activos. */
    List<Producto> findByActivoTrue();

    /** Productos de una categoría (para validaciones de borrado). */
    long countByCategoriaId(Long categoriaId);

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.categoria.nombre = :categoria")
    List<Producto> buscarPorCategoria(@Param("categoria") String categoria);

    /**
     * Descuento atómico de cupos: solo aplica si quedan suficientes.
     * Devuelve 0 filas afectadas cuando no hay stock (evita sobreventa).
     */
    @Modifying
    @Query("UPDATE Producto p SET p.cuposIniciales = p.cuposIniciales - :cantidad "
            + "WHERE p.id = :id AND p.activo = true AND p.cuposIniciales >= :cantidad")
    int descontarCupos(@Param("id") Long id, @Param("cantidad") int cantidad);

    /** Devolución de cupos al cancelar una orden. */
    @Modifying
    @Query("UPDATE Producto p SET p.cuposIniciales = p.cuposIniciales + :cantidad WHERE p.id = :id")
    int devolverCupos(@Param("id") Long id, @Param("cantidad") int cantidad);
}

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

    /** Devuelve los servicios que siguen visibles en el catálogo. */
    List<Producto> findByActivoTrue();

    /** Cuenta los servicios de una categoría antes de eliminarla. */
    long countByCategoriaId(Long categoriaId);

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.categoria.nombre = :categoria")
    List<Producto> buscarPorCategoria(@Param("categoria") String categoria);

    /**
     * Descuenta cupos solo si todavía hay suficientes.
     * Devuelve cero si otra reserva ya ocupó los últimos cupos.
     */
    @Modifying
    @Query("UPDATE Producto p SET p.cuposIniciales = p.cuposIniciales - :cantidad "
            + "WHERE p.id = :id AND p.activo = true AND p.cuposIniciales >= :cantidad")
    int descontarCupos(@Param("id") Long id, @Param("cantidad") int cantidad);

    /** Devuelve al catálogo los cupos de una orden cancelada. */
    @Modifying
    @Query("UPDATE Producto p SET p.cuposIniciales = p.cuposIniciales + :cantidad WHERE p.id = :id")
    int devolverCupos(@Param("id") Long id, @Param("cantidad") int cantidad);
}

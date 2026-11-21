package cl.duocuc.nutrivida_backend.repository;

import cl.duocuc.nutrivida_backend.model.Categoria;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    boolean existsByProductosId(Long productoId);

    /** Categorías ordenadas alfabéticamente para el catálogo. */
    List<Categoria> findAllByOrderByNombreAsc();
}

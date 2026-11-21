package cl.duocuc.nutrivida_backend.repository;

import cl.duocuc.nutrivida_backend.model.Orden;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, Long> {

    /** Historial del cliente, más reciente primero. */
    List<Orden> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    /** Listado administrativo general, más reciente primero. */
    List<Orden> findAllByOrderByFechaDesc();

    /** Detalle con líneas ya cargadas (evita lazy en la respuesta). */
    @Query("SELECT o FROM Orden o LEFT JOIN FETCH o.detalles WHERE o.id = :id")
    Optional<Orden> buscarConDetalles(@Param("id") Long id);
}

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

    /** Busca las reservas de una persona, empezando por la más reciente. */
    List<Orden> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    /** Busca todas las reservas, empezando por la más reciente. */
    List<Orden> findAllByOrderByFechaDesc();

    /** Busca una orden y carga sus líneas antes de preparar la respuesta. */
    @Query("SELECT o FROM Orden o LEFT JOIN FETCH o.detalles WHERE o.id = :id")
    Optional<Orden> buscarConDetalles(@Param("id") Long id);
}

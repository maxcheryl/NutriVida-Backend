package cl.duocuc.nutrivida_backend.repository;

import cl.duocuc.nutrivida_backend.model.Rol;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Busca los roles guardados en la base de datos. */
@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(String nombre);
}

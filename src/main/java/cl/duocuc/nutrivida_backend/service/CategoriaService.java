package cl.duocuc.nutrivida_backend.service;

import cl.duocuc.nutrivida_backend.dto.CategoriaRequest;
import cl.duocuc.nutrivida_backend.dto.CategoriaResponse;
import cl.duocuc.nutrivida_backend.exception.ConflictoException;
import cl.duocuc.nutrivida_backend.exception.RecursoNoEncontradoException;
import cl.duocuc.nutrivida_backend.model.Categoria;
import cl.duocuc.nutrivida_backend.repository.CategoriaRepository;
import cl.duocuc.nutrivida_backend.repository.ProductoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Crea, consulta, actualiza y elimina las categorías del catálogo. */
@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAllByOrderByNombreAsc().stream()
                .map(c -> new CategoriaResponse(
                        c.getId(), c.getNombre(),
                        productoRepository.countByCategoriaId(c.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse obtener(Long id) {
        Categoria categoria = buscar(id);
        return new CategoriaResponse(categoria.getId(), categoria.getNombre(),
                productoRepository.countByCategoriaId(categoria.getId()));
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest datos) {
        String nombre = datos.nombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ConflictoException("Ya existe una categoría con ese nombre");
        }
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        Categoria guardada = categoriaRepository.save(categoria);
        return new CategoriaResponse(guardada.getId(), guardada.getNombre(), 0);
    }

    @Transactional
    public CategoriaResponse editar(Long id, CategoriaRequest datos) {
        Categoria categoria = buscar(id);
        String nombre = datos.nombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ConflictoException("Ya existe una categoría con ese nombre");
        }
        categoria.setNombre(nombre);
        Categoria guardada = categoriaRepository.save(categoria);
        return new CategoriaResponse(guardada.getId(), guardada.getNombre(),
                productoRepository.countByCategoriaId(guardada.getId()));
    }

    /** Elimina la categoría solo si no tiene servicios asociados. */
    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = buscar(id);
        long productos = productoRepository.countByCategoriaId(id);
        if (productos > 0) {
            // Evita dejar productos sin una categoría válida.
            throw new ConflictoException(
                    "No se puede eliminar la categoría: tiene " + productos + " producto(s) asociado(s)");
        }
        categoriaRepository.delete(categoria);
    }

    private Categoria buscar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada"));
    }
}

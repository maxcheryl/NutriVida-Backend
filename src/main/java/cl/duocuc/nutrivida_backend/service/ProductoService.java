package cl.duocuc.nutrivida_backend.service;

import cl.duocuc.nutrivida_backend.dto.CategoriaRequest;
import cl.duocuc.nutrivida_backend.dto.ProductoRequest;
import cl.duocuc.nutrivida_backend.dto.ProductoResponse;
import cl.duocuc.nutrivida_backend.dto.StockRequest;
import cl.duocuc.nutrivida_backend.exception.ConflictoException;
import cl.duocuc.nutrivida_backend.exception.OperacionNoPermitidaException;
import cl.duocuc.nutrivida_backend.exception.RecursoNoEncontradoException;
import cl.duocuc.nutrivida_backend.model.Categoria;
import cl.duocuc.nutrivida_backend.model.Producto;
import cl.duocuc.nutrivida_backend.repository.CategoriaRepository;
import cl.duocuc.nutrivida_backend.repository.ProductoRepository;
import cl.duocuc.nutrivida_backend.validation.ReglasNegocio;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Catálogo de productos: lectura pública (solo activos) y mantenimiento
 * administrativo (alta, edición, baja lógica) más la actualización de
 * cupos libres que también puede hacer la recepción.
 */
@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    /** Catálogo público; opcionalmente filtrado por nombre de categoría. */
    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(String categoria) {
        List<Producto> productos = productoRepository.findByActivoTrue();
        if (categoria != null && !categoria.isBlank()) {
            String filtro = categoria.trim();
            productos = productos.stream()
                    .filter(p -> p.getCategoria().getNombre().equalsIgnoreCase(filtro))
                    .toList();
        }
        return productos.stream().map(ProductoService::respuesta).toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        Producto producto = productoRepository.findById(id)
                .filter(Producto::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
        return respuesta(producto);
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorCodigo(String codigo) {
        Producto producto = productoRepository.findByCodigo(codigo.trim().toUpperCase())
                .filter(Producto::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con código: " + codigo));
        return respuesta(producto);
    }

    /** Alta de producto en el panel administrativo. */
    @Transactional
    public ProductoResponse crear(ProductoRequest datos) {
        String codigo = datos.codigo().trim().toUpperCase();
        ReglasNegocio.validarPrecio(datos.precio());
        ReglasNegocio.validarCupos(datos.cuposIniciales());
        if (productoRepository.existsByCodigo(codigo)) {
            throw new ConflictoException("Ya existe un producto con el código " + codigo);
        }

        Producto producto = new Producto();
        producto.setCodigo(codigo);
        producto.setNombre(datos.nombre().trim());
        producto.setCategoria(obtenerCategoria(datos.tipo()));
        producto.setDuracion(datos.duracion());
        producto.setModalidad(datos.modalidad());
        producto.setPrecio(datos.precio());
        producto.setCuposIniciales(datos.cuposIniciales());
        producto.setDescripcion(datos.descripcion());
        producto.setImagen(datos.imagen());
        producto.setImagenUrl(datos.imagenUrl());
        producto.setActivo(true);
        return respuesta(productoRepository.save(producto));
    }

    /** Edición de producto desde el panel administrativo. */
    @Transactional
    public ProductoResponse editar(Long id, ProductoRequest datos) {
        return editar(String.valueOf(id), datos);
    }

    @Transactional
    public ProductoResponse editar(String identificador, ProductoRequest datos) {
        Producto producto = buscar(identificador);
        String codigo = datos.codigo().trim().toUpperCase();
        ReglasNegocio.validarPrecio(datos.precio());
        ReglasNegocio.validarCupos(datos.cuposIniciales());
        if (!producto.getCodigo().equals(codigo) && productoRepository.existsByCodigo(codigo)) {
            throw new ConflictoException("Ya existe un producto con el código " + codigo);
        }

        producto.setCodigo(codigo);
        producto.setNombre(datos.nombre().trim());
        producto.setCategoria(obtenerCategoria(datos.tipo()));
        producto.setDuracion(datos.duracion());
        producto.setModalidad(datos.modalidad());
        producto.setPrecio(datos.precio());
        producto.setCuposIniciales(datos.cuposIniciales());
        producto.setDescripcion(datos.descripcion());
        producto.setImagen(datos.imagen());
        producto.setImagenUrl(datos.imagenUrl());
        return respuesta(productoRepository.save(producto));
    }

    /** Baja lógica: el producto sale del catálogo pero el historial se conserva. */
    @Transactional
    public ProductoResponse eliminar(Long id) {
        return eliminar(String.valueOf(id));
    }

    @Transactional
    public ProductoResponse eliminar(String identificador) {
        Producto producto = buscar(identificador);
        producto.setActivo(false);
        return respuesta(productoRepository.save(producto));
    }

    /**
     * Actualiza los cupos LIBRES (admin o recepción). El valor ingresado es
     * directamente el disponible actual, igual que en el panel original.
     */
    @Transactional
    public ProductoResponse actualizarStock(Long id, StockRequest datos) {
        return actualizarStock(String.valueOf(id), datos);
    }

    @Transactional
    public ProductoResponse actualizarStock(String identificador, StockRequest datos) {
        Producto producto = buscar(identificador);
        int cupos = datos.cupos();
        if (cupos < 0 || cupos > ReglasNegocio.STOCK_MAXIMO) {
            throw new OperacionNoPermitidaException(
                    "El stock de " + producto.getCodigo() + " debe ser un número entero entre 0 y "
                            + ReglasNegocio.STOCK_MAXIMO);
        }
        producto.setCuposIniciales(cupos);
        return respuesta(productoRepository.save(producto));
    }

    private Producto buscar(String identificador) {
        if (identificador.matches("\\d+")) {
            return productoRepository.findById(Long.parseLong(identificador))
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
        }
        return productoRepository.findByCodigo(identificador.trim().toUpperCase())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con código: " + identificador));
    }

    private Categoria obtenerCategoria(String nombreTipo) {
        if (nombreTipo == null || nombreTipo.isBlank()) {
            throw new OperacionNoPermitidaException("La categoría es obligatoria");
        }
        return categoriaRepository.findByNombreIgnoreCase(nombreTipo.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada: " + nombreTipo));
    }

    /** Proyecta la entidad a la forma que consume el frontend (`tipo` = categoría). */
    public static ProductoResponse respuesta(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getCodigo(),
                producto.getCategoria().getNombre(),
                producto.getNombre(),
                producto.getDuracion(),
                producto.getModalidad(),
                producto.getPrecio(),
                producto.getCuposIniciales(),
                producto.getDescripcion(),
                producto.getImagen(),
                producto.getImagenUrl());
    }
}

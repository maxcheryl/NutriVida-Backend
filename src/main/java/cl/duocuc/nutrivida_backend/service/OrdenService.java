package cl.duocuc.nutrivida_backend.service;

import cl.duocuc.nutrivida_backend.dto.DetalleOrdenResponse;
import cl.duocuc.nutrivida_backend.dto.OrdenCreateRequest;
import cl.duocuc.nutrivida_backend.dto.OrdenResponse;
import cl.duocuc.nutrivida_backend.exception.OperacionNoPermitidaException;
import cl.duocuc.nutrivida_backend.exception.RecursoNoEncontradoException;
import cl.duocuc.nutrivida_backend.exception.StockInsuficienteException;
import cl.duocuc.nutrivida_backend.model.DetalleOrden;
import cl.duocuc.nutrivida_backend.model.EstadoOrden;
import cl.duocuc.nutrivida_backend.model.Orden;
import cl.duocuc.nutrivida_backend.model.Producto;
import cl.duocuc.nutrivida_backend.model.Usuario;
import cl.duocuc.nutrivida_backend.repository.OrdenRepository;
import cl.duocuc.nutrivida_backend.repository.ProductoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Crea y consulta reservas, y devuelve los cupos cuando se cancela una orden. */
@Service
@RequiredArgsConstructor
public class OrdenService {

    private final OrdenRepository ordenRepository;
    private final ProductoRepository productoRepository;

    /** Comprueba los cupos y guarda la reserva con los precios del momento. */
    @Transactional
    public OrdenResponse crear(Usuario usuario, OrdenCreateRequest datos) {
        Map<String, Integer> cantidades = new LinkedHashMap<>();
        for (var item : datos.items()) {
            // Si el mismo servicio aparece más de una vez, suma sus cantidades.
            cantidades.merge(item.codigo().trim().toUpperCase(), item.cantidad(), Integer::sum);
        }

        // Primero descuenta todos los cupos. Si uno falla, la transacción revierte los anteriores.
        Map<Producto, Integer> aReservar = new LinkedHashMap<>();
        for (var entrada : cantidades.entrySet()) {
            Producto producto = productoRepository.findByCodigo(entrada.getKey())
                    .filter(Producto::isActivo)
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Producto no encontrado: " + entrada.getKey()));

            if (productoRepository.descontarCupos(producto.getId(), entrada.getValue()) == 0) {
                throw new StockInsuficienteException(
                        "No hay cupos suficientes para \"" + producto.getNombre()
                                + "\". Ajusta tu reserva para continuar");
            }
            aReservar.put(producto, entrada.getValue());
        }

        // Guarda el precio actual en el detalle para conservarlo aunque luego cambie el catálogo.
        Orden orden = new Orden();
        orden.setUsuario(usuario);
        orden.setFecha(LocalDateTime.now());
        orden.setEstado(EstadoOrden.RESERVADA);
        orden.setTotal(BigDecimal.ZERO);

        BigDecimal total = BigDecimal.ZERO;
        for (var entrada : aReservar.entrySet()) {
            Producto producto = entrada.getKey();
            int cantidad = entrada.getValue();

            DetalleOrden detalle = new DetalleOrden();
            detalle.setOrden(orden);
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(producto.getPrecio());
            orden.getDetalles().add(detalle);

            total = total.add(producto.getPrecio().multiply(BigDecimal.valueOf(cantidad)));
        }
        orden.setTotal(total);

        Orden guardada = ordenRepository.save(orden);
        return respuesta(ordenRepository.buscarConDetalles(guardada.getId()).orElse(guardada));
    }

    /** Devuelve las reservas de este cliente, empezando por la más reciente. */
    @Transactional(readOnly = true)
    public List<OrdenResponse> listarDelUsuario(Usuario usuario) {
        return ordenRepository.findByUsuarioIdOrderByFechaDesc(usuario.getId()).stream()
                .map(OrdenService::respuesta)
                .toList();
    }

    /** Devuelve todas las reservas, empezando por la más reciente. */
    @Transactional(readOnly = true)
    public List<OrdenResponse> listarTodas() {
        return ordenRepository.findAllByOrderByFechaDesc().stream()
                .map(OrdenService::respuesta)
                .toList();
    }

    /** Comprueba que el cliente solo pueda consultar sus propias reservas. */
    @Transactional(readOnly = true)
    public OrdenResponse obtener(Usuario usuario, Long id) {
        Orden orden = ordenRepository.buscarConDetalles(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));
        boolean esStaff = "administrador".equalsIgnoreCase(usuario.getRol().getNombre())
                || "recepcionista".equalsIgnoreCase(usuario.getRol().getNombre());
        if (!esStaff && !orden.getUsuario().getId().equals(usuario.getId())) {
            throw new OperacionNoPermitidaException("No tienes permiso para ver esta orden");
        }
        return respuesta(orden);
    }

    /** Cancela una reserva activa y devuelve al catálogo los cupos que ocupaba. */
    @Transactional
    public OrdenResponse cancelar(Long id) {
        Orden orden = ordenRepository.buscarConDetalles(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));

        if (orden.getEstado() != EstadoOrden.RESERVADA) {
            throw new OperacionNoPermitidaException("Solo se pueden cancelar órdenes reservadas");
        }

        for (DetalleOrden detalle : orden.getDetalles()) {
            productoRepository.devolverCupos(detalle.getProducto().getId(), detalle.getCantidad());
        }
        orden.setEstado(EstadoOrden.CANCELADA);
        return respuesta(ordenRepository.save(orden));
    }

    /** Prepara los datos de la reserva y sus líneas para la respuesta de la API. */
    public static OrdenResponse respuesta(Orden orden) {
        List<DetalleOrdenResponse> detalles = orden.getDetalles().stream()
                .map(d -> new DetalleOrdenResponse(
                        d.getId(),
                        d.getProducto().getCodigo(),
                        d.getProducto().getNombre(),
                        d.getCantidad(),
                        d.getPrecioUnitario(),
                        d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad()))))
                .toList();

        String cliente = orden.getUsuario() == null ? null
                : orden.getUsuario().getNombre() + " " + orden.getUsuario().getApellido();

        return new OrdenResponse(
                orden.getId(),
                orden.getFecha(),
                orden.getTotal(),
                orden.getEstado().name(),
                cliente,
                detalles);
    }
}

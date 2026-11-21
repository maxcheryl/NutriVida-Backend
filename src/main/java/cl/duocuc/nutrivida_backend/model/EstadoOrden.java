package cl.duocuc.nutrivida_backend.model;

/**
 * Estados posibles de una orden (compra/reserva).
 * Nace RESERVADA al confirmar el carrito; el administrador puede cancelarla.
 */
public enum EstadoOrden {
    RESERVADA,
    CANCELADA
}

package cl.duocuc.nutrivida_backend.exception;

/** Cupos/stock insuficiente para la cantidad solicitada. */
public class StockInsuficienteException extends RuntimeException {

    public StockInsuficienteException(String mensaje) {
        super(mensaje);
    }
}

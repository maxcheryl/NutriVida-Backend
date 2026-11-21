package cl.duocuc.nutrivida_backend.exception;

/** Operación no permitida por reglas de negocio o por el rol del usuario. */
public class OperacionNoPermitidaException extends RuntimeException {

    public OperacionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}

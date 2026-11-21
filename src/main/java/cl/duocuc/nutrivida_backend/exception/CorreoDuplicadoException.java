package cl.duocuc.nutrivida_backend.exception;

/** Correo electrónico ya registrado por otro usuario. */
public class CorreoDuplicadoException extends RuntimeException {

    public CorreoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}

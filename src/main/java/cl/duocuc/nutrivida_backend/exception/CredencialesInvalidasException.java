package cl.duocuc.nutrivida_backend.exception;

/** Credenciales de acceso incorrectas o cuenta bloqueada. */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}

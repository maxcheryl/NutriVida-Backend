package cl.duocuc.nutrivida_backend.exception;

/** Conflicto de integridad (categoría en uso, datos duplicados). */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}

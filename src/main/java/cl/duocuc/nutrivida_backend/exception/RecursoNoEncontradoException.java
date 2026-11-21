package cl.duocuc.nutrivida_backend.exception;

/** Recurso solicitado inexistente (usuarios, productos, categorías, órdenes). */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}

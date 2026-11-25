package cl.duocuc.nutrivida_backend.exception;

import cl.duocuc.nutrivida_backend.dto.ErrorResponse;
import java.util.HashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Convierte los errores de la aplicación en respuestas JSON uniformes. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException e) {
        return responder(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponse> credenciales(CredencialesInvalidasException e) {
        return responder(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(OperacionNoPermitidaException.class)
    public ResponseEntity<ErrorResponse> noPermitida(OperacionNoPermitidaException e) {
        return responder(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler({CorreoDuplicadoException.class, ConflictoException.class})
    public ResponseEntity<ErrorResponse> conflicto(RuntimeException e) {
        return responder(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<ErrorResponse> stock(StockInsuficienteException e) {
        return responder(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> accesoDenegado(AccessDeniedException e) {
        return responder(HttpStatus.FORBIDDEN, "No tienes permisos para realizar esta operación.");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> noAutenticado(AuthenticationException e) {
        return responder(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión para acceder a este recurso.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException e) {
        // Conserva cada error junto al nombre del campo para que el frontend lo pueda mostrar.
        Map<String, String> campos = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            campos.put(error.getField(), error.getDefaultMessage());
        }
        String mensaje = campos.size() == 1
                ? campos.values().iterator().next()
                : "Los datos enviados no son válidos.";
        return ResponseEntity.badRequest().body(new ErrorResponse(mensaje, campos));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integridad(DataIntegrityViolationException e) {
        return responder(HttpStatus.CONFLICT,
                "La operación viola una restricción de integridad de datos.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> inesperado(Exception e) {
        return responder(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor. Intenta nuevamente.");
    }

    private ResponseEntity<ErrorResponse> responder(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(new ErrorResponse(mensaje));
    }
}

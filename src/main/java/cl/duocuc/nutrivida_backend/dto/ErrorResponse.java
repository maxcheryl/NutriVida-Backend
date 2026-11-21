package cl.duocuc.nutrivida_backend.dto;

import java.util.Map;

/** Respuesta uniforme de error de la API. */
public record ErrorResponse(String mensaje, Map<String, String> campos) {

    public ErrorResponse(String mensaje) {
        this(mensaje, null);
    }
}

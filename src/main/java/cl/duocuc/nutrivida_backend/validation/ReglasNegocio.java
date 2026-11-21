package cl.duocuc.nutrivida_backend.validation;

import cl.duocuc.nutrivida_backend.exception.OperacionNoPermitidaException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Reglas de negocio reutilizables. Reproduce las validaciones del proyecto
 * original: dominio @duocuc.cl, política de contraseña, edad y precios.
 */
public final class ReglasNegocio {

    public static final String DOMINIO_CORREO = "@duocuc.cl";
    public static final int EDAD_MINIMA = 14;
    public static final int EDAD_MAXIMA = 120;
    public static final int PRECIO_MINIMO = 1000;
    public static final int PRECIO_MAXIMO = 10_000_000;
    public static final int STOCK_MAXIMO = 1000;
    public static final int MAX_INTENTOS_FALLIDOS = 3;

    private ReglasNegocio() {
    }

    /** Contraseña: mínimo 8 caracteres con mayúscula, minúscula y dígito. */
    public static void validarContrasena(String contrasena) {
        boolean largo = contrasena != null && contrasena.length() >= 8;
        boolean mayuscula = contrasena != null && contrasena.matches(".*[A-Z].*");
        boolean minuscula = contrasena != null && contrasena.matches(".*[a-z].*");
        boolean digito = contrasena != null && contrasena.matches(".*\\d.*");
        if (!largo || !mayuscula || !minuscula || !digito) {
            throw new OperacionNoPermitidaException(
                    "La contraseña debe contener 8 carácteres, una mayúscula, una minúscula y un número");
        }
    }

    /** Correo institucional: debe terminar en @duocuc.cl. */
    public static void validarDominioCorreo(String correo) {
        if (correo == null || !correo.toLowerCase().endsWith(DOMINIO_CORREO)) {
            throw new OperacionNoPermitidaException("El dominio del correo debe ser " + DOMINIO_CORREO);
        }
    }

    /** Edad del usuario: entre 14 y 120 años (`contexto` = "El registro" o "El perfil"). */
    public static void validarEdad(LocalDate fechaNacimiento, String contexto) {
        if (fechaNacimiento == null || fechaNacimiento.isAfter(LocalDate.now())) {
            throw new OperacionNoPermitidaException("La fecha de nacimiento no puede ser posterior al día de hoy");
        }
        long edad = ChronoUnit.YEARS.between(fechaNacimiento, LocalDate.now());
        if (edad < EDAD_MINIMA) {
            throw new OperacionNoPermitidaException(contexto + " es solo para personas mayores a 14 años");
        }
        if (edad > EDAD_MAXIMA) {
            throw new OperacionNoPermitidaException("La edad máxima permitida es de " + EDAD_MAXIMA + " años");
        }
    }

    /** Precio de producto: entero entre 1.000 y 10.000.000. */
    public static void validarPrecio(java.math.BigDecimal precio) {
        if (precio == null
                || precio.compareTo(java.math.BigDecimal.valueOf(PRECIO_MINIMO)) < 0
                || precio.compareTo(java.math.BigDecimal.valueOf(PRECIO_MAXIMO)) > 0
                || precio.stripTrailingZeros().scale() > 0) {
            throw new OperacionNoPermitidaException(
                    "El precio debe ser un número entero entre 1.000 y 10.000.000.");
        }
    }

    /** Cupos disponibles: entero entre 0 y 1000. */
    public static void validarCupos(int cupos) {
        if (cupos < 0 || cupos > STOCK_MAXIMO) {
            throw new OperacionNoPermitidaException(
                    "Los cupos deben ser un número entero entre 0 y " + STOCK_MAXIMO + ".");
        }
    }
}

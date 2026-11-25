package cl.duocuc.nutrivida_backend.controller;

import cl.duocuc.nutrivida_backend.dto.OrdenCreateRequest;
import cl.duocuc.nutrivida_backend.dto.OrdenResponse;
import cl.duocuc.nutrivida_backend.dto.ErrorResponse;
import cl.duocuc.nutrivida_backend.model.Usuario;
import cl.duocuc.nutrivida_backend.service.OrdenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Expone las operaciones para crear, consultar y cancelar reservas. */
@RestController
@RequestMapping("/api/v1/ordenes")
@RequiredArgsConstructor
@Tag(name = "Órdenes", description = "Historial de reservas del cliente y gestión administrativa")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Solicitud inválida.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Se requiere autenticación.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Rol sin permisos para esta operación.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Conflicto de negocio, por ejemplo stock insuficiente.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
})
public class OrdenController {

    private final OrdenService ordenService;

    @Operation(summary = "Confirmar carrito", description = "Descuenta cupos y crea la orden en estado RESERVADA.")
    @ApiResponse(responseCode = "201", description = "Orden creada.")
    @PostMapping
    public ResponseEntity<OrdenResponse> crear(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody OrdenCreateRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ordenService.crear(usuario, datos));
    }

    @Operation(summary = "Mis órdenes", description = "Historial del usuario autenticado, más reciente primero.")
    @ApiResponse(responseCode = "200", description = "Listado de órdenes del usuario.")
    @GetMapping("/mias")
    public ResponseEntity<List<OrdenResponse>> mias(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(ordenService.listarDelUsuario(usuario));
    }

    @Operation(summary = "Listar todas las órdenes", description = "Administrador o recepcionista.")
    @ApiResponse(responseCode = "200", description = "Listado de órdenes.")
    @GetMapping
    public ResponseEntity<List<OrdenResponse>> listar() {
        return ResponseEntity.ok(ordenService.listarTodas());
    }

    @Operation(summary = "Detalle de orden", description = "El cliente solo puede ver las suyas.")
    @ApiResponse(responseCode = "200", description = "Orden encontrada.")
    @GetMapping("/{id}")
    public ResponseEntity<OrdenResponse> obtener(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable Long id) {
        return ResponseEntity.ok(ordenService.obtener(usuario, id));
    }

    @Operation(summary = "Cancelar orden", description = "Administrador. Devuelve los cupos y marca CANCELADA.")
    @ApiResponse(responseCode = "200", description = "Orden cancelada.")
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<OrdenResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(ordenService.cancelar(id));
    }
}

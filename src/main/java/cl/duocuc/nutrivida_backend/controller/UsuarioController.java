package cl.duocuc.nutrivida_backend.controller;

import cl.duocuc.nutrivida_backend.dto.UsuarioRequest;
import cl.duocuc.nutrivida_backend.dto.ErrorResponse;
import cl.duocuc.nutrivida_backend.dto.UsuarioResponse;
import cl.duocuc.nutrivida_backend.service.UsuarioService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Expone las operaciones para consultar y administrar cuentas de usuario. */
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Alta, edición y baja lógica de cuentas")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Solicitud inválida.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Se requiere autenticación.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Rol sin permisos para esta operación.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Conflicto con los datos del usuario.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
})
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Operation(summary = "Listar usuarios", description = "Búsqueda opcional por nombre, apellido o correo.")
    @ApiResponse(responseCode = "200", description = "Listado de usuarios.")
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar(
            @RequestParam(required = false) String busqueda) {
        return ResponseEntity.ok(usuarioService.listar(busqueda));
    }

    @Operation(summary = "Obtener usuario por id")
    @ApiResponse(responseCode = "200", description = "Usuario encontrado.")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtener(id));
    }

    @Operation(summary = "Crear usuario", description = "Administrador o recepcionista (clientes desde Gestión).")
    @ApiResponse(responseCode = "201", description = "Usuario creado.")
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(datos));
    }

    @Operation(summary = "Editar usuario", description = "La contraseña solo se cambia si se envía.")
    @ApiResponse(responseCode = "200", description = "Usuario actualizado.")
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> editar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequest datos) {
        return ResponseEntity.ok(usuarioService.editar(id, datos));
    }

    @Operation(summary = "Desactivar usuario", description = "Baja lógica: conserva usuario y órdenes.")
    @ApiResponse(responseCode = "200", description = "Usuario desactivado.")
    @DeleteMapping("/{id}")
    public ResponseEntity<UsuarioResponse> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.desactivar(id));
    }

    @Operation(summary = "Reactivar usuario")
    @ApiResponse(responseCode = "200", description = "Usuario reactivado.")
    @PutMapping("/{id}/activar")
    public ResponseEntity<UsuarioResponse> activar(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.activar(id));
    }
}

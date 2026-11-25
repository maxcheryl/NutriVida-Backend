package cl.duocuc.nutrivida_backend.controller;

import cl.duocuc.nutrivida_backend.dto.PerfilRequest;
import cl.duocuc.nutrivida_backend.dto.ErrorResponse;
import cl.duocuc.nutrivida_backend.dto.UsuarioResponse;
import cl.duocuc.nutrivida_backend.model.Usuario;
import cl.duocuc.nutrivida_backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Permite a la persona autenticada consultar y actualizar su perfil. */
@RestController
@RequestMapping("/api/v1/perfil")
@RequiredArgsConstructor
@Tag(name = "Perfil", description = "Datos del usuario en sesión")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Solicitud inválida.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Se requiere autenticación.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Operación no permitida.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
})
public class PerfilController {

    private final UsuarioService usuarioService;

    @Operation(summary = "Ver mi perfil")
    @ApiResponse(responseCode = "200", description = "Datos del perfil.")
    @GetMapping
    public ResponseEntity<UsuarioResponse> obtener(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(usuarioService.obtener(usuario.getId()));
    }

    @Operation(summary = "Actualizar mi perfil",
            description = "El cambio de contraseña aplica solo si se envían contraseña actual y nueva.")
    @ApiResponse(responseCode = "200", description = "Perfil actualizado.")
    @PutMapping
    public ResponseEntity<UsuarioResponse> actualizar(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody PerfilRequest datos) {
        return ResponseEntity.ok(usuarioService.actualizarPerfil(usuario, datos));
    }
}

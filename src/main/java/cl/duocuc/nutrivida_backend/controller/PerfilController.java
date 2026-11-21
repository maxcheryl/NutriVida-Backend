package cl.duocuc.nutrivida_backend.controller;

import cl.duocuc.nutrivida_backend.dto.PerfilRequest;
import cl.duocuc.nutrivida_backend.dto.UsuarioResponse;
import cl.duocuc.nutrivida_backend.model.Usuario;
import cl.duocuc.nutrivida_backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
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

/** Perfil del usuario autenticado: datos personales y cambio de contraseña. */
@RestController
@RequestMapping("/api/v1/perfil")
@RequiredArgsConstructor
@Tag(name = "Perfil", description = "Datos del usuario en sesión")
public class PerfilController {

    private final UsuarioService usuarioService;

    @Operation(summary = "Ver mi perfil")
    @GetMapping
    public ResponseEntity<UsuarioResponse> obtener(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(usuarioService.obtener(usuario.getId()));
    }

    @Operation(summary = "Actualizar mi perfil",
            description = "El cambio de contraseña aplica solo si se envían contraseña actual y nueva.")
    @PutMapping
    public ResponseEntity<UsuarioResponse> actualizar(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody PerfilRequest datos) {
        return ResponseEntity.ok(usuarioService.actualizarPerfil(usuario, datos));
    }
}

package cl.duocuc.nutrivida_backend.controller;

import cl.duocuc.nutrivida_backend.dto.LoginRequest;
import cl.duocuc.nutrivida_backend.dto.RegistroRequest;
import cl.duocuc.nutrivida_backend.dto.UsuarioResponse;
import cl.duocuc.nutrivida_backend.model.Usuario;
import cl.duocuc.nutrivida_backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Autenticación: login, registro público, refresh y cierre de sesión. */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Login, registro y renovación de sesión (cookies httpOnly)")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Iniciar sesión", description = "Emite access (30 min) y refresh (7 días) en cookies httpOnly.")
    @PostMapping("/login")
    public ResponseEntity<UsuarioResponse> login(
            @Valid @RequestBody LoginRequest datos,
            HttpServletResponse respuesta) {
        return ResponseEntity.ok(authService.login(datos, respuesta));
    }

    @Operation(summary = "Registrarse", description = "Alta pública de cliente con dominio @duocuc.cl; inicia sesión automáticamente.")
    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registro(
            @Valid @RequestBody RegistroRequest datos,
            HttpServletResponse respuesta) {
        return ResponseEntity.ok(authService.registro(datos, respuesta));
    }

    @Operation(summary = "Renovar sesión", description = "Intercambia la cookie de refresh por un nuevo par de cookies.")
    @PostMapping("/refresh")
    public ResponseEntity<UsuarioResponse> refresh(
            HttpServletRequest solicitud,
            HttpServletResponse respuesta) {
        return ResponseEntity.ok(authService.refresh(solicitud, respuesta));
    }

    @Operation(summary = "Cerrar sesión", description = "Borra las cookies de sesión.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse respuesta) {
        authService.logout(respuesta);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Usuario en sesión", description = "Devuelve los datos públicos del usuario autenticado.")
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> me(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(authService.obtenerActual(usuario));
    }
}

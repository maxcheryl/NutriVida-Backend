package cl.duocuc.nutrivida_backend.controller;

import cl.duocuc.nutrivida_backend.dto.CategoriaRequest;
import cl.duocuc.nutrivida_backend.dto.CategoriaResponse;
import cl.duocuc.nutrivida_backend.dto.ErrorResponse;
import cl.duocuc.nutrivida_backend.service.CategoriaService;
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
import org.springframework.web.bind.annotation.RestController;

/** Categorías del catálogo: lectura pública y mantenimiento administrativo. */
@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorías", description = "Grupos del catálogo (Nutrición, Planes, Evaluación...)")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Solicitud inválida.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Se requiere autenticación para mantenimiento.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Rol sin permisos para mantenimiento.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Conflicto al crear o eliminar la categoría.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
})
public class CategoriaController {

    private final CategoriaService categoriaService;

    @Operation(summary = "Listar categorías", description = "Incluye el conteo de productos asociados.")
    @ApiResponse(responseCode = "200", description = "Listado de categorías.")
    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> listar() {
        return ResponseEntity.ok(categoriaService.listar());
    }

    @Operation(summary = "Obtener categoría por id")
    @ApiResponse(responseCode = "200", description = "Categoría encontrada.")
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.obtener(id));
    }

    @Operation(summary = "Crear categoría")
    @ApiResponse(responseCode = "201", description = "Categoría creada.")
    @PostMapping
    public ResponseEntity<CategoriaResponse> crear(@Valid @RequestBody CategoriaRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.crear(datos));
    }

    @Operation(summary = "Editar categoría")
    @ApiResponse(responseCode = "200", description = "Categoría actualizada.")
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponse> editar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequest datos) {
        return ResponseEntity.ok(categoriaService.editar(id, datos));
    }

    @Operation(summary = "Eliminar categoría",
            description = "Borrado físico; falla con 409 si tiene productos asociados.")
    @ApiResponse(responseCode = "204", description = "Categoría eliminada; respuesta sin cuerpo.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

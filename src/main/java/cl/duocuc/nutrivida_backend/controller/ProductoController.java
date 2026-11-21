package cl.duocuc.nutrivida_backend.controller;

import cl.duocuc.nutrivida_backend.dto.ProductoRequest;
import cl.duocuc.nutrivida_backend.dto.ProductoResponse;
import cl.duocuc.nutrivida_backend.dto.StockRequest;
import cl.duocuc.nutrivida_backend.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
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

/** Catálogo de productos: lectura pública y mantenimiento administrativo. */
@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Catálogo de servicios/planes y stock de cupos")
public class ProductoController {

    private final ProductoService productoService;

    @Operation(summary = "Catálogo de productos", description = "Solo productos activos; filtro opcional por categoría.")
    @GetMapping
    public ResponseEntity<List<ProductoResponse>> listar(
            @RequestParam(required = false) String categoria) {
        return ResponseEntity.ok(productoService.listar(categoria));
    }

    @Operation(summary = "Obtener producto por id o código", description = "Acepta el ID numérico o el código alfanumérico (ej. CN001).")
    @GetMapping("/{identificador}")
    public ResponseEntity<ProductoResponse> obtener(@PathVariable String identificador) {
        if (identificador.matches("\\d+")) {
            return ResponseEntity.ok(productoService.obtener(Long.parseLong(identificador)));
        }
        return ResponseEntity.ok(productoService.obtenerPorCodigo(identificador));
    }

    @Operation(summary = "Crear producto")
    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(datos));
    }

    @Operation(summary = "Editar producto", description = "Acepta el ID numérico o el código del producto.")
    @PutMapping("/{identificador}")
    public ResponseEntity<ProductoResponse> editar(
            @PathVariable String identificador,
            @Valid @RequestBody ProductoRequest datos) {
        return ResponseEntity.ok(productoService.editar(identificador, datos));
    }

    @Operation(summary = "Actualizar cupos libres",
            description = "Administrador o recepcionista. Acepta ID o código. El valor es el cupo disponible actual (0 a 1000).")
    @PutMapping("/{identificador}/stock")
    public ResponseEntity<ProductoResponse> actualizarStock(
            @PathVariable String identificador,
            @Valid @RequestBody StockRequest datos) {
        return ResponseEntity.ok(productoService.actualizarStock(identificador, datos));
    }

    @Operation(summary = "Desactivar producto", description = "Baja lógica: sale del catálogo, conserva historial.")
    @DeleteMapping("/{identificador}")
    public ResponseEntity<ProductoResponse> eliminar(@PathVariable String identificador) {
        return ResponseEntity.ok(productoService.eliminar(identificador));
    }
}

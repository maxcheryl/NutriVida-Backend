package cl.duocuc.nutrivida_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Guarda los datos de un servicio y sus cupos disponibles para reservar. */
@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código que identifica el servicio, por ejemplo CN001. */
    @Column(nullable = false, length = 10, unique = true)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nombre;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(length = 20)
    private String duracion;

    @Column(length = 40)
    private String modalidad;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(name = "cupos", nullable = false)
    private int cuposIniciales;

    @Column(length = 1000)
    private String descripcion;

    /** Dirección web de la imagen que se muestra en el catálogo. */
    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    /** Permite ocultar el servicio sin eliminar sus reservas anteriores. */
    @Column(nullable = false)
    private boolean activo = true;
}

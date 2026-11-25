package cl.duocuc.nutrivida_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Guarda los datos de una persona, su rol y el estado de su cuenta. */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String apellido;

    @Column(nullable = false, length = 100, unique = true)
    private String correo;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    /** Contraseña protegida; nunca se incluye en la respuesta de la API. */
    @Column(nullable = false, length = 100)
    private String contrasena;

    @Column(length = 20)
    private String genero;

    @Column(length = 50)
    private String region;

    @Column(length = 150)
    private String direccion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    /** Indica si la persona puede iniciar sesión. */
    @Column(nullable = false)
    private boolean activo = true;

    /** Cuenta los intentos fallidos consecutivos de inicio de sesión. */
    @Column(name = "intentos_fallidos", nullable = false)
    private int intentosFallidos = 0;
}

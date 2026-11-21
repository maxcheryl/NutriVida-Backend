-- ============================================================
-- V1: esquema inicial de Clínica NutriVida
-- Roles, usuarios, categorías, productos, órdenes y detalles.
-- ============================================================

CREATE TABLE roles (
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    nombre  VARCHAR(30)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_roles_nombre (nombre)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE usuarios (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    nombre            VARCHAR(50)   NOT NULL,
    apellido          VARCHAR(50)   NOT NULL,
    correo            VARCHAR(100)  NOT NULL,
    fecha_nacimiento  DATE          NOT NULL,
    contrasena        VARCHAR(100)  NOT NULL,
    genero            VARCHAR(20)   NULL,
    region            VARCHAR(50)   NULL,
    direccion         VARCHAR(150)  NULL,
    avatar            VARCHAR(255)  NULL,
    rol_id            BIGINT        NOT NULL,
    activo            TINYINT(1)    NOT NULL DEFAULT 1,
    intentos_fallidos INT           NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uq_usuarios_correo (correo),
    CONSTRAINT fk_usuarios_rol FOREIGN KEY (rol_id) REFERENCES roles (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE categorias (
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    nombre  VARCHAR(60)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_categorias_nombre (nombre)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE productos (
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    codigo           VARCHAR(10)    NOT NULL,
    nombre           VARCHAR(120)   NOT NULL,
    categoria_id     BIGINT         NOT NULL,
    duracion         VARCHAR(20)    NULL,
    modalidad        VARCHAR(40)    NULL,
    precio           DECIMAL(12, 2) NOT NULL,
    cupos_iniciales  INT            NOT NULL,
    descripcion      VARCHAR(1000)  NULL,
    imagen           VARCHAR(255)   NULL,
    imagen_url       VARCHAR(500)   NULL,
    activo           TINYINT(1)     NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uq_productos_codigo (codigo),
    CONSTRAINT fk_productos_categoria FOREIGN KEY (categoria_id) REFERENCES categorias (id),
    CONSTRAINT chk_productos_precio CHECK (precio >= 1000),
    CONSTRAINT chk_productos_cupos CHECK (cupos_iniciales >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE ordenes (
    id         BIGINT         NOT NULL AUTO_INCREMENT,
    usuario_id BIGINT         NOT NULL,
    fecha      DATETIME       NOT NULL,
    total      DECIMAL(12, 2) NOT NULL,
    estado     VARCHAR(20)    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_ordenes_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    CONSTRAINT chk_ordenes_estado CHECK (estado IN ('RESERVADA', 'CANCELADA')),
    CONSTRAINT chk_ordenes_total CHECK (total >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE detalles_orden (
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    orden_id        BIGINT         NOT NULL,
    producto_id     BIGINT         NOT NULL,
    cantidad        INT            NOT NULL,
    precio_unitario DECIMAL(12, 2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_detalles_orden FOREIGN KEY (orden_id) REFERENCES ordenes (id) ON DELETE CASCADE,
    CONSTRAINT fk_detalles_producto FOREIGN KEY (producto_id) REFERENCES productos (id),
    CONSTRAINT chk_detalles_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_detalles_precio CHECK (precio_unitario >= 1000)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Índices adicionales (los de las FK los crea InnoDB automáticamente;
-- los UNIQUE ya generan índice implícito en correo, código y nombre)
CREATE INDEX idx_ordenes_fecha ON ordenes (fecha);

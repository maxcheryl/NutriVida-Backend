# Clínica NutriVida — Backend

Proyecto backend de la **Evaluación 3 (IE3)** de la asignatura **DSY1104**. Expone la API REST que utiliza el frontend de NutriVida para administrar servicios nutricionales, usuarios y reservas.

## Sobre el proyecto

El backend implementa la lógica de negocio y persistencia de la aplicación NutriVida. Está desarrollado con Spring Boot y conecta con MySQL mediante JPA. Flyway administra el esquema y carga los datos iniciales. La API sigue una organización por recursos bajo `/api/v1`.

## Funcionalidades

- Catálogo REST de productos/servicios y categorías.
- CRUD de usuarios, servicios y categorías, con validación de datos y permisos.
- Creación, consulta y cancelación de órdenes/reservas, con control de cupos.
- Registro e inicio de sesión con JWT en cookies `HttpOnly`, renovación y cierre de sesión.
- Autorización basada en roles: administrador, recepcionista y cliente.
- Conteo de intentos fallidos de inicio de sesión y bloqueo de cuenta según las reglas del servicio.
- Validación y respuestas JSON para errores de la API.
- Documentación OpenAPI disponible en Swagger UI.

### Roles y acceso

- **Administrador:** administra usuarios, productos, categorías y órdenes; puede cancelar órdenes.
- **Recepcionista:** puede crear usuarios, actualizar stock/cupos y consultar el listado general de órdenes.
- **Cliente:** puede consultar el catálogo, gestionar su perfil, crear órdenes y consultar sus propias órdenes.

El login, registro, renovación y cierre de sesión no requieren autenticación. También son públicas las consultas `GET` de productos y categorías. Las demás operaciones están protegidas según el endpoint y el rol.

## Endpoints de la API

Prefijo local: `http://localhost:8080/api/v1`.

| Recurso | Operaciones |
| --- | --- |
| Autenticación: `/auth` | `POST /login`, `/registro`, `/refresh`, `/logout`; `GET /me` |
| Usuarios: `/usuarios` | `GET` y `GET /{id}`; `POST`; `PUT /{id}` y `PUT /{id}/activar`; `DELETE /{id}` |
| Productos/servicios: `/productos` | `GET` y `GET /{identificador}`; `POST`; `PUT /{identificador}` y `PUT /{identificador}/stock`; `DELETE /{identificador}` |
| Categorías: `/categorias` | `GET` y `GET /{id}`; `POST`; `PUT /{id}`; `DELETE /{id}` |
| Órdenes: `/ordenes` | `POST`; `GET /mias`, `GET`, `GET /{id}`; `PUT /{id}/cancelar` |
| Perfil: `/perfil` | `GET`; `PUT` |

Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)  
Documento OpenAPI: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## Credenciales de demostración

Flyway inserta estas cuentas al iniciar por primera vez la base de datos:

| Rol | Correo |
| --- | --- |
| Administrador | `admin@duocuc.cl` |
| Recepcionista | `recepcion@duocuc.cl` |
| Cliente | `cliente@duocuc.cl` |

**Contraseña común:** `Nutrivida2026`

Estas cuentas son para demostración local. No las utilices en producción ni en un entorno público; crea credenciales propias y seguras.

## Tecnologías utilizadas

- Java 21 y Spring Boot 4.1.2.
- Spring Web MVC para la API REST.
- Spring Security y JWT para autenticación y autorización.
- Spring Data JPA e Hibernate para persistencia.
- MySQL como base de datos y Flyway para migraciones.
- Springdoc OpenAPI 3.2.0 para Swagger UI.
- Maven Wrapper para compilar, probar y ejecutar.
- El frontend asociado usa React 19, TypeScript y Vite.

## Estructura del proyecto

```text
nutrivida-backend/
├── src/main/java/cl/duocuc/nutrivida_backend/
│   ├── config/       # Configuración de aplicación y OpenAPI
│   ├── controller/   # Controladores y rutas REST
│   ├── dto/          # Objetos de solicitud y respuesta
│   ├── exception/    # Excepciones y manejadores
│   ├── model/        # Entidades y modelos
│   ├── repository/   # Persistencia JPA
│   ├── security/     # JWT, filtros y autorización
│   ├── service/      # Lógica de negocio
│   └── validation/   # Validaciones y reglas de negocio
├── src/main/resources/
│   ├── db/migration/ # Migraciones Flyway y datos semilla
│   └── application.properties
├── mvnw.cmd
└── pom.xml
```

## Requisitos

- JDK 21.
- MySQL en ejecución (XAMPP u otra instalación compatible).
- Una base de datos MySQL llamada `nutrivida`.
- Para ejecutar la aplicación completa, el frontend necesita Node.js/npm y el backend debe permitir el origen `http://localhost:5173`.

## Pasos para ejecutar

### 1. Preparar MySQL

Inicia MySQL y crea una base de datos vacía llamada `nutrivida`. La configuración local está en `src/main/resources/application.properties`; por defecto apunta a `localhost:3306`, usuario `root` y contraseña vacía. Ajusta la URL, usuario y contraseña si tu instalación usa otros valores.

Flyway crea las tablas y carga roles, cuentas, categorías y servicios cuando se inicia la aplicación.

### 2. Ejecutar el backend

Desde la raíz de este repositorio, en PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

La API estará disponible en `http://localhost:8080`.

Para ejecutar las pruebas:

```powershell
.\mvnw.cmd test
```

### 3. Ejecutar el frontend asociado

Clona e inicia el repositorio Frontend con Node.js/npm. Su servidor de desarrollo utiliza normalmente `http://localhost:5173` y consume esta API en `http://localhost:8080/api/v1`.

## Notas de seguridad

- Los valores actuales de conexión MySQL y la clave JWT de `application.properties` son de desarrollo local; reemplázalos por configuración segura antes de desplegar.
- No reutilices la contraseña de demostración en producción ni publiques credenciales reales.
- La autenticación es stateless y utiliza cookies JWT `HttpOnly`; los errores de seguridad se responden en JSON.

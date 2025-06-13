# Backend del Sistema de Peritaje Inmobiliario

Este documento detalla la arquitectura, tecnologías y funcionalidades del componente backend del sistema de peritaje inmobiliario.

## 1. Visión General

El backend es una aplicación **Spring Boot** desarrollada en **Java 24** que actúa como la API principal para el sistema. Es responsable de la gestión de usuarios (autenticación y autorización), el almacenamiento y recuperación de los resultados de peritajes, la integración con servicios externos (como Supabase para autenticación) y la generación de documentos PDF a partir de los resultados de los peritajes.

## 2. Tecnologías Clave

*   **Framework**: Spring Boot 3.5.0
*   **Lenguaje**: Java 24
*   **Gestor de Dependencias**: Maven
*   **Base de Datos**: PostgreSQL (integrado con Supabase)
*   **ORM**: Spring Data JPA / Hibernate
*   **Seguridad**: Spring Security, JWT (JSON Web Tokens) con JJWT
*   **Autenticación Externa**: Supabase Auth
*   **Generación de HTML**: Thymeleaf
*   **Generación de PDF**: Flying Saucer PDF (xhtmlrenderer)
*   **Utilidades**: Lombok (para reducción de boilerplate), Jackson (para manejo de JSON)
*   **Web Client**: Spring WebFlux (WebClient) para comunicación reactiva con APIs externas.
*   **Validación**: Jakarta Validation (Hibernate Validator)
*   **Logging**: SLF4J con Logback (configurado para archivos de log)

## 3. Estructura del Proyecto

El proyecto sigue una estructura de paquetes modular, típica de aplicaciones Spring Boot:

```
src/main/java/peritaje/inmobiliario/integrador/
├── controller/             # Controladores REST para la API
│   ├── AppraisalController.java
│   └── AuthController.java
├── domain/                 # Entidades de dominio (modelos de base de datos)
│   └── AppraisalResult.java
├── dto/                    # Objetos de Transferencia de Datos (DTOs)
│   ├── AppraisalResultDTO.java
│   ├── AuthRequest.java
│   ├── AuthResponse.java
│   ├── ErrorResponse.java
│   ├── MigrationRequest.java
│   └── SaveAppraisalRequestDTO.java
├── exception/              # Clases para el manejo global de excepciones
│   └── GlobalExceptionHandler.java
├── repository/             # Interfaces de repositorio para acceso a datos
│   └── AppraisalResultRepository.java
├── security/               # Componentes de seguridad (JWT, Spring Security)
│   ├── config/
│   │   └── SecurityConfig.java
│   ├── CustomUserDetails.java
│   ├── CustomUserDetailsService.java
│   ├── JwtService.java
│   └── JwtTokenFilter.java
├── service/                # Lógica de negocio y servicios
│   ├── AppraisalResultService.java
│   ├── HtmlGenerationService.java
│   ├── HtmlGenerationServiceImpl.java
│   ├── PdfGenerationService.java
│   └── SupabaseAuthService.java
└── IntegradorApplication.java # Clase principal de la aplicación
```

## 4. Componentes Clave

### 4.1. Controladores (`controller/`)

*   **`AppraisalController`**:
    *   `POST /api/appraisal/save-result`: Guarda un nuevo resultado de peritaje. Requiere autenticación.
    *   `GET /api/appraisal/history`: Recupera el historial de peritajes del usuario autenticado.
    *   `POST /api/appraisal/download-pdf`: Genera y descarga un informe de peritaje en formato PDF. Acceso público.
    *   `POST /api/appraisal/migrate-anonymous-data`: Migra datos de peritajes anónimos a un usuario registrado.
*   **`AuthController`**:
    *   `POST /api/public/auth/signup`: Registra un nuevo usuario a través de Supabase.
    *   `POST /api/public/auth/signin`: Inicia sesión de un usuario a través de Supabase y devuelve un token JWT.
    *   `POST /api/public/auth/migrate-session`: Migra una sesión anónima a una sesión de usuario autenticado (a nivel de Supabase).

### 4.2. Servicios (`service/`)

*   **`AppraisalResultService`**:
    *   Gestiona la persistencia de los resultados de peritajes en la base de datos.
    *   Asigna el `userId` del usuario autenticado a los peritajes.
    *   Extrae el `requestId` del JSON de peritaje para indexación.
    *   Maneja la migración de resultados de sesiones anónimas a usuarios registrados.
*   **`HtmlGenerationService` / `HtmlGenerationServiceImpl`**:
    *   Interfaz y su implementación para generar contenido HTML dinámico utilizando plantillas de Thymeleaf.
*   **`PdfGenerationService`**:
    *   Convierte el contenido HTML generado por `HtmlGenerationService` en un archivo PDF utilizando la librería Flying Saucer PDF.
*   **`SupabaseAuthService`**:
    *   Se comunica con la API de autenticación de Supabase para realizar operaciones de registro, inicio de sesión y cierre de sesión.
    *   Utiliza `WebClient` para realizar solicitudes HTTP reactivas.

### 4.3. Seguridad (`security/`)

*   **`SecurityConfig`**: Configura Spring Security para la aplicación.
    *   Deshabilita CSRF.
    *   Define rutas públicas (`/api/public/**`, `/api/appraisal/download-pdf/**`) y rutas protegidas.
    *   Configura la gestión de sesiones como `STATELESS` (sin estado) para soportar JWT.
    *   Integra `JwtTokenFilter` para procesar los tokens JWT en cada solicitud.
*   **`JwtService`**: Proporciona métodos para:
    *   Extraer claims (cargas) de un token JWT.
    *   Validar la firma y la expiración de un token.
    *   Extraer el `userId` y el `username` (email) del token.
*   **`JwtTokenFilter`**: Un filtro de servlet que se ejecuta antes de la autenticación de Spring Security.
    *   Intercepta las solicitudes HTTP.
    *   Extrae el token JWT del encabezado `Authorization`.
    *   Valida el token utilizando `JwtService`.
    *   Si el token es válido, crea un objeto `Authentication` y lo establece en el `SecurityContextHolder`, permitiendo que las solicitudes autenticadas accedan a los recursos protegidos.
*   **`CustomUserDetailsService`**: Implementación de `UserDetailsService` que carga los detalles del usuario a partir de un token JWT, en lugar de una base de datos tradicional. Esto es crucial para la integración con Supabase, donde la autenticación se maneja externamente.

### 4.4. Base de Datos y Persistencia

*   **`AppraisalResult`**: Entidad JPA que mapea la tabla `appraisal_results` en la base de datos PostgreSQL. Almacena el ID de usuario, ID de sesión anónima, ID de solicitud, los datos del peritaje como una cadena JSON y la fecha de creación.
*   **`AppraisalResultRepository`**: Interfaz de Spring Data JPA que proporciona métodos para interactuar con la tabla `appraisal_results`, incluyendo búsquedas por `userId`, `anonymousSessionId`, `id` y `requestId`.

## 5. Configuración (`application.properties`)

El archivo `src/main/resources/application.properties` contiene la configuración esencial para la aplicación:

*   **`spring.application.name`**: Nombre de la aplicación.
*   **`supabase.url`**: URL de la instancia de Supabase.
*   **`supabase.service-key`**: Clave de servicio de Supabase (¡Mantener segura y no exponer en el frontend!).
*   **`supabase.jwt.secret`**: Secreto JWT utilizado para firmar y verificar tokens (debe coincidir con el secreto de Supabase).
*   **Configuración de Base de Datos (PostgreSQL)**:
    *   `spring.datasource.url`: URL de conexión a la base de datos.
    *   `spring.datasource.username`: Nombre de usuario de la base de datos.
    *   `spring.datasource.password`: Contraseña de la base de datos.
    *   `spring.datasource.driver-class-name`: Driver JDBC para PostgreSQL.
*   **Configuración JPA/Hibernate**:
    *   `spring.jpa.hibernate.ddl-auto=update`: Configura Hibernate para actualizar el esquema de la base de datos automáticamente (útil en desarrollo, considerar `none` o `validate` en producción).
    *   `spring.jpa.show-sql=true`: Muestra las sentencias SQL generadas por Hibernate en la consola.
    *   `spring.jpa.properties.hibernate.dialect`: Dialecto de Hibernate para PostgreSQL.
*   **Configuración de Logging**: Define los niveles de log y la ubicación del archivo de log.

## 6. Cómo Ejecutar el Proyecto

1.  **Requisitos Previos**:
    *   Java Development Kit (JDK) 24 o superior.
    *   Maven.
    *   Acceso a una instancia de PostgreSQL (preferiblemente Supabase) con las credenciales configuradas en `application.properties`.
    *   Una clave de servicio de Supabase y un secreto JWT configurados en `application.properties`.

2.  **Clonar el Repositorio**:
    ```bash
    git clone [URL_DEL_REPOSITORIO]
    cd real-state-appraisal-springboot
    ```

3.  **Configurar `application.properties`**:
    Asegúrate de que los valores de `supabase.url`, `supabase.service-key`, `supabase.jwt.secret`, y las credenciales de la base de datos sean correctos para tu entorno.

4.  **Compilar y Ejecutar**:
    Puedes compilar y ejecutar la aplicación usando Maven:
    ```bash
    mvn clean install
    mvn spring-boot:run
    ```
    Alternativamente, puedes ejecutar la clase principal `IntegradorApplication.java` directamente desde tu IDE (IntelliJ IDEA, Eclipse, VS Code con extensiones de Java).

5.  **Acceso a la API**:
    Una vez que la aplicación esté en funcionamiento (por defecto en el puerto 8080), los endpoints de la API estarán disponibles en `http://localhost:8080/api/...`.

## 7. Pruebas

El proyecto incluye pruebas unitarias y de integración en el directorio `src/test/java`. Puedes ejecutar las pruebas con Maven:

```bash
mvn test
```

## 8. Consideraciones de Despliegue

*   **Seguridad de Credenciales**: Asegúrate de que `supabase.service-key` y `supabase.jwt.secret` no se expongan en entornos de producción. Utiliza variables de entorno o un sistema de gestión de secretos.
*   **Base de Datos**: En producción, se recomienda una base de datos PostgreSQL gestionada.
*   **Logging**: Configura los niveles de log apropiados para producción para evitar la sobrecarga de logs.
*   **DDL Auto**: Cambia `spring.jpa.hibernate.ddl-auto` a `none` o `validate` en producción para evitar la modificación automática del esquema de la base de datos.
# Tarea Principal: Asegurar 100% de Cobertura del Backend

**Objetivo:** Alcanzar el 100% de cobertura de código en el backend de la aplicación Spring Boot, garantizando la calidad y robustez del software.

**Diseño Arquitectónico y Enfoque del Proceso:**
El enfoque se centrará en la mejora de la estrategia de pruebas existente, sin alterar la arquitectura fundamental de la aplicación. Se aplicarán rigurosamente los principios de calidad en la escritura de nuevas pruebas y en la refactorización del código si fuera necesario para mejorar su testabilidad.

**Fases del Proceso de Implementación Requerido:**

## 1. Fase de Análisis de Cobertura Inicial
*   [x] **ANL-1.1:** Ejecutar el comando Maven para generar el informe de cobertura de JaCoCo.
    *   **Propósito:** Obtener una línea base del estado actual de la cobertura del código.
    *   **Modo Estimado:** `Code Mode` (para ejecutar el comando).
    *   **Prerrequisitos:** Ninguno.
    *   **Criterios de Calidad:** El informe debe generarse sin errores y ser accesible.

## 2. Fase de Identificación de Brechas y Planificación de Pruebas
*   [x] **IDB-2.1:** Analizar el informe de cobertura de JaCoCo para identificar las clases, métodos y líneas de código no cubiertas.
    *   **Propósito:** Determinar las áreas específicas que requieren nuevas pruebas.
    *   **Modo Estimado:** `Ask Mode` (para análisis y reporte).
    *   **Prerrequisitos:** ANL-1.1 completado.
    *   **Criterios de Calidad:** Identificación clara y detallada de todas las brechas de cobertura.
    *   **Nota:** Re-análisis basado en el informe más reciente (93% instrucciones, 89% ramas).
    *   **Brechas de Cobertura Identificadas:**
        *   **`peritaje.inmobiliario.integrador.controller.AppraisalController`**:
            *   `downloadPdf(String appraisalId)`: 0% de instrucciones, 0% de ramas. Líneas perdidas: 11 de 11.
            *   `getAppraisalsForCurrentUser()`: 0% de instrucciones. Líneas perdidas: 2 de 2.
        *   **`peritaje.inmobiliario.integrador.service.SupabaseAuthService`**:
            *   `lambda$signIn$0(HttpStatusCode)`: 0% de instrucciones, 0% de ramas. Líneas perdidas: 3 de 3.
            *   `lambda$signOut$0(HttpStatusCode)`: 0% de instrucciones. Líneas perdidas: 1 de 1.
            *   `lambda$signUp$0(HttpStatusCode)`: 0% de instrucciones. Líneas perdidas: 1 de 1.
        *   **`peritaje.inmobiliario.integrador.security.JwtService`**:
            *   `validateToken(String token)`: 52% de instrucciones, 100% de ramas. Líneas perdidas: 3 de 4.
            *   `extractUsername(String token)`: 100% de instrucciones, 75% de ramas. Líneas perdidas: 0 de 5 (rama no cubierta).
        *   **`peritaje.inmobiliario.integrador.IntegradorApplication`**:
            *   `main(String[] args)`: 0% de instrucciones. Líneas perdidas: 2 de 2.
*   [x] **IDB-2.2:** Diseñar casos de prueba unitarios y/o de integración para cubrir las brechas identificadas.
    *   **Propósito:** Crear un plan de pruebas detallado que aborde cada brecha, incluyendo casos límite y manejo de errores.
    *   **Modo Estimado:** `Code Mode`.
    *   **Prerrequisitos:** IDB-2.1 completado.
    *   **Criterios de Calidad:** Casos de prueba bien definidos, específicos y que apunten a la cobertura de las líneas faltantes.
    *   **Detalles de Casos de Prueba Diseñados:**
        *   **`peritaje.inmobiliario.integrador.controller.AuthController`**:
            *   **`signUp(AuthRequest authRequest)`**:
                *   Caso de éxito: Registro de usuario con `AuthRequest` válido.
                *   Caso de error (`UserAlreadyExistsException`): Email ya registrado.
                *   Caso de error (`SupabaseIntegrationException`): Error genérico de integración con Supabase.
                *   Caso de error (`MethodArgumentNotValidException`): `AuthRequest` inválido (email o password nulo/vacío).
            *   **`signIn(AuthRequest authRequest)`**:
                *   Caso de éxito: Inicio de sesión con `AuthRequest` válido.
                *   Caso de error (`InvalidCredentialsException`): Credenciales inválidas.
                *   Caso de error (`SupabaseIntegrationException`): Error genérico de integración con Supabase.
                *   Caso de error (`MethodArgumentNotValidException`): `AuthRequest` inválido.
        *   **`peritaje.inmobiliario.integrador.controller.AppraisalController`**:
            *   **`getAppraisalsForCurrentUser()`**:
                *   Caso de éxito: Retorno de lista vacía de tasaciones.
                *   Caso de éxito: Retorno de lista con múltiples tasaciones.
            *   **`downloadPdf(@RequestParam("appraisalId") String appraisalId)`**:
                *   Caso de éxito: Descarga de PDF con ID de tasación válido y `resultData` presente.
                *   Caso de error (`ResourceNotFoundException`): Tasación no encontrada por ID.
                *   Caso de error (`ResourceNotFoundException`): Tasación encontrada, pero `resultData` es nulo.
                *   Caso de error (`PdfGenerationException`): Error durante la generación del PDF.
                *   Caso de error (`IOException`): Error de I/O durante la descarga.
        *   **`peritaje.inmobiliario.integrador.service.UserContextService`**:
            *   **`getCurrentUserDetails()`**:
                *   Caso de éxito: Usuario autenticado y `principal` es `CustomUserDetails`.
                *   Caso de error (`ResourceNotFoundException`): No hay autenticación.
                *   Caso de error (`ResourceNotFoundException`): Autenticación presente, pero no autenticada.
                *   Caso de error (`ResourceNotFoundException`): `principal` no es `CustomUserDetails`.
            *   **`getCurrentUserId()`**:
                *   Caso de éxito: Retorno del ID de usuario.
                *   Caso de error (`ResourceNotFoundException`): No hay usuario autenticado (delegado a `getCurrentUserDetails`).
            *   **`getCurrentUserIdOptional()`**:
                *   Caso de éxito: Retorno de `Optional` con ID de usuario cuando está autenticado.
                *   Caso de éxito: Retorno de `Optional.empty()` cuando no hay autenticación.
                *   Caso de éxito: Retorno de `Optional.empty()` cuando `principal` no es `CustomUserDetails`.
        *   **`peritaje.inmobiliario.integrador.service.SupabaseAuthService`**:
            *   **Constructor:**
                *   Caso de éxito: Inicialización correcta del `WebClient` con `supabaseUrl` y `supabaseServiceKey`.
            *   **`signUp(AuthRequest authRequest)`**:
                *   Caso de éxito: Respuesta exitosa de Supabase.
                *   Caso de error (`UserAlreadyExistsException`): Supabase devuelve "Email already registered".
                *   Caso de error (`SupabaseIntegrationException`): Supabase devuelve otro error 4xx.
                *   Caso de error (`SupabaseIntegrationException`): Supabase devuelve error 5xx.
            *   **`signIn(AuthRequest authRequest)`**:
                *   Caso de éxito: Respuesta exitosa de Supabase.
                *   Caso de error (`InvalidCredentialsException`): Supabase devuelve "Invalid login credentials".
                *   Caso de error (`SupabaseIntegrationException`): Supabase devuelve otro error 4xx/5xx con cuerpo de error.
                *   Caso de error (`SupabaseIntegrationException`): Supabase devuelve error 4xx/5xx sin cuerpo de error o con cuerpo malformado (cubrir `catch (Exception e)` en la lambda).
            *   **`signOut(String accessToken)`**:
                *   Caso de éxito: Cierre de sesión exitoso.
                *   Caso de error (`SupabaseIntegrationException`): Supabase devuelve error 4xx.
        *   **`peritaje.inmobiliario.integrador.service.AppraisalService`**:
            *   **`getAppraisalsForCurrentUser()`**:
                *   Caso de éxito: Retorno de lista vacía.
                *   Caso de éxito: Retorno de lista con tasaciones.
            *   **`getAppraisalById(String id)`**:
                *   Caso de éxito: Tasación encontrada.
                *   Caso de error (`ResourceNotFoundException`): Tasación no encontrada.
                *   Caso de error (`IllegalArgumentException`): ID de tasación con formato inválido (no UUID).
            *   **`getAppraisalByIdAndCurrentUser(String id)`**:
                *   Caso de éxito: Tasación encontrada para el usuario actual.
                *   Caso de error (`ResourceNotFoundException`): Usuario no autenticado.
                *   Caso de error (`ResourceNotFoundException`): Tasación no encontrada por ID.
                *   Caso de error (`ResourceNotFoundException`): Tasación encontrada pero no pertenece al usuario actual.
                *   Caso de error (`IllegalArgumentException`): ID de tasación con formato inválido.
            *   **`updateAppraisalStatus(UUID id, String status)`**:
                *   Caso de éxito: Actualización de estado exitosa.
                *   Caso de error (`ResourceNotFoundException`): Tasación no encontrada.
            *   **`updateAppraisalResultData(UUID id, AppraisalDetailsDTO appraisalDetailsDTO)`**:
                *   Caso de éxito: Actualización de datos de resultado exitosa.
                *   Caso de error (`ResourceNotFoundException`): Tasación no encontrada.
                *   Caso de error (`IllegalArgumentException`): `appraisalDetailsDTO` inválido.
        *   **`peritaje.inmobiliario.integrador.service.PdfGenerationService`**:
            *   **`generatePdf(String templateName, Map<String, Object> dataModel)`**:
                *   Caso de éxito: Generación de PDF exitosa.
                *   Caso de error (`PdfGenerationException`): Error durante la generación de HTML.
                *   Caso de error (`PdfGenerationException`): Error durante la creación del PDF.
        *   **`peritaje.inmobiliario.integrador.service.HtmlGenerationServiceImpl`**:
            *   **`generateHtmlContent(String templateName, Map<String, Object> dataModel)`**:
                *   Caso de éxito: Generación de HTML con plantilla y datos válidos.
                *   Caso de error (`TemplateInputException` o similar de Thymeleaf): Plantilla no encontrada o inválida.
                *   Caso de error: `dataModel` nulo o vacío.
        *   **`peritaje.inmobiliario.integrador.security.JwtService`**:
            *   **`getSigningKey()`**:
                *   Caso de éxito: Retorno de `SecretKey` válido.
            *   **`extractAllClaims(String token)`**:
                *   Caso de éxito: Extracción de claims de un token válido.
                *   Casos de error: `SignatureException`, `MalformedJwtException`, `ExpiredJwtException`, `UnsupportedJwtException`, `IllegalArgumentException`.
            *   **`extractClaim(String token, Function<Claims, T> claimsResolver)`**:
                *   Caso de éxito: Extracción de un claim específico.
            *   **`extractUserId(String token)`**:
                *   Caso de éxito: Extracción de `userId` válido.
                *   Caso de error (`IllegalArgumentException`): `sub` no es un UUID válido.
            *   **`extractUsername(String token)`**:
                *   Caso de éxito: Extracción de email si está presente.
                *   Caso de éxito: Extracción de `sub` si email no está presente.
            *   **`extractExpiration(String token)`**:
                *   Caso de éxito: Extracción de fecha de expiración.
            *   **`isTokenExpired(String token)`**:
                *   Caso de éxito: Token no expirado.
                *   Caso de éxito: Token expirado (`ExpiredJwtException`).
                *   Caso de error (`Exception`): Cualquier otra excepción durante la extracción.
            *   **`validateToken(String token)`**:
                *   Caso de éxito: Token válido.
                *   Caso de error: Token inválido.
        *   **`peritaje.inmobiliario.integrador.security.JwtTokenFilter`**:
            *   **`doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)`**:
                *   Caso de éxito: Solicitud sin token JWT (público).
                *   Caso de éxito: Solicitud con token JWT válido.
                *   Caso de error: Token JWT ausente o formato inválido.
                *   Caso de error: Token JWT inválido.
                *   Caso de error: Excepción durante la extracción de `userId` o `username`.
        *   **`peritaje.inmobiliario.integrador.security.CustomUserDetailsService`**:
            *   **`loadUserByUsername(String token)`**:
                *   Caso de éxito: Token válido, usuario encontrado.
                *   Caso de error (`UsernameNotFoundException`): Token nulo o vacío.
                *   Caso de error (`UsernameNotFoundException`): `userId` no encontrado en el token.
                *   Caso de error (`UsernameNotFoundException`): Token inválido (cubrir varias excepciones JWT).
        *   **`peritaje.inmobiliario.integrador.security.CustomUserDetails`**:
            *   **Constructor:**
                *   Caso de éxito: Creación de instancia con `userId`, `username` y `authorities`.
            *   **Getters:**
                *   Caso de éxito: `getUserId()`, `getUsername()`, `getAuthorities()`, `getPassword()` (debe ser nulo).
            *   **Métodos booleanos:**
                *   Caso de éxito: `isAccountNonExpired()`, `isAccountNonLocked()`, `isCredentialsNonExpired()`, `isEnabled()` (todos deben devolver `true`).
        *   **`peritaje.inmobiliario.integrador.security.config.SecurityConfig`**:
            *   **`securityFilterChain(HttpSecurity http)`**:
                *   Caso de éxito: Configuración de seguridad aplicada correctamente (verificar con pruebas de integración a nivel de controlador).
        *   **`peritaje.inmobiliario.integrador.IntegradorApplication`**:
            *   **`main(String[] args)`**:
                *   Caso de éxito: La aplicación Spring Boot se inicia correctamente (prueba de integración de contexto completo).
        *   **`peritaje.inmobiliario.integrador.dto.ErrorResponse`**:
            *   **Constructores:**
                *   Caso de éxito: Constructor por defecto.
                *   Caso de éxito: Constructor con `message` y `error`.
                *   Caso de éxito: Constructor con `message`, `msg`, `error` y `statusCode`.
            *   **Getters y Setters:**
                *   Caso de éxito: Probar todos los getters y setters.
            *   **`equals(Object o)`**:
                *   Caso de éxito: Objetos iguales, diferentes, nulo, diferente clase.
            *   **`hashCode()`**:
                *   Caso de éxito: Hash code consistente.
            *   **`toString()`**:
                *   Caso de éxito: Formato de cadena correcto.
        *   **`peritaje.inmobiliario.integrador.exception.GlobalExceptionHandler`**:
            *   **`handleValidationExceptions(MethodArgumentNotValidException ex)`**: Manejo y retorno de `BAD_REQUEST`.
            *   **`handleAuthenticationException(AuthenticationException ex)`**: Manejo y retorno de `UNAUTHORIZED`.
            *   **`handleResourceNotFoundException(ResourceNotFoundException ex)`**: Manejo y retorno de `NOT_FOUND`.
            *   **`handleUserAlreadyExistsException(UserAlreadyExistsException ex)`**: Manejo y retorno de `CONFLICT`.
            *   **`handleSupabaseIntegrationException(SupabaseIntegrationException ex)`**: Manejo y retorno de `SERVICE_UNAVAILABLE`.
            *   **`handlePdfGenerationException(PdfGenerationException ex)`**: Manejo y retorno de `INTERNAL_SERVER_ERROR`.
            *   **`handleInvalidCredentialsException(InvalidCredentialsException ex)`**: Manejo y retorno de `UNAUTHORIZED`.
            *   **`handleGlobalException(Exception ex)`**: Manejo y retorno de `INTERNAL_SERVER_ERROR`.
        *   **`peritaje.inmobiliario.integrador.exception.ResourceNotFoundException`**:
            *   **Constructores:**
                *   Caso de éxito: Constructor con `message`.
                *   Caso de éxito: Constructor con `message` y `cause`.

## 3. Fase de Implementación de Pruebas
*   [x] **IMPL-3.1:** Escribir pruebas unitarias para las clases de servicio y utilidad que carecen de cobertura.
    *   **Propósito:** Aumentar la cobertura de la lógica de negocio y componentes internos.
    *   **Modo Estimado:** `Code Mode`.
    *   **Prerrequisitos:** IDB-2.2 completado.
    *   **Criterios de Calidad:** Pruebas siguiendo principios TDD (si aplica), claras, concisas, que pasen y que aumenten la cobertura. Adherencia a SOLID y DRY.
*   [x] **IMPL-3.2:** Escribir pruebas de integración para los controladores y servicios que interactúan con dependencias externas (ej. base de datos, servicios de terceros).
    *   **Propósito:** Asegurar la cobertura de los flujos de extremo a extremo y la interacción entre componentes.
    *   **Modo Estimado:** `Code Mode`.
    *   **Prerrequisitos:** IDB-2.2 completado.
    *   **Criterios de Calidad:** Pruebas robustas, con mocks/stubs adecuados para dependencias, que pasen y aumenten la cobertura.
*   [x] **IMPL-3.3:** Implementar pruebas para el manejo de excepciones y casos límite en todas las capas.
    *   **Propósito:** Garantizar que el sistema se comporte correctamente bajo condiciones inesperadas o entradas inválidas.
    *   **Modo Estimado:** `Code Mode`.
    *   **Prerrequisitos:** IDB-2.2 completado.
    *   **Criterios de Calidad:** Pruebas que validen el manejo de errores, mensajes de error apropiados y que no expongan información sensible.
*   [x] **IMPL-3.4:** Escribir pruebas adicionales para cubrir las brechas restantes identificadas en el re-análisis de cobertura. (Se verificó que las pruebas existentes ya cubren los escenarios de `IntegradorApplication`, `AppraisalController` y `JwtService` para asegurar el registro de cobertura por JaCoCo).
    *   **Propósito:** Alcanzar el 100% de cobertura de instrucciones y ramas.
    *   **Modo Estimado:** `Code Mode`.
    *   **Prerrequisitos:** IDB-2.1 completado.
    *   **Criterios de Calidad:** Pruebas claras, concisas, que pasen y que cubran las líneas y ramas faltantes. Adherencia a SOLID y DRY.
    *   **Detalles de Brechas a Cubrir:**
        *   **`peritaje.inmobiliario.integrador.IntegradorApplication`**:
            *   `main(String[] args)`: Asegurar que exista una prueba de integración (`@SpringBootTest`) que inicie el contexto de Spring para cubrir el método `main`.
        *   **`peritaje.inmobiliario.integrador.controller.AppraisalController`**:
            *   `downloadPdf(String appraisalId)`: Añadir/verificar pruebas para el caso donde `appraisal.getResultData()` es `null`, lanzando `ResourceNotFoundException`.
            *   `getAppraisalsForCurrentUser()`: Añadir/verificar pruebas para asegurar la cobertura completa de este método.
        *   **`peritaje.inmobiliario.integrador.security.JwtService`**:
            *   `isTokenExpired(String token)`: Añadir pruebas que provoquen la `catch (Exception e)` genérica (líneas 68-73).
            *   `validateToken(String token)`: Añadir pruebas que provoquen la `catch (Exception e)` genérica (líneas 79-84), por ejemplo, con un token malformado o con firma inválida.
            *   `extractUsername(String token)`: Añadir pruebas que cubran la rama de fallback (`return claims.get("sub", String.class);`) cuando el claim 'email' no está presente o está vacío.

## 4. Fase de Refactorización (Condicional)
*   [ ] **REF-4.1:** Refactorizar el código existente para mejorar la testabilidad, si es necesario.
    *   **Propósito:** Simplificar la lógica, reducir dependencias y mejorar la modularidad para facilitar la escritura de pruebas.
    *   **Modo Estimado:** `Code Mode`.
    *   **Prerrequisitos:** IMPL-3.1, IMPL-3.2, IMPL-3.3 (si se identifican problemas de testabilidad durante la implementación de pruebas).
    *   **Criterios de Calidad:** La refactorización no debe introducir nuevos errores y todas las pruebas existentes deben seguir pasando. Adherencia a SOLID.

## 5. Fase de Verificación de Cobertura Final
*   [x] **VER-5.1:** Re-ejecutar el comando Maven para generar el informe de cobertura de JaCoCo. (Cobertura: 98% instrucciones, 100% ramas) - Completado: Las pruebas se ejecutaron exitosamente. La verificación manual del informe de cobertura se realizará en la subtarea VER-5.2.
*   [ ] **VER-5.2:** Ejecutar pruebas Maven y generar informe JaCoCo.
    *   **Propósito:** Generar el informe de cobertura de JaCoCo para verificar el 100% de cobertura.
    *   **Modo Estimado:** `Code Mode`.
    *   **Prerrequisitos:** Todas las subtareas de IMPL-3.x y REF-4.1 (si aplica) completadas.
    *   **Criterios de Calidad:** El informe debe generarse sin errores y ser accesible en `target/site/jacoco/index.html`.
    *   **Propósito:** Confirmar que se ha alcanzado el 100% de cobertura.
    *   **Modo Estimado:** `Code Mode`.
    *   **Prerrequisitos:** Todas las subtareas de IMPL-3.x y REF-4.1 (si aplica) completadas.
    *   **Criterios de Calidad:** El informe debe mostrar 100% de cobertura de línea y rama.
    *   **Nota:** Cobertura actual: 93% de instrucciones y 89% de ramas.

## 7. Fase de Depuración de Cobertura
*   [x] **DBG-7.1:** Investigar la configuración de JaCoCo en el `pom.xml` y el entorno de Maven para diagnosticar por qué el informe de cobertura muestra 0%.
    *   **Propósito:** Identificar y resolver la causa raíz de la falta de reporte de cobertura.
    *   **Modo Estimado:** `Code Mode` (para leer `pom.xml` y ejecutar comandos de diagnóstico), `Ask Mode` (para análisis de configuración).
    *   **Prerrequisitos:** VER-5.1 bloqueado.
    *   **Criterios de Calidad:** Identificación clara del problema y propuesta de solución.
*   [x] **DBG-7.2:** Investigar discrepancia de cobertura de JaCoCo con Debug Mode - Los problemas de configuración de Maven/JaCoCo y la corrección de `AuthControllerTest.signIn_invalidCredentials` fueron resueltos en las subtareas `DBG-7.1` y `REVAL-3.2` respectivamente.
    *   **Propósito:** Entender por qué las líneas y ramas de código especificadas no están siendo reportadas como cubiertas.
    *   **Modo Estimado:** `Debug Mode`.
    *   **Prerrequisitos:** VER-5.1 completado y DBG-7.1 completado.
    *   **Criterios de Calidad:** Identificación clara de la causa raíz y propuesta de solución.

## 6. Fase de Documentación
*   [ ] **DOC-6.1:** Actualizar la documentación de pruebas o `context_project.md` (si el usuario lo aprueba) para reflejar la estrategia de cobertura y cualquier cambio significativo.
    *   **Propósito:** Mantener la documentación del proyecto actualizada.
    *   **Modo Estimado:** `Orchestrator Mode` (para proponer y coordinar), `Code Mode` (para la escritura).
    *   **Prerrequisitos:** VER-5.1 completado.
    *   **Criterios de Calidad:** Documentación clara, concisa y precisa.

**Estado General:**
*   [ ] Todo
*   [>] In Progress
*   [x] Completed
*   [-] Canceled - Reason
*   [!] Blocked - Reason
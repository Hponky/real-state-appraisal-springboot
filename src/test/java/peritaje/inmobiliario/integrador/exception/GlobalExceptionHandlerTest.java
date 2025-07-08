package peritaje.inmobiliario.integrador.exception;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 import org.springframework.web.bind.MethodArgumentNotValidException;

 import peritaje.inmobiliario.integrador.dto.ErrorResponse;
 import reactor.core.publisher.Mono;
 import reactor.test.StepVerifier;
 
 public class GlobalExceptionHandlerTest {
 
     private GlobalExceptionHandler globalExceptionHandler;
 
     @BeforeEach
     void setUp() {
         globalExceptionHandler = new GlobalExceptionHandler();
     }
 
     @Test
     void handleMethodArgumentNotValidException_shouldReturnBadRequest() {
         BindingResult bindingResult = mock(BindingResult.class);
         FieldError fieldError = new FieldError("objectName", "fieldName", "default message");
         when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));
         MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
 
         Mono<ResponseEntity<ErrorResponse>> monoResponse = globalExceptionHandler.handleValidationExceptions(ex);
 
         StepVerifier.create(monoResponse)
                 .assertNext(response -> {
                     assertNotNull(response);
                     assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                     assertNotNull(response.getBody());
                     assertEquals("Validation Failed", response.getBody().getMessage());
                     assertEquals("Los datos de entrada no son v\u00e1lidos. Por favor, revise su solicitud.",
                             response.getBody().getMsg());
                 })
                 .verifyComplete();
     }
 
     @Test
     void handleAuthenticationException_shouldReturnUnauthorized() {
         BadCredentialsException ex = new BadCredentialsException("Invalid username or password");
         Mono<ResponseEntity<ErrorResponse>> monoResponse = globalExceptionHandler.handleAuthenticationException(ex);
 
         StepVerifier.create(monoResponse)
                 .assertNext(response -> {
                     assertNotNull(response);
                     assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
                     assertNotNull(response.getBody());
                     assertEquals("Authentication Failed", response.getBody().getMessage());
                     assertEquals("Credenciales de autenticacion invalidas.", response.getBody().getMsg());
                 })
                 .verifyComplete();
     }
 
     @Test
     void handleResourceNotFoundException_shouldReturnNotFound() {
         ResourceNotFoundException ex = new ResourceNotFoundException("Resource not found");
         Mono<ResponseEntity<ErrorResponse>> monoResponse = globalExceptionHandler.handleResourceNotFoundException(ex);
 
         StepVerifier.create(monoResponse)
                 .assertNext(response -> {
                     assertNotNull(response);
                     assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                     assertNotNull(response.getBody());
                     assertEquals("Recurso no encontrado", response.getBody().getMessage());
                     assertEquals("Resource not found", response.getBody().getMsg());
                 })
                 .verifyComplete();
     }
 
     @Test
     void handleUserAlreadyExistsException_shouldReturnConflict() {
         UserAlreadyExistsException ex = new UserAlreadyExistsException("User already exists");
         Mono<ResponseEntity<ErrorResponse>> monoResponse = globalExceptionHandler.handleUserAlreadyExistsException(ex);
 
         StepVerifier.create(monoResponse)
                 .assertNext(response -> {
                     assertNotNull(response);
                     assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
                     assertNotNull(response.getBody());
                     assertEquals("El usuario ya existe.", response.getBody().getMessage());
                     assertEquals("El usuario ya existe.", response.getBody().getError());
                 })
                 .verifyComplete();
     }
 
     @Test
     void handleSupabaseIntegrationException_shouldReturnServiceUnavailable() {
         SupabaseIntegrationException ex = new SupabaseIntegrationException("Supabase error");
         Mono<ResponseEntity<ErrorResponse>> monoResponse = globalExceptionHandler.handleSupabaseIntegrationException(ex);
 
         StepVerifier.create(monoResponse)
                 .assertNext(response -> {
                     assertNotNull(response);
                     assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
                     assertNotNull(response.getBody());
                     assertEquals("Error de integración con el servicio externo. Intente de nuevo más tarde.", response.getBody().getMessage());
                     assertEquals("Error de integración con el servicio externo. Intente de nuevo más tarde.",
                             response.getBody().getError());
                 })
                 .verifyComplete();
     }
 
     @Test
     void handlePdfGenerationException_shouldReturnInternalServerError() {
         PdfGenerationException ex = new PdfGenerationException("PDF error");
         Mono<ResponseEntity<ErrorResponse>> monoResponse = globalExceptionHandler.handlePdfGenerationException(ex);
 
         StepVerifier.create(monoResponse)
                 .assertNext(response -> {
                     assertNotNull(response);
                     assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                     assertNotNull(response.getBody());
                     assertEquals("Error interno del servidor", response.getBody().getMessage());
                     assertEquals("Fallo en la generacion del PDF", response.getBody().getError());
                 })
                 .verifyComplete();
     }
 
     @Test
     void handleInvalidCredentialsException_shouldReturnUnauthorized() {
         InvalidCredentialsException ex = new InvalidCredentialsException("Invalid credentials");
         Mono<ResponseEntity<ErrorResponse>> monoResponse = globalExceptionHandler.handleInvalidCredentialsException(ex);
 
         StepVerifier.create(monoResponse)
                 .assertNext(response -> {
                     assertNotNull(response);
                     assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
                     assertNotNull(response.getBody());
                     assertEquals("Credenciales de autenticacion invalidas.", response.getBody().getMessage());
                     assertEquals("Credenciales de autenticacion invalidas.", response.getBody().getError());
                 })
                 .verifyComplete();
     }
 
     @Test
     void handleGlobalException_shouldReturnInternalServerError() {
         Exception ex = new Exception("Generic error");
         Mono<ResponseEntity<ErrorResponse>> monoResponse = globalExceptionHandler.handleGlobalException(ex);
 
         StepVerifier.create(monoResponse)
                 .assertNext(response -> {
                     assertNotNull(response);
                     assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                     assertNotNull(response.getBody());
                     assertEquals("Internal Server Error", response.getBody().getMessage());
                     assertEquals("Ha ocurrido un error inesperado. Por favor, intente de nuevo más tarde.",
                             response.getBody().getError());
                 })
                 .verifyComplete();
     }
 }
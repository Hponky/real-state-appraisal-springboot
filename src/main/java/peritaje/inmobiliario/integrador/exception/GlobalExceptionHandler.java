package peritaje.inmobiliario.integrador.exception;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import peritaje.inmobiliario.integrador.dto.ErrorResponse;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        
        logger.warn("Validation failed for request: {}", errors);
        
        ErrorResponse errorResponse = new ErrorResponse("Validation Failed",
                "Los datos de entrada no son válidos. Por favor, revise su solicitud.",
                "Validation Failed", HttpStatus.BAD_REQUEST.value());
        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(AuthenticationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleAuthenticationException(AuthenticationException ex) {
        logger.warn("Authentication failed: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse("Authentication Failed", "Credenciales de autenticacion invalidas.", "Authentication Failed", HttpStatus.UNAUTHORIZED.value());
        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        logger.warn("Resource not found: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse("Resource Not Found", ex.getMessage(), "Resource Not Found", HttpStatus.NOT_FOUND.value());
        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        logger.warn("User already exists: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse("User Already Exists", "El usuario ya existe.");
        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT));
    }

    @ExceptionHandler(SupabaseIntegrationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleSupabaseIntegrationException(SupabaseIntegrationException ex) {
        logger.error("Supabase integration error: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse("Service Unavailable",
                "Error de integración con el servicio externo. Intente de nuevo más tarde.");
        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE));
    }

    @ExceptionHandler(PdfGenerationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handlePdfGenerationException(PdfGenerationException ex) {
        logger.error("PDF generation error: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse("PDF Generation Failed",
                "Error al generar el documento PDF. Intente de nuevo mas tarde.");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return Mono.just(new ResponseEntity<>(errorResponse, headers, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        logger.warn("Invalid credentials: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse("Invalid Credentials",
                "Credenciales de autenticacion invalidas.");
        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGlobalException(Exception ex) {
        logger.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse("Internal Server Error",
                "Ha ocurrido un error inesperado. Por favor, intente de nuevo más tarde.");
        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
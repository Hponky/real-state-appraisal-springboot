package peritaje.inmobiliario.integrador.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class SupabaseIntegrationException extends RuntimeException {
    public SupabaseIntegrationException(String message) {
        super(message);
    }

    public SupabaseIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
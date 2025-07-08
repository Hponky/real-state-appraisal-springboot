package peritaje.inmobiliario.integrador.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class SupabaseIntegrationExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String errorMessage = "Supabase integration error";
        SupabaseIntegrationException exception = new SupabaseIntegrationException(errorMessage);
        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String errorMessage = "Supabase integration error with a cause";
        Throwable cause = new RuntimeException("Root cause");
        SupabaseIntegrationException exception = new SupabaseIntegrationException(errorMessage, cause);
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
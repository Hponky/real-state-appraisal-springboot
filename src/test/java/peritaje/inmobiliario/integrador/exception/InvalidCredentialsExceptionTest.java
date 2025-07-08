package peritaje.inmobiliario.integrador.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class InvalidCredentialsExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String errorMessage = "Invalid credentials provided";
        InvalidCredentialsException exception = new InvalidCredentialsException(errorMessage);
        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String errorMessage = "Invalid credentials with a cause";
        Throwable cause = new RuntimeException("Root cause");
        InvalidCredentialsException exception = new InvalidCredentialsException(errorMessage, cause);
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
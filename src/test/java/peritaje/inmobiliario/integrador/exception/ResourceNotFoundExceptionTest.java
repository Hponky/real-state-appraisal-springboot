package peritaje.inmobiliario.integrador.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ResourceNotFoundExceptionTest {

    @Test
    void constructorWithMessage_shouldCreateExceptionWithCorrectMessage() {
        String message = "Resource not found for ID: 123";
        ResourceNotFoundException exception = new ResourceNotFoundException(message);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void constructorWithMessageAndCause_shouldCreateExceptionWithCorrectMessageAndCause() {
        String message = "Resource not found due to external error";
        Throwable cause = new RuntimeException("External service unavailable");
        ResourceNotFoundException exception = new ResourceNotFoundException(message, cause);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void isRuntimeException_shouldReturnTrue() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Test");
        assertTrue(exception instanceof RuntimeException);
    }
}
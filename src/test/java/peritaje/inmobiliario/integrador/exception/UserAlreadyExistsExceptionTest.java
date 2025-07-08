package peritaje.inmobiliario.integrador.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class UserAlreadyExistsExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String errorMessage = "User already exists";
        UserAlreadyExistsException exception = new UserAlreadyExistsException(errorMessage);
        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String errorMessage = "User already exists with a cause";
        Throwable cause = new RuntimeException("Root cause");
        UserAlreadyExistsException exception = new UserAlreadyExistsException(errorMessage, cause);
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
package peritaje.inmobiliario.integrador.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class PdfGenerationExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String errorMessage = "Error generating PDF";
        PdfGenerationException exception = new PdfGenerationException(errorMessage);
        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String errorMessage = "Error generating PDF with a cause";
        Throwable cause = new RuntimeException("Root cause");
        PdfGenerationException exception = new PdfGenerationException(errorMessage, cause);
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
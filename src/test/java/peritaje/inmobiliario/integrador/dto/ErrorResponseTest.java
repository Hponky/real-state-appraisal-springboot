package peritaje.inmobiliario.integrador.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ErrorResponseTest {

    @Test
    void defaultConstructor_shouldCreateObjectWithNullFields() {
        ErrorResponse errorResponse = new ErrorResponse();
        assertNull(errorResponse.getMessage());
        assertNull(errorResponse.getMsg());
        assertNull(errorResponse.getError());
        assertNull(errorResponse.getStatusCode());
    }

    @Test
    void constructorWithMessageAndError_shouldSetCorrectFields() {
        String message = "Test Message";
        String error = "Test Error";
        ErrorResponse errorResponse = new ErrorResponse(message, error);
        assertEquals(message, errorResponse.getMessage());
        assertNull(errorResponse.getMsg());
        assertEquals(error, errorResponse.getError());
        assertNull(errorResponse.getStatusCode());
    }

    @Test
    void fullConstructor_shouldSetAllFieldsCorrectly() {
        String message = "Full Message";
        String msg = "Full Msg";
        String error = "Full Error";
        Integer statusCode = 500;
        ErrorResponse errorResponse = new ErrorResponse(message, msg, error, statusCode);
        assertEquals(message, errorResponse.getMessage());
        assertEquals(msg, errorResponse.getMsg());
        assertEquals(error, errorResponse.getError());
        assertEquals(statusCode, errorResponse.getStatusCode());
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        ErrorResponse errorResponse = new ErrorResponse();

        String message = "New Message";
        errorResponse.setMessage(message);
        assertEquals(message, errorResponse.getMessage());

        String msg = "New Msg";
        errorResponse.setMsg(msg);
        assertEquals(msg, errorResponse.getMsg());

        String error = "New Error";
        errorResponse.setError(error);
        assertEquals(error, errorResponse.getError());

        Integer statusCode = 404;
        errorResponse.setStatusCode(statusCode);
        assertEquals(statusCode, errorResponse.getStatusCode());
    }

    @Test
    void equals_shouldReturnTrue_forSameObjects() {
        ErrorResponse errorResponse1 = new ErrorResponse("msg1", "err1", "errorType1", 400);
        ErrorResponse errorResponse2 = new ErrorResponse("msg1", "err1", "errorType1", 400);
        ErrorResponse errorResponse3 = errorResponse1;

        assertTrue(errorResponse1.equals(errorResponse2));
        assertTrue(errorResponse1.equals(errorResponse3));
    }

    @Test
    void equals_shouldReturnFalse_forDifferentObjects() {
        ErrorResponse errorResponse1 = new ErrorResponse("msg1", "err1", "errorType1", 400);
        ErrorResponse errorResponse2 = new ErrorResponse("msg2", "err1", "errorType1", 400); // Different message
        ErrorResponse errorResponse3 = new ErrorResponse("msg1", "err2", "errorType1", 400); // Different msg
        ErrorResponse errorResponse4 = new ErrorResponse("msg1", "err1", "errorType2", 400); // Different error
        ErrorResponse errorResponse5 = new ErrorResponse("msg1", "err1", "errorType1", 500); // Different status code
        ErrorResponse errorResponse6 = new ErrorResponse("msg1", "err1"); // Different constructor

        assertFalse(errorResponse1.equals(errorResponse2));
        assertFalse(errorResponse1.equals(errorResponse3));
        assertFalse(errorResponse1.equals(errorResponse4));
        assertFalse(errorResponse1.equals(errorResponse5));
        assertFalse(errorResponse1.equals(errorResponse6));
        assertFalse(errorResponse1.equals(null));
        assertFalse(errorResponse1.equals(new Object()));
    }

    @Test
    void hashCode_shouldBeConsistent_forEqualObjects() {
        ErrorResponse errorResponse1 = new ErrorResponse("msg1", "err1", "errorType1", 400);
        ErrorResponse errorResponse2 = new ErrorResponse("msg1", "err1", "errorType1", 400);
        assertEquals(errorResponse1.hashCode(), errorResponse2.hashCode());
    }

    @Test
    void toString_shouldContainAllFields() {
        String message = "Test Message";
        String msg = "Test Msg";
        String error = "Test Error";
        Integer statusCode = 400;
        ErrorResponse errorResponse = new ErrorResponse(message, msg, error, statusCode);
        String toStringResult = errorResponse.toString();

        assertTrue(toStringResult.contains("message='" + message + "'"));
        assertTrue(toStringResult.contains("msg='" + msg + "'"));
        assertTrue(toStringResult.contains("error='" + error + "'"));
        assertTrue(toStringResult.contains("statusCode=" + statusCode));
    }
}
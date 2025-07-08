package peritaje.inmobiliario.integrador.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import org.junit.jupiter.api.Test;

class AuthRequestTest {

    @Test
    void testGettersAndSetters() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail("test@example.com");
        authRequest.setPassword("password123");

        assertEquals("test@example.com", authRequest.getEmail());
        assertEquals("password123", authRequest.getPassword());
    }

    @Test
    void testConstructorWithParameters() {
        AuthRequest authRequest = new AuthRequest("test@example.com", "password123");
        assertEquals("test@example.com", authRequest.getEmail());
        assertEquals("password123", authRequest.getPassword());
    }

    @Test
    void testEqualsAndHashCode() {
        AuthRequest authRequest1 = new AuthRequest("test@example.com", "password123");
        AuthRequest authRequest2 = new AuthRequest("test@example.com", "password123");
        AuthRequest authRequest3 = new AuthRequest("different@example.com", "password123");

        assertEquals(authRequest1, authRequest2);
        assertNotEquals(authRequest1, authRequest3);
        assertEquals(authRequest1.hashCode(), authRequest2.hashCode());
        assertNotEquals(authRequest1.hashCode(), authRequest3.hashCode());
    }

    @Test
    void testToString() {
        AuthRequest authRequest = new AuthRequest("test@example.com", "password123");
        String expectedToString = "AuthRequest(email=test@example.com, password=password123)";
        assertEquals(expectedToString, authRequest.toString());
    }
}
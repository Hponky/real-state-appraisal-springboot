package peritaje.inmobiliario.integrador.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class AuthResponseTest {

    @Test
    void testGettersAndSetters() {
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken("access_token");
        authResponse.setRefreshToken("refresh_token");
        authResponse.setExpiresIn(3600);
        authResponse.setTokenType("bearer");
        authResponse.setUser(null);

        assertEquals("access_token", authResponse.getAccessToken());
        assertEquals("refresh_token", authResponse.getRefreshToken());
        assertEquals(3600, authResponse.getExpiresIn());
        assertEquals("bearer", authResponse.getTokenType());
        assertNull(authResponse.getUser());
    }

    @Test
    void testConstructorWithParameters() {
        AuthResponse authResponse = new AuthResponse("access_token", "bearer", 3600, "refresh_token", null);
        assertEquals("access_token", authResponse.getAccessToken());
        assertEquals("refresh_token", authResponse.getRefreshToken());
        assertEquals(3600, authResponse.getExpiresIn());
        assertEquals("bearer", authResponse.getTokenType());
        assertNull(authResponse.getUser());
    }

    @Test
    void testEqualsAndHashCode() {
        AuthResponse authResponse1 = new AuthResponse("access_token", "refresh_token", 3600, "bearer", null);
        AuthResponse authResponse2 = new AuthResponse("access_token", "refresh_token", 3600, "bearer", null);
        AuthResponse authResponse3 = new AuthResponse("different_token", "refresh_token", 3600, "bearer", null);

        assertEquals(authResponse1, authResponse2);
        assertNotEquals(authResponse1, authResponse3);
        assertEquals(authResponse1.hashCode(), authResponse2.hashCode());
        assertNotEquals(authResponse1.hashCode(), authResponse3.hashCode());
    }

    @Test
    void testToString() {
        AuthResponse authResponse = new AuthResponse("access_token", "bearer", 3600, "refresh_token", null);
        String expectedToString = "AuthResponse(accessToken=access_token, tokenType=bearer, expiresIn=3600, refreshToken=refresh_token, user=null)";
        assertEquals(expectedToString, authResponse.toString());
    }
}
package peritaje.inmobiliario.integrador.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.anyString;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private String testSecret;
    private String validToken;
    private String expiredToken;
    private String tokenWithoutEmail;
    private String malformedToken;
    private String invalidSignatureToken;

    @BeforeEach
    void setUp() {
        testSecret = "thisisasecretkeyforjwttokensthatissufficientlylongforhmacsha256"; // 64 bytes for HS256
        ReflectionTestUtils.setField(jwtService, "jwtSecret", testSecret);

        // Generate a valid token
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", UUID.randomUUID().toString());
        claims.put("email", "test@example.com");
        validToken = Jwts.builder()
                .claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)))
                .signWith(Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();

        // Generate an expired token
        expiredToken = Jwts.builder()
                .claims(claims)
                .issuedAt(new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2)))
                .expiration(new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)))
                .signWith(Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();

        // Generate a token without email claim
        Map<String, Object> claimsWithoutEmail = new HashMap<>();
        claimsWithoutEmail.put("sub", UUID.randomUUID().toString());
        tokenWithoutEmail = Jwts.builder()
                .claims(claimsWithoutEmail)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)))
                .signWith(Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();

        // Malformed token
        malformedToken = "malformed.jwt.token";

        // Invalid signature token
        String wrongSecret = "wrongsecretkeyforjwttokensthatissufficientlylongforhmacsha256";
        invalidSignatureToken = Jwts.builder()
                .claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)))
                .signWith(Keys.hmacShaKeyFor(wrongSecret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    void extractAllClaims_validToken_returnsClaims() {
        Claims claims = assertDoesNotThrow(() -> jwtService.extractAllClaims(validToken));
        assertNotNull(claims);
        assertEquals("test@example.com", claims.get("email", String.class));
    }

    @Test
    void extractAllClaims_malformedToken_throwsException() {
        assertThrows(io.jsonwebtoken.MalformedJwtException.class, () -> jwtService.extractAllClaims(malformedToken));
    }

    @Test
    void extractAllClaims_expiredToken_throwsExpiredJwtException() {
        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> jwtService.extractAllClaims(expiredToken));
    }

    @Test
    void extractAllClaims_invalidSignatureToken_throwsSignatureException() {
        assertThrows(io.jsonwebtoken.security.SignatureException.class, () -> jwtService.extractAllClaims(invalidSignatureToken));
    }

    @Test
    void extractClaim_validTokenAndClaim_returnsClaimValue() {
        String email = jwtService.extractClaim(validToken, claims -> claims.get("email", String.class));
        assertEquals("test@example.com", email);
    }

    @Test
    void extractClaim_validTokenAndNonExistentClaim_returnsNull() {
        String nonExistentClaim = jwtService.extractClaim(validToken, claims -> claims.get("nonExistent", String.class));
        assertNull(nonExistentClaim);
    }

    @Test
    void extractUserId_validToken_returnsUserId() {
        UUID expectedUserId = UUID.fromString(jwtService.extractClaim(validToken, claims -> claims.get("sub", String.class)));
        UUID actualUserId = jwtService.extractUserId(validToken);
        assertEquals(expectedUserId, actualUserId);
    }

    @Test
    void extractUserId_tokenWithInvalidUuidFormat_throwsIllegalArgumentException() {
        String invalidUuidToken = Jwts.builder()
                .claims(Map.of("sub", "not-a-valid-uuid"))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)))
                .signWith(Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
        assertThrows(IllegalArgumentException.class, () -> jwtService.extractUserId(invalidUuidToken));
    }

    @Test
    void extractUsername_tokenWithEmail_returnsEmail() {
        String username = jwtService.extractUsername(validToken);
        assertEquals("test@example.com", username);
    }

    @Test
    void extractUsername_tokenWithoutEmail_returnsSub() {
        String username = jwtService.extractUsername(tokenWithoutEmail);
        assertNotNull(username);
        assertTrue(username.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")); // Check if it's a UUID format
    }

    @Test
    void extractExpiration_validToken_returnsExpirationDate() {
        Date expiration = jwtService.extractExpiration(validToken);
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }
 
     @Test
     void extractUsername_tokenWithEmptyEmail_returnsSub() {
         Map<String, Object> claims = new HashMap<>();
         UUID sub = UUID.randomUUID();
         claims.put("sub", sub.toString());
         claims.put("email", "");
 
         String tokenWithEmptyEmail = Jwts.builder()
                 .claims(claims)
                 .issuedAt(new Date(System.currentTimeMillis()))
                 .expiration(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)))
                 .signWith(Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                 .compact();
 
         String username = jwtService.extractUsername(tokenWithEmptyEmail);
         assertEquals(sub.toString(), username);
     }
 
     @Test
     void isTokenExpired_expiredToken_returnsTrue() {
         // Using ReflectionTestUtils to access private method for testing purposes
         Boolean expired = (Boolean) ReflectionTestUtils.invokeMethod(jwtService, "isTokenExpired", expiredToken);
         assertTrue(expired);
     }
 
     @Test
     void isTokenExpired_validToken_returnsFalse() {
         // Using ReflectionTestUtils to access private method for testing purposes
         Boolean expired = (Boolean) ReflectionTestUtils.invokeMethod(jwtService, "isTokenExpired", validToken);
         assertFalse(expired);
     }
 
     @Test
     void isTokenExpired_genericException_returnsTrue() {
         JwtService spyJwtService = org.mockito.Mockito.spy(jwtService);
         org.mockito.Mockito.doThrow(new RuntimeException("Generic Test Exception"))
             .when(spyJwtService).extractExpiration(anyString());
 
         Boolean isExpired = (Boolean) ReflectionTestUtils.invokeMethod(spyJwtService, "isTokenExpired", "anytoken");
         assertTrue(isExpired);
     }
 
     @Test
     void validateToken_validToken_returnsTrue() {
        assertTrue(jwtService.validateToken(validToken));
    }

    @Test
    void validateToken_expiredToken_returnsFalse() {
        assertFalse(jwtService.validateToken(expiredToken));
    }

    @Test
    void validateToken_malformedToken_returnsFalse() {
        assertFalse(jwtService.validateToken(malformedToken));
    }

    @Test
    void validateToken_invalidSignatureToken_returnsFalse() {
        assertFalse(jwtService.validateToken(invalidSignatureToken));
    }
}
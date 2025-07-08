package peritaje.inmobiliario.integrador.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas para JwtService")
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private final String testSecret = "a-very-secure-secret-key-for-testing-purposes-that-is-long-enough";
    private final SecretKey signingKey = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));
    private final UUID userId = UUID.randomUUID();
    private final String userEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "jwtSecret", testSecret);
    }

    private String createToken(Date expiration, Date issuedAt, String subject, String email) {
        return Jwts.builder()
                .subject(subject)
                .claim("email", email)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }
    
    private String createTokenWithCustomKey(Date expiration, SecretKey key) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", userEmail)
                .issuedAt(Date.from(Instant.now()))
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    @Nested
    @DisplayName("Pruebas de Extracción de Claims")
    class ClaimExtractionTests {

        private String validToken;

        @BeforeEach
        void createValidToken() {
            validToken = createToken(
                    Date.from(Instant.now().plus(1, ChronoUnit.HOURS)),
                    Date.from(Instant.now()),
                    userId.toString(),
                    userEmail
            );
        }

        @Test
        @DisplayName("Debe extraer todos los claims de un token válido")
        void extractAllClaims_whenTokenIsValid_shouldReturnClaims() {
            Claims claims = jwtService.extractAllClaims(validToken);
            assertNotNull(claims);
            assertEquals(userId.toString(), claims.getSubject());
            assertEquals(userEmail, claims.get("email", String.class));
        }

        @Test
        @DisplayName("Debe extraer un claim específico usando un resolver")
        void extractClaim_shouldReturnSpecificClaim() {
            String extractedEmail = jwtService.extractClaim(validToken, claims -> claims.get("email", String.class));
            assertEquals(userEmail, extractedEmail);
        }

        @Test
        @DisplayName("Debe extraer el ID de usuario (sub)")
        void extractUserId_shouldReturnCorrectUserId() {
            UUID extractedUserId = jwtService.extractUserId(validToken);
            assertEquals(userId, extractedUserId);
        }

        @Test
        @DisplayName("Debe extraer el nombre de usuario (email)")
        void extractUsername_whenEmailIsPresent_shouldReturnEmail() {
            String username = jwtService.extractUsername(validToken);
            assertEquals(userEmail, username);
        }

        @Test
        @DisplayName("Debe extraer el 'sub' como nombre de usuario si el email está ausente")
        void extractUsername_whenEmailIsAbsent_shouldReturnSubject() {
            String tokenWithoutEmail = createToken(
                Date.from(Instant.now().plus(1, ChronoUnit.HOURS)),
                Date.from(Instant.now()),
                userId.toString(),
                null
            );
            String username = jwtService.extractUsername(tokenWithoutEmail);
            assertEquals(userId.toString(), username);
        }
        
        @Test
        @DisplayName("Debe extraer el 'sub' como nombre de usuario si el email está vacío")
        void extractUsername_whenEmailIsEmpty_shouldReturnSubject() {
            String tokenWithEmptyEmail = createToken(
                Date.from(Instant.now().plus(1, ChronoUnit.HOURS)),
                Date.from(Instant.now()),
                userId.toString(),
                ""
            );
            String username = jwtService.extractUsername(tokenWithEmptyEmail);
            assertEquals(userId.toString(), username);
        }

        @Test
        @DisplayName("Debe extraer la fecha de expiración")
        void extractExpiration_shouldReturnExpirationDate() {
            Date expiration = jwtService.extractExpiration(validToken);
            assertNotNull(expiration);
            assertTrue(expiration.after(new Date()));
        }
    }

    @Nested
    @DisplayName("Pruebas de Validación de Token")
    class TokenValidationTests {

        @Test
        @DisplayName("Debe devolver true para un token válido")
        void validateToken_whenTokenIsValid_shouldReturnTrue() {
            String validToken = createToken(
                Date.from(Instant.now().plus(1, ChronoUnit.HOURS)),
                Date.from(Instant.now()),
                userId.toString(),
                userEmail
            );
            assertTrue(jwtService.validateToken(validToken));
        }

        @Test
        @DisplayName("Debe devolver false y loguear SignatureException para una firma inválida")
        void validateToken_whenSignatureIsInvalid_shouldReturnFalse() {
            SecretKey wrongKey = Keys.hmacShaKeyFor("another-secret-key-that-is-definitely-not-the-right-one-to-use".getBytes(StandardCharsets.UTF_8));
            String tokenWithWrongSignature = createTokenWithCustomKey(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)), wrongKey);
            
            assertThrows(SignatureException.class, () -> jwtService.extractAllClaims(tokenWithWrongSignature));
            assertFalse(jwtService.validateToken(tokenWithWrongSignature));
        }

        @Test
        @DisplayName("Debe devolver false y loguear MalformedJwtException para un token malformado")
        void validateToken_whenTokenIsMalformed_shouldReturnFalse() {
            String malformedToken = "this.is.not.a.jwt";
            assertThrows(MalformedJwtException.class, () -> jwtService.extractAllClaims(malformedToken));
            assertFalse(jwtService.validateToken(malformedToken));
        }

        @Test
        @DisplayName("Debe devolver false y loguear ExpiredJwtException para un token expirado")
        void validateToken_whenTokenIsExpired_shouldReturnFalse() {
            String expiredToken = createToken(
                Date.from(Instant.now().minus(1, ChronoUnit.MINUTES)),
                Date.from(Instant.now().minus(2, ChronoUnit.MINUTES)),
                userId.toString(),
                userEmail
            );
            assertThrows(ExpiredJwtException.class, () -> jwtService.extractAllClaims(expiredToken));
            assertFalse(jwtService.validateToken(expiredToken));
        }

        @Test
        @DisplayName("Debe devolver false y loguear UnsupportedJwtException para un token no soportado")
        void validateToken_whenTokenIsUnsupported_shouldReturnFalse() {
            String unsupportedToken = Jwts.builder().compact(); // JWS sin firma
            assertThrows(UnsupportedJwtException.class, () -> jwtService.extractAllClaims(unsupportedToken));
            assertFalse(jwtService.validateToken(unsupportedToken));
        }

        @Test
        @DisplayName("Debe devolver false y loguear IllegalArgumentException para un token nulo o vacío")
        void validateToken_whenTokenIsNullOrEmpty_shouldReturnFalse() {
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractAllClaims(""));
            assertFalse(jwtService.validateToken(""));
            
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractAllClaims(null));
            assertFalse(jwtService.validateToken(null));
        }
    }
}
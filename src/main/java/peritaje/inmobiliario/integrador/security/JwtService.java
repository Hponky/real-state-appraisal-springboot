package peritaje.inmobiliario.integrador.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${supabase.jwt.secret}")
    private String jwtSecret;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public UUID extractUserId(String token) {
        String sub = extractClaim(token, claims -> claims.get("sub", String.class));
        return UUID.fromString(sub);
    }

    public String extractUsername(String token) {
        // Supabase JWTs often use 'email' as the username, or 'sub' (subject) as the
        // user ID.
        // Prioritize 'email' if available, otherwise fall back to 'sub'.
        Claims claims = extractAllClaims(token);
        String email = claims.get("email", String.class);
        if (email != null && !email.isEmpty()) {
            return email;
        }
        return claims.get("sub", String.class); // Fallback to user ID if email is not present
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // Si el token está expirado, se considera expirado.
            return true;
        } catch (Exception e) {
            // Para cualquier otra excepción durante la extracción de la expiración,
            // se considera que el token no es válido o está malformado.
            System.err.println("JWT Expiration Check Error: " + e.getMessage());
            return true; // O false, dependiendo de la política de seguridad. True para ser más restrictivo.
        }
    }

    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            // Log the exception (e.g., SignatureException, MalformedJwtException,
            // ExpiredJwtException)
            System.err.println("JWT Validation Error: " + e.getMessage());
            return false;
        }
    }
}
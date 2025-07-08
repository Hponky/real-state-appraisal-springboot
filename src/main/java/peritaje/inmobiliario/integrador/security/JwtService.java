package peritaje.inmobiliario.integrador.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

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
        Claims claims = extractAllClaims(token);
        String email = claims.get("email", String.class);
        if (email != null && !email.isEmpty()) {
            return email;
        }
        return claims.get("sub", String.class);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (SignatureException e) {
            logger.error("JWT Validation Error: Invalid signature - {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("JWT Validation Error: Malformed token - {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT Validation Error: Expired token - {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT Validation Error: Unsupported token - {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT Validation Error: Illegal argument or empty token - {}", e.getMessage());
        }
        return false;
    }
}
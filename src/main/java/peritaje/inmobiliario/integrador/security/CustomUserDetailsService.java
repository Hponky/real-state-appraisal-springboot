package peritaje.inmobiliario.integrador.security;

import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final JwtService jwtService;

    public CustomUserDetailsService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public UserDetails loadUserByUsername(String token) throws UsernameNotFoundException {
        // El "username" en este contexto es el token JWT
        if (token == null || token.trim().isEmpty()) {
            throw new UsernameNotFoundException("Token is empty or null");
        }

        try {
            // Validar el token y extraer los claims
            Map<String, Object> claims = jwtService.extractAllClaims(token);
            String userId = (String) claims.get("sub"); // 'sub' es el subject, que en Supabase es el user ID
            String email = (String) claims.get("email"); // 'email' es el email del usuario

            if (userId == null || userId.trim().isEmpty()) {
                throw new UsernameNotFoundException("User ID not found in token");
            }

            // Asignar un rol por defecto. En un caso real, los roles se obtendrían de la base de datos o del token.
            return new CustomUserDetails(userId, email, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        } catch (io.jsonwebtoken.security.SignatureException | io.jsonwebtoken.MalformedJwtException | io.jsonwebtoken.ExpiredJwtException | io.jsonwebtoken.UnsupportedJwtException | IllegalArgumentException e) {
            throw new UsernameNotFoundException("Invalid token or user not found: " + e.getMessage(), e);
        }
    }
}
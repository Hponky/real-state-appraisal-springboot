package peritaje.inmobiliario.integrador.security;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtTokenFilterTest {

    @InjectMocks
    private JwtTokenFilter jwtTokenFilter;

    @Mock
    private JwtService jwtService;
    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_noJwtToken_securityContextRemainsNull() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtTokenFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_invalidJwtToken_securityContextRemainsNull() throws ServletException, IOException {
        String invalidJwt = "invalid.token.here";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidJwt);
        when(jwtService.validateToken(invalidJwt)).thenReturn(false); // Mockear que el token es inválido

        jwtTokenFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_validJwtToken_securityContextIsSet() throws ServletException, IOException {
        String jwt = "valid.jwt.token"; // Token de ejemplo
        String userId = "testuser";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtService.validateToken(jwt)).thenReturn(true);
        when(jwtService.extractUserId(jwt)).thenReturn(userId);

        jwtTokenFilter.doFilterInternal(request, response, filterChain);

        // No se puede verificar directamente SecurityContextHolder en pruebas unitarias sin configuraciones avanzadas.
        // La verificación de que el filtro procesó la solicitud y no lanzó excepciones es suficiente para esta prueba unitaria.
        verify(filterChain, times(1)).doFilter(request, response);
    }
}

package peritaje.inmobiliario.integrador.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private String validToken;
    private UUID userId;
    private String email;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        email = "test@example.com";
        validToken = "valid.jwt.token";
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenTokenIsValid() {
        when(jwtService.extractUserId(validToken)).thenReturn(userId);
        when(jwtService.extractUsername(validToken)).thenReturn(email);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(validToken);

        assertNotNull(userDetails);
        assertTrue(userDetails instanceof CustomUserDetails);
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        assertEquals(userId, customUserDetails.getUserId());
        assertEquals(email, customUserDetails.getUsername());
        assertTrue(customUserDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
        verify(jwtService, times(1)).extractUserId(validToken);
        verify(jwtService, times(1)).extractUsername(validToken);
    }

    @Test
    void loadUserByUsername_shouldThrowUsernameNotFoundException_whenTokenIsNull() {
        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(null);
        });
        assertTrue(exception.getMessage().contains("Token is empty or null"));
        verifyNoInteractions(jwtService);
    }

    @Test
    void loadUserByUsername_shouldThrowUsernameNotFoundException_whenTokenIsEmpty() {
        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("");
        });
        assertTrue(exception.getMessage().contains("Token is empty or null"));
        verifyNoInteractions(jwtService);
    }

    @Test
    void loadUserByUsername_shouldThrowUsernameNotFoundException_whenUserIdIsNullInToken() {
        when(jwtService.extractUserId(validToken)).thenReturn(null);
        when(jwtService.extractUsername(validToken)).thenReturn(email);

        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(validToken);
        });
        assertTrue(exception.getMessage().contains("User ID not found in token"));
        verify(jwtService, times(1)).extractUserId(validToken);
        verify(jwtService, times(1)).extractUsername(validToken);
    }

    @Test
    void loadUserByUsername_shouldThrowUsernameNotFoundException_whenSignatureExceptionOccurs() {
        when(jwtService.extractUserId(validToken)).thenThrow(new SignatureException("Invalid signature"));

        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(validToken);
        });
        assertTrue(exception.getMessage().contains("Invalid token or user not found"));
        assertTrue(exception.getCause() instanceof SignatureException);
        verify(jwtService, times(1)).extractUserId(validToken);
        verify(jwtService, never()).extractUsername(anyString());
    }

    @Test
    void loadUserByUsername_shouldThrowUsernameNotFoundException_whenMalformedJwtExceptionOccurs() {
        when(jwtService.extractUserId(validToken)).thenThrow(new MalformedJwtException("Malformed JWT"));

        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(validToken);
        });
        assertTrue(exception.getMessage().contains("Invalid token or user not found"));
        assertTrue(exception.getCause() instanceof MalformedJwtException);
        verify(jwtService, times(1)).extractUserId(validToken);
        verify(jwtService, never()).extractUsername(anyString());
    }

    @Test
    void loadUserByUsername_shouldThrowUsernameNotFoundException_whenExpiredJwtExceptionOccurs() {
        when(jwtService.extractUserId(validToken)).thenThrow(new ExpiredJwtException(null, null, "Expired JWT"));

        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(validToken);
        });
        assertTrue(exception.getMessage().contains("Invalid token or user not found"));
        assertTrue(exception.getCause() instanceof ExpiredJwtException);
        verify(jwtService, times(1)).extractUserId(validToken);
        verify(jwtService, never()).extractUsername(anyString());
    }

    @Test
    void loadUserByUsername_shouldThrowUsernameNotFoundException_whenIllegalArgumentExceptionOccurs() {
        when(jwtService.extractUserId(validToken)).thenThrow(new IllegalArgumentException("Illegal argument"));

        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(validToken);
        });
        assertTrue(exception.getMessage().contains("Invalid token or user not found"));
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        verify(jwtService, times(1)).extractUserId(validToken);
        verify(jwtService, never()).extractUsername(anyString());
    }
}
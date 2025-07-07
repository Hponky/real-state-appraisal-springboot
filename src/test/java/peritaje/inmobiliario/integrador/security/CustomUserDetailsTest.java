package peritaje.inmobiliario.integrador.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class CustomUserDetailsTest {

    private UUID userId;
    private String username;
    private Collection<? extends GrantedAuthority> authorities;
    private CustomUserDetails customUserDetails;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        username = "test@example.com";
        authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        customUserDetails = new CustomUserDetails(userId, username, authorities);
    }

    @Test
    void getUserId_shouldReturnCorrectUserId() {
        assertEquals(userId, customUserDetails.getUserId());
    }

    @Test
    void getAuthorities_shouldReturnCorrectAuthorities() {
        assertEquals(authorities, customUserDetails.getAuthorities());
    }

    @Test
    void getPassword_shouldReturnNull() {
        assertNull(customUserDetails.getPassword());
    }

    @Test
    void getUsername_shouldReturnCorrectUsername() {
        assertEquals(username, customUserDetails.getUsername());
    }

    @Test
    void isAccountNonExpired_shouldReturnTrue() {
        assertTrue(customUserDetails.isAccountNonExpired());
    }

    @Test
    void isAccountNonLocked_shouldReturnTrue() {
        assertTrue(customUserDetails.isAccountNonLocked());
    }

    @Test
    void isCredentialsNonExpired_shouldReturnTrue() {
        assertTrue(customUserDetails.isCredentialsNonExpired());
    }

    @Test
    void isEnabled_shouldReturnTrue() {
        assertTrue(customUserDetails.isEnabled());
    }
}
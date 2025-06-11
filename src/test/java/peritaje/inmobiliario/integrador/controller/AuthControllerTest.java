package peritaje.inmobiliario.integrador.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import peritaje.inmobiliario.integrador.dto.AuthRequest;
import peritaje.inmobiliario.integrador.dto.AuthResponse;
import peritaje.inmobiliario.integrador.service.SupabaseAuthService;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private SupabaseAuthService supabaseAuthService;

    @InjectMocks
    private AuthController authController;

    private AuthRequest authRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        authRequest = new AuthRequest("test@example.com", "password123");
        authResponse = new AuthResponse("access_token_test", "refresh_token_test", 3600, "bearer", null); // Añadir
                                                                                                          // 'null' para
                                                                                                          // el campo
                                                                                                          // 'user'
    }

    @Test
    void signUp_success() {
        when(supabaseAuthService.signUp(authRequest))
                .thenReturn(Mono.just(authResponse));

        Mono<ResponseEntity<AuthResponse>> responseMono = authController.signUp(authRequest);

        responseMono.subscribe(responseEntity -> {
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            assertEquals(authResponse, responseEntity.getBody());
        });
    }

    @Test
    void signUp_failure() {
        when(supabaseAuthService.signUp(authRequest))
                .thenReturn(Mono.error(new RuntimeException("Registration failed")));

        Mono<ResponseEntity<AuthResponse>> responseMono = authController.signUp(authRequest);

        responseMono.subscribe(
                responseEntity -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode()),
                error -> assertEquals("Supabase signup failed", error.getMessage()));
    }

    @Test
    void signIn_success() {
        when(supabaseAuthService.signIn(authRequest))
                .thenReturn(Mono.just(authResponse));

        Mono<ResponseEntity<AuthResponse>> responseMono = authController.signIn(authRequest);

        responseMono.subscribe(responseEntity -> {
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            assertEquals(authResponse, responseEntity.getBody());
        });
    }

    @Test
    void signIn_failure() {
        when(supabaseAuthService.signIn(authRequest))
                .thenReturn(Mono.error(new RuntimeException("Login failed")));

        Mono<ResponseEntity<AuthResponse>> responseMono = authController.signIn(authRequest);

        responseMono.subscribe(
                responseEntity -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode()),
                error -> assertEquals("Supabase signin failed", error.getMessage()));
    }
}
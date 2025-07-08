package peritaje.inmobiliario.integrador.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import peritaje.inmobiliario.integrador.config.TestSecurityConfig;
import peritaje.inmobiliario.integrador.dto.AuthRequest;
import peritaje.inmobiliario.integrador.dto.AuthResponse;
import peritaje.inmobiliario.integrador.exception.InvalidCredentialsException;
import peritaje.inmobiliario.integrador.exception.SupabaseIntegrationException;
import peritaje.inmobiliario.integrador.exception.UserAlreadyExistsException;
import peritaje.inmobiliario.integrador.service.ISupabaseAuthService;
import peritaje.inmobiliario.integrador.security.JwtTokenFilter;
import reactor.core.publisher.Mono;

@WebFluxTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ISupabaseAuthService supabaseAuthService;

    @MockBean
    private JwtTokenFilter jwtTokenFilter;

    private AuthRequest validAuthRequest = new AuthRequest("test@example.com", "password123");
    private AuthRequest invalidAuthRequest = new AuthRequest("invalid@example.com", "wrongpassword");
    private AuthResponse authResponse = new AuthResponse("access_token_test", "bearer", 3600, "refresh_token_test", new AuthResponse.User("user-id-123", "test@example.com"));

    @BeforeEach
    void setUp() {
        // El setup ahora es manejado por las anotaciones de Spring Boot
    }

    @Test
    void signUp_success() {
        when(supabaseAuthService.signUp(any(AuthRequest.class))).thenReturn(Mono.just(authResponse));

        webTestClient
                .post().uri("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validAuthRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .value(response -> {
                    assert response.getAccessToken().equals(authResponse.getAccessToken());
                });
    }

    @Test
    void signUp_userAlreadyExists() {
        String errorMessage = "El usuario ya existe.";
        when(supabaseAuthService.signUp(any(AuthRequest.class))).thenReturn(Mono.error(new UserAlreadyExistsException(errorMessage)));

        webTestClient
                .post().uri("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validAuthRequest)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody()
                .jsonPath("$.message").isEqualTo(errorMessage);
    }

    @Test
    void signIn_success() {
        when(supabaseAuthService.signIn(any(AuthRequest.class))).thenReturn(Mono.just(authResponse));

        webTestClient
                .post().uri("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validAuthRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .value(response -> {
                    assert response.getAccessToken().equals(authResponse.getAccessToken());
                });
    }

    @Test
    void signIn_invalidCredentials() {
        String errorMessage = "Credenciales de autenticacion invalidas.";
        when(supabaseAuthService.signIn(any(AuthRequest.class))).thenReturn(Mono.error(new InvalidCredentialsException(errorMessage)));

        webTestClient
                .post().uri("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidAuthRequest)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.message").isEqualTo(errorMessage);
    }
    
    @Test
    void signIn_supabaseIntegrationFailure() {
        String errorMessage = "Error de integración con el servicio externo. Intente de nuevo más tarde.";
        when(supabaseAuthService.signIn(any(AuthRequest.class))).thenReturn(Mono.error(new SupabaseIntegrationException("Supabase signin failed")));

        webTestClient
                .post().uri("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validAuthRequest)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody()
                .jsonPath("$.message").isEqualTo(errorMessage);
    }
}
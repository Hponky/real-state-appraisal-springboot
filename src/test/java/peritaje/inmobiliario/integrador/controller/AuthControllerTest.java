package peritaje.inmobiliario.integrador.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 import peritaje.inmobiliario.integrador.dto.AuthRequest;
 import peritaje.inmobiliario.integrador.dto.AuthResponse;
 import peritaje.inmobiliario.integrador.exception.GlobalExceptionHandler;
 import peritaje.inmobiliario.integrador.exception.InvalidCredentialsException;
 import peritaje.inmobiliario.integrador.service.ISupabaseAuthService;
 import reactor.core.publisher.Mono;
 
 @ExtendWith(MockitoExtension.class)
 class AuthControllerTest {
 
     private WebTestClient webTestClient;
 
     @Mock
     private ISupabaseAuthService supabaseAuthService;
 
     @InjectMocks
     private AuthController authController;
 
     private ObjectMapper objectMapper;
 
     private AuthRequest authRequest;
     private AuthResponse authResponse;
 
     @BeforeEach
     void setUp() {
         webTestClient = WebTestClient.bindToController(authController)
                 .controllerAdvice(new GlobalExceptionHandler())
                 .build();
         objectMapper = new ObjectMapper();
         authRequest = new AuthRequest("test@example.com", "password123");
         authResponse = new AuthResponse("access_token_test", "refresh_token_test", 3600, "bearer", null);
     }
 
     @Test
     void signUp_success() throws Exception {
         when(supabaseAuthService.signUp(any(AuthRequest.class))).thenReturn(Mono.just(authResponse));
 
         webTestClient.post().uri("/api/public/auth/signup")
                 .contentType(MediaType.APPLICATION_JSON)
                 .bodyValue(authRequest)
                 .exchange()
                 .expectStatus().isOk()
                 .expectBody(AuthResponse.class)
                 .value(response -> {
                     assertEquals(authResponse.getAccessToken(), response.getAccessToken());
                     assertEquals(authResponse.getRefreshToken(), response.getRefreshToken());
                 });
     }
 
     @Test
     void signIn_invalidCredentials() throws Exception {
         when(supabaseAuthService.signIn(any(AuthRequest.class)))
                 .thenReturn(Mono.error(new InvalidCredentialsException("Invalid email or password")));
 
         webTestClient.post().uri("/api/public/auth/signin")
                 .contentType(MediaType.APPLICATION_JSON)
                 .bodyValue(authRequest)
                 .exchange()
                 .expectStatus().isUnauthorized()
                 .expectBody()
                 .jsonPath("$.message").isEqualTo("Invalid Credentials");
     }
 }

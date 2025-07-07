package peritaje.inmobiliario.integrador.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import peritaje.inmobiliario.integrador.config.TestSecurityConfig;
import peritaje.inmobiliario.integrador.dto.AuthRequest;
import peritaje.inmobiliario.integrador.dto.AuthResponse;
import peritaje.inmobiliario.integrador.exception.InvalidCredentialsException;
import peritaje.inmobiliario.integrador.exception.SupabaseIntegrationException;
import peritaje.inmobiliario.integrador.exception.UserAlreadyExistsException;
import peritaje.inmobiliario.integrador.service.ISupabaseAuthService;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ISupabaseAuthService supabaseAuthService;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthRequest validAuthRequest;
    private AuthRequest invalidAuthRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        validAuthRequest = new AuthRequest("test@example.com", "password123");
        invalidAuthRequest = new AuthRequest("invalid@example.com", "wrongpassword");
        authResponse = new AuthResponse("access_token_test", "bearer", 3600, "refresh_token_test", new AuthResponse.User("user-id-123", "test@example.com"));
    }

    @Test
    void signUp_success() throws Exception {
        when(supabaseAuthService.signUp(any(AuthRequest.class))).thenReturn(Mono.just(authResponse));

        mockMvc.perform(post("/api/public/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAuthRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value(authResponse.getAccessToken()));
    }

    @Test
    void signUp_userAlreadyExists() throws Exception {
        when(supabaseAuthService.signUp(any(AuthRequest.class))).thenThrow(new UserAlreadyExistsException("Email already registered"));

        mockMvc.perform(post("/api/public/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAuthRequest))
                .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    void signIn_success() throws Exception {
        when(supabaseAuthService.signIn(any(AuthRequest.class))).thenReturn(Mono.just(authResponse));

        mockMvc.perform(post("/api/public/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAuthRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value(authResponse.getAccessToken()));
    }

    @Test
    void signIn_invalidCredentials() throws Exception {
        when(supabaseAuthService.signIn(any(AuthRequest.class))).thenThrow(new InvalidCredentialsException("Credenciales inválidas"));

        mockMvc.perform(post("/api/public/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidAuthRequest))
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void signIn_supabaseIntegrationFailure() throws Exception {
        when(supabaseAuthService.signIn(any(AuthRequest.class))).thenThrow(new SupabaseIntegrationException("Supabase signin failed"));

        mockMvc.perform(post("/api/public/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAuthRequest))
                .with(csrf()))
                .andExpect(status().isServiceUnavailable());
    }
}
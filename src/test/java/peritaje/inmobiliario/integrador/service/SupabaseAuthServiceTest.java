package peritaje.inmobiliario.integrador.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import peritaje.inmobiliario.integrador.dto.AuthRequest;
import peritaje.inmobiliario.integrador.dto.AuthResponse;
import peritaje.inmobiliario.integrador.dto.ErrorResponse;
import peritaje.inmobiliario.integrador.exception.InvalidCredentialsException;
import peritaje.inmobiliario.integrador.exception.SupabaseIntegrationException;
import peritaje.inmobiliario.integrador.exception.UserAlreadyExistsException;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class SupabaseAuthServiceTest {

    private MockWebServer mockBackEnd;
    private SupabaseAuthService supabaseAuthService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private AuthRequest authRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void initialize() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
        
        String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
        supabaseAuthService = new SupabaseAuthService(baseUrl, "test-key");
        
        authRequest = new AuthRequest("test@example.com", "password123");
        authResponse = new AuthResponse("access_token", "refresh_token", 3600, "bearer", null);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    // --- Sign Up Tests ---

    @Test
    void signUp_success() throws JsonProcessingException, InterruptedException {
        mockBackEnd.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(authResponse))
            .addHeader(HttpHeaders.CONTENT_TYPE, "application/json"));

        StepVerifier.create(supabaseAuthService.signUp(authRequest))
            .expectNextMatches(response -> "access_token".equals(response.getAccessToken()))
            .verifyComplete();

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/auth/v1/signup", recordedRequest.getPath());
    }

    @Test
    void signUp_userAlreadyExists() throws JsonProcessingException {
        ErrorResponse errorDto = new ErrorResponse();
        errorDto.setMessage("User already registered");
        String errorBody = objectMapper.writeValueAsString(errorDto);

        mockBackEnd.enqueue(new MockResponse()
            .setResponseCode(HttpStatus.BAD_REQUEST.value())
            .setBody(errorBody)
            .addHeader(HttpHeaders.CONTENT_TYPE, "application/json"));

        StepVerifier.create(supabaseAuthService.signUp(authRequest))
            .expectError(UserAlreadyExistsException.class)
            .verify();
    }

    // --- Sign In Tests ---

    @Test
    void signIn_success() throws JsonProcessingException, InterruptedException {
        mockBackEnd.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(authResponse))
            .addHeader(HttpHeaders.CONTENT_TYPE, "application/json"));

        StepVerifier.create(supabaseAuthService.signIn(authRequest))
            .expectNextMatches(response -> "access_token".equals(response.getAccessToken()))
            .verifyComplete();
        
        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/auth/v1/token?grant_type=password", recordedRequest.getPath());
    }

    @Test
    void signIn_invalidCredentials() throws JsonProcessingException {
        ErrorResponse errorDto = new ErrorResponse();
        errorDto.setError("invalid_grant");
        String errorBody = objectMapper.writeValueAsString(errorDto);

        mockBackEnd.enqueue(new MockResponse()
            .setResponseCode(HttpStatus.UNAUTHORIZED.value())
            .setBody(errorBody)
            .addHeader(HttpHeaders.CONTENT_TYPE, "application/json"));

        StepVerifier.create(supabaseAuthService.signIn(authRequest))
            .expectError(InvalidCredentialsException.class)
            .verify();
    }

    // --- Sign Out Tests ---

    @Test
    void signOut_success() throws InterruptedException {
        mockBackEnd.enqueue(new MockResponse().setResponseCode(HttpStatus.NO_CONTENT.value()));

        StepVerifier.create(supabaseAuthService.signOut("test_token"))
            .verifyComplete();

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/auth/v1/logout", recordedRequest.getPath());
        assertEquals("Bearer test_token", recordedRequest.getHeader("Authorization"));
    }

    @Test
    void signOut_failure() {
        mockBackEnd.enqueue(new MockResponse().setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value()));

        StepVerifier.create(supabaseAuthService.signOut("test_token"))
            .expectError(SupabaseIntegrationException.class)
            .verify();
    }
    
    // --- Generic Error Handling Test ---
    
    @Test
    void handleSupabaseError_unparseableError() {
        mockBackEnd.enqueue(new MockResponse()
            .setResponseCode(HttpStatus.BAD_REQUEST.value())
            .setBody("This is not valid JSON")
            .addHeader(HttpHeaders.CONTENT_TYPE, "application/json"));

        StepVerifier.create(supabaseAuthService.signUp(authRequest))
            .expectErrorMatches(error -> error instanceof SupabaseIntegrationException 
                && error.getMessage().contains("response could not be parsed"))
            .verify();
    }

    @Test
    void handleSupabaseError_genericApiError() throws JsonProcessingException {
        ErrorResponse errorDto = new ErrorResponse();
        errorDto.setError("some_other_error");
        errorDto.setMessage("Something else went wrong.");
        String errorBody = objectMapper.writeValueAsString(errorDto);

        mockBackEnd.enqueue(new MockResponse()
            .setResponseCode(HttpStatus.BAD_REQUEST.value())
            .setBody(errorBody)
            .addHeader(HttpHeaders.CONTENT_TYPE, "application/json"));

        StepVerifier.create(supabaseAuthService.signUp(authRequest))
            .expectErrorMatches(error -> error instanceof SupabaseIntegrationException
                && error.getMessage().contains("Supabase operation failed:"))
            .verify();
    }
}

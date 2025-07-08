package peritaje.inmobiliario.integrador.service;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import peritaje.inmobiliario.integrador.dto.AuthRequest;
import peritaje.inmobiliario.integrador.dto.AuthResponse;
import peritaje.inmobiliario.integrador.dto.ErrorResponse;
import peritaje.inmobiliario.integrador.exception.InvalidCredentialsException;
import peritaje.inmobiliario.integrador.exception.SupabaseIntegrationException;
import peritaje.inmobiliario.integrador.exception.UserAlreadyExistsException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class SupabaseAuthServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private SupabaseAuthService supabaseAuthService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private AuthRequest authRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void initialize() {
        supabaseAuthService = new SupabaseAuthService(webClient);
        authRequest = new AuthRequest("test@example.com", "password123");
        authResponse = new AuthResponse("access_token", "refresh_token", 3600, "bearer", null);
    }

    private void setupWebClientMocksForBody() {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }
    
    private void setupWebClientMocksForSignOut() {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.header(any(), any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    }

    // --- Sign Up Tests ---

    @Test
    void signUp_success() {
        setupWebClientMocksForBody();
        when(responseSpec.bodyToMono(AuthResponse.class)).thenReturn(Mono.just(authResponse));

        StepVerifier.create(supabaseAuthService.signUp(authRequest))
            .expectNextMatches(response -> "access_token".equals(response.getAccessToken()))
            .verifyComplete();
    }

    @Test
    void signUp_userAlreadyExists() throws JsonProcessingException {
        setupWebClientMocksForBody();
        ErrorResponse errorDto = new ErrorResponse();
        errorDto.setMessage("User already registered");
        String errorBody = objectMapper.writeValueAsString(errorDto);
        WebClientResponseException exception = WebClientResponseException.create(400, "Bad Request", null, errorBody.getBytes(), null);

        when(responseSpec.bodyToMono(AuthResponse.class)).thenReturn(Mono.error(exception));

        StepVerifier.create(supabaseAuthService.signUp(authRequest))
            .expectError(UserAlreadyExistsException.class)
            .verify();
    }

    // --- Sign In Tests ---

    @Test
    void signIn_success() {
        setupWebClientMocksForBody();
        when(responseSpec.bodyToMono(AuthResponse.class)).thenReturn(Mono.just(authResponse));

        StepVerifier.create(supabaseAuthService.signIn(authRequest))
            .expectNextMatches(response -> "access_token".equals(response.getAccessToken()))
            .verifyComplete();
    }

    @Test
    void signIn_invalidCredentials() throws JsonProcessingException {
        setupWebClientMocksForBody();
        ErrorResponse errorDto = new ErrorResponse();
        errorDto.setError("invalid_grant");
        String errorBody = objectMapper.writeValueAsString(errorDto);
        WebClientResponseException exception = WebClientResponseException.create(401, "Unauthorized", null, errorBody.getBytes(), null);

        when(responseSpec.bodyToMono(AuthResponse.class)).thenReturn(Mono.error(exception));

        StepVerifier.create(supabaseAuthService.signIn(authRequest))
            .expectError(InvalidCredentialsException.class)
            .verify();
    }

    // --- Sign Out Tests ---

    @Test
    void signOut_success() {
        setupWebClientMocksForSignOut();
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        StepVerifier.create(supabaseAuthService.signOut("test_token"))
            .verifyComplete();
    }

    @Test
    void signOut_failure() {
        setupWebClientMocksForSignOut();
        WebClientResponseException exception = WebClientResponseException.create(500, "Internal Server Error", null, null, null);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.error(exception));

        StepVerifier.create(supabaseAuthService.signOut("test_token"))
            .expectError(SupabaseIntegrationException.class)
            .verify();
    }
    
    // --- Generic Error Handling Tests ---
    
    @Test
    void handleSupabaseError_unparseableError() {
        setupWebClientMocksForBody();
        WebClientResponseException exception = WebClientResponseException.create(400, "Bad Request", null, "This is not valid JSON".getBytes(), null);
        when(responseSpec.bodyToMono(AuthResponse.class)).thenReturn(Mono.error(exception));

        StepVerifier.create(supabaseAuthService.signUp(authRequest))
            .expectErrorMatches(error -> error instanceof SupabaseIntegrationException 
                && error.getMessage().contains("response could not be parsed"))
            .verify();
    }

    @Test
    void handleSupabaseError_genericApiError() throws JsonProcessingException {
        setupWebClientMocksForBody();
        ErrorResponse errorDto = new ErrorResponse();
        errorDto.setError("some_other_error");
        errorDto.setMessage("Something else went wrong.");
        String errorBody = objectMapper.writeValueAsString(errorDto);
        WebClientResponseException exception = WebClientResponseException.create(400, "Bad Request", null, errorBody.getBytes(), null);

        when(responseSpec.bodyToMono(AuthResponse.class)).thenReturn(Mono.error(exception));

        StepVerifier.create(supabaseAuthService.signUp(authRequest))
            .expectErrorMatches(error -> error instanceof SupabaseIntegrationException
                && error.getMessage().contains("Supabase operation failed:"))
            .verify();
    }

    @Test
    void handleSupabaseError_nonWebClientException() {
        setupWebClientMocksForBody();
        when(responseSpec.bodyToMono(AuthResponse.class)).thenReturn(Mono.error(new IOException("Network Error")));

        StepVerifier.create(supabaseAuthService.signUp(authRequest))
            .expectErrorMatches(error -> error instanceof SupabaseIntegrationException
                && error.getMessage().contains("An unexpected error occurred")
                && error.getCause() instanceof IOException)
            .verify();
    }

    @Test
    void handleSupabaseError_withNullMessage() throws JsonProcessingException {
        setupWebClientMocksForBody();
        ErrorResponse errorDto = new ErrorResponse();
        errorDto.setError("some_error_without_message");
        String errorBody = objectMapper.writeValueAsString(errorDto);
        WebClientResponseException exception = WebClientResponseException.create(400, "Bad Request", null, errorBody.getBytes(), null);

        when(responseSpec.bodyToMono(AuthResponse.class)).thenReturn(Mono.error(exception));

        StepVerifier.create(supabaseAuthService.signUp(authRequest))
            .expectErrorMatches(error -> error instanceof SupabaseIntegrationException
                && error.getMessage().contains("Supabase operation failed:"))
            .verify();
    }
}

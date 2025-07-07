package peritaje.inmobiliario.integrador.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;

import peritaje.inmobiliario.integrador.dto.AuthRequest;
import peritaje.inmobiliario.integrador.dto.AuthResponse;
import peritaje.inmobiliario.integrador.dto.ErrorResponse;
import peritaje.inmobiliario.integrador.exception.InvalidCredentialsException;
import peritaje.inmobiliario.integrador.exception.SupabaseIntegrationException;
import peritaje.inmobiliario.integrador.exception.UserAlreadyExistsException;
import reactor.core.publisher.Mono;

@Service
public class SupabaseAuthService implements ISupabaseAuthService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public SupabaseAuthService(@Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.service-key}") String supabaseServiceKey) {
        this.objectMapper = new ObjectMapper();
        this.webClient = WebClient.builder().baseUrl(supabaseUrl)
                .defaultHeader("apikey", supabaseServiceKey)
                .defaultHeader("Authorization", "Bearer " + supabaseServiceKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public Mono<AuthResponse> signUp(AuthRequest authRequest) {
        return webClient.post()
                .uri("/auth/v1/signup")
                .bodyValue(authRequest)
                .retrieve()
                .bodyToMono(AuthResponse.class)
                .onErrorMap(this::handleSupabaseError);
    }

    @Override
    public Mono<AuthResponse> signIn(AuthRequest authRequest) {
        return webClient.post()
                .uri("/auth/v1/token?grant_type=password")
                .bodyValue(authRequest)
                .retrieve()
                .bodyToMono(AuthResponse.class)
                .onErrorMap(this::handleSupabaseError);
    }

    @Override
    public Mono<Void> signOut(String accessToken) {
        return webClient.post()
                .uri("/auth/v1/logout")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorMap(this::handleSupabaseError);
    }

    private Throwable handleSupabaseError(Throwable error) {
        if (!(error instanceof WebClientResponseException)) {
            return new SupabaseIntegrationException("An unexpected error occurred", error);
        }

        WebClientResponseException ex = (WebClientResponseException) error;
        String errorBody = ex.getResponseBodyAsString();

        try {
            ErrorResponse errorResponse = objectMapper.readValue(errorBody, ErrorResponse.class);
            
            if (errorResponse.getMessage() != null && errorResponse.getMessage().contains("User already registered")) {
                return new UserAlreadyExistsException("User already registered", ex);
            }
            
            if (errorResponse.getError() != null && errorResponse.getError().contains("invalid_grant")) {
                return new InvalidCredentialsException("Invalid login credentials", ex);
            }

            return new SupabaseIntegrationException("Supabase operation failed: " + errorBody, ex);
        } catch (Exception parseException) {
            return new SupabaseIntegrationException("Supabase operation failed and response could not be parsed: " + errorBody, ex);
        }
    }
}
package peritaje.inmobiliario.integrador.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import peritaje.inmobiliario.integrador.dto.AuthRequest;
import peritaje.inmobiliario.integrador.dto.AuthResponse;
import peritaje.inmobiliario.integrador.dto.ErrorResponse;
import reactor.core.publisher.Mono;

@Service
public class SupabaseAuthService {

    private final WebClient webClient;

    public SupabaseAuthService(@Value("${supabase.url}") String supabaseUrl,
                               @Value("${supabase.service-key}") String supabaseServiceKey,
                               WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(supabaseUrl)
                .defaultHeader("apikey", supabaseServiceKey)
                .defaultHeader("Authorization", "Bearer " + supabaseServiceKey)
                .build();
    }

    public Mono<AuthResponse> signUp(AuthRequest authRequest) {
        return webClient.post()
                .uri("/auth/v1/signup")
                .bodyValue(authRequest)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response ->
                        response.bodyToMono(ErrorResponse.class)
                                .flatMap(error -> Mono.error(new RuntimeException(error.getMessage() != null ? error.getMessage() : error.getError())))
                )
                .bodyToMono(AuthResponse.class);
    }

    public Mono<AuthResponse> signIn(AuthRequest authRequest) {
        return webClient.post()
                .uri("/auth/v1/token?grant_type=password")
                .bodyValue(authRequest)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response ->
                        response.bodyToMono(ErrorResponse.class)
                                .flatMap(error -> Mono.error(new RuntimeException(error.getMessage() != null ? error.getMessage() : error.getError())))
                )
                .bodyToMono(AuthResponse.class);
    }

    public Mono<Void> signOut(String accessToken) {
        return webClient.post()
                .uri("/auth/v1/logout")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response ->
                        response.bodyToMono(ErrorResponse.class)
                                .flatMap(error -> Mono.error(new RuntimeException(error.getMessage() != null ? error.getMessage() : error.getError())))
                )
                .bodyToMono(Void.class);
    }
}
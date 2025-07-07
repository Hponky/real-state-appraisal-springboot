package peritaje.inmobiliario.integrador.service;

import peritaje.inmobiliario.integrador.dto.AuthRequest;
import peritaje.inmobiliario.integrador.dto.AuthResponse;
import reactor.core.publisher.Mono;

public interface ISupabaseAuthService {
    Mono<AuthResponse> signUp(AuthRequest authRequest);

    Mono<AuthResponse> signIn(AuthRequest authRequest);

    Mono<Void> signOut(String accessToken);
}
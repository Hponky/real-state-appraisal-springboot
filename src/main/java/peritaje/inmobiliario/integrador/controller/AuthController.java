package peritaje.inmobiliario.integrador.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import peritaje.inmobiliario.integrador.dto.AuthRequest;
import peritaje.inmobiliario.integrador.dto.AuthResponse;
import peritaje.inmobiliario.integrador.service.SupabaseAuthService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/public/auth")
public class AuthController {

    private final SupabaseAuthService supabaseAuthService;

    public AuthController(SupabaseAuthService supabaseAuthService) {
        this.supabaseAuthService = supabaseAuthService;
    }

    @PostMapping("/signup")
    public Mono<ResponseEntity<AuthResponse>> signUp(@Valid @RequestBody AuthRequest authRequest) {
        return supabaseAuthService.signUp(authRequest)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    if (e instanceof RuntimeException && e.getMessage() != null
                            && e.getMessage().contains("Email already registered")) {
                        return Mono.error(new RuntimeException("Email already registered", e));
                    }
                    return Mono.error(new RuntimeException("Supabase signup failed", e));
                });
    }

    @PostMapping("/signin")
    public Mono<ResponseEntity<AuthResponse>> signIn(@Valid @RequestBody AuthRequest authRequest) {
        System.out.println("Received sign-in request for email: " + authRequest.getEmail());
        return supabaseAuthService.signIn(authRequest)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    if (e instanceof RuntimeException && e.getMessage() != null
                            && e.getMessage().contains("Invalid login credentials")) {
                        return Mono.error(new RuntimeException("Invalid login credentials", e));
                    }
                    return Mono.error(new RuntimeException("Supabase signin failed", e));
                });
    }
}
package peritaje.inmobiliario.integrador.controller;

import org.springframework.http.HttpStatus;
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
    public Mono<ResponseEntity<AuthResponse>> signUp(@RequestBody AuthRequest authRequest) {
        if (authRequest.getEmail() == null || authRequest.getPassword() == null) {
            return Mono.just(ResponseEntity.badRequest().body(null));
        }

        return supabaseAuthService.signUp(authRequest)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
                    String errorMessage = "Supabase signup failed: " + e.getMessage();
                    if (e instanceof RuntimeException && e.getMessage() != null && e.getMessage().contains("Email already registered")) {
                        status = HttpStatus.CONFLICT;
                        errorMessage = "Email already registered";
                    }
                    return Mono.just(ResponseEntity.status(status).body(null));
                });
    }

    @PostMapping("/signin")
    public Mono<ResponseEntity<AuthResponse>> signIn(@RequestBody AuthRequest authRequest) {
        if (authRequest.getEmail() == null || authRequest.getPassword() == null) {
            return Mono.just(ResponseEntity.badRequest().body(null));
        }

        return supabaseAuthService.signIn(authRequest)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
                    String errorMessage = "Supabase signin failed: " + e.getMessage();
                    if (e instanceof RuntimeException && e.getMessage() != null && e.getMessage().contains("Invalid login credentials")) {
                        status = HttpStatus.UNAUTHORIZED;
                        errorMessage = "Invalid login credentials";
                    }
                    return Mono.just(ResponseEntity.status(status).body(null));
                });
    }
}
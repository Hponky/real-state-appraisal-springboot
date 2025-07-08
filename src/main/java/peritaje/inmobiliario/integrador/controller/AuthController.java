package peritaje.inmobiliario.integrador.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import peritaje.inmobiliario.integrador.dto.AuthRequest;
import peritaje.inmobiliario.integrador.dto.AuthResponse;
import peritaje.inmobiliario.integrador.service.ISupabaseAuthService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ISupabaseAuthService supabaseAuthService;

    public AuthController(ISupabaseAuthService supabaseAuthService) {
        this.supabaseAuthService = supabaseAuthService;
    }

    @PostMapping("/signup")
    public Mono<AuthResponse> signUp(@Valid @RequestBody AuthRequest authRequest) {
        return supabaseAuthService.signUp(authRequest);
    }

    @PostMapping("/signin")
    public Mono<AuthResponse> signIn(@Valid @RequestBody AuthRequest authRequest) {
        return supabaseAuthService.signIn(authRequest);
    }
}

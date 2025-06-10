package peritaje.inmobiliario.integrador.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import peritaje.inmobiliario.integrador.security.JwtTokenFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;

    public SecurityConfig(JwtTokenFilter jwtTokenFilter) {
        this.jwtTokenFilter = jwtTokenFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF para APIs REST
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/public/**").permitAll() // Permitir acceso público a ciertos endpoints
                .anyRequest().authenticated() // Requerir autenticación para cualquier otra solicitud
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // No usar sesiones basadas en estado
            )
            .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class); // Añadir el filtro JWT

        return http.build();
    }
}
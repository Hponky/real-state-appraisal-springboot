package peritaje.inmobiliario.integrador.security;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenFilter.class);

    private final JwtService jwtService;

    @Autowired
    public JwtTokenFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String requestURI = request.getRequestURI();

        logger.info("Processing request for URI: {}", requestURI);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("No JWT token found or invalid format for URI: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix
        logger.info("JWT token found for URI: {}", requestURI);

        if (jwtService.validateToken(token)) {
            UUID userId = jwtService.extractUserId(token);
            String username = jwtService.extractUsername(token);
            List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
            CustomUserDetails userDetails = new CustomUserDetails(userId, username, authorities);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails,
                    null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("Successfully authenticated user: {} (ID: {}) for URI: {}", username, userId, requestURI);
        } else {
            logger.warn("Invalid JWT token for URI: {}", requestURI);
        }

        filterChain.doFilter(request, response);
    }
}

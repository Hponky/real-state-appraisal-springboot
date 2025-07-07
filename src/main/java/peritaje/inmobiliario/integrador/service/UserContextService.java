package peritaje.inmobiliario.integrador.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import peritaje.inmobiliario.integrador.security.CustomUserDetails;
import peritaje.inmobiliario.integrador.exception.ResourceNotFoundException;

@Service
public class UserContextService implements IUserContextService {

    @Override
    public CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new ResourceNotFoundException("No authenticated user found in context.");
        }
        return (CustomUserDetails) authentication.getPrincipal();
    }

    @Override
    public UUID getCurrentUserId() {
        return getCurrentUserDetails().getUserId();
    }

    @Override
    public Optional<UUID> getCurrentUserIdOptional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return Optional.empty();
        }
        return Optional.of(((CustomUserDetails) authentication.getPrincipal()).getUserId());
    }
}
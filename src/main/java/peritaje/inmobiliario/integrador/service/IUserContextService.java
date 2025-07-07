package peritaje.inmobiliario.integrador.service;

import java.util.Optional;
import java.util.UUID;
import peritaje.inmobiliario.integrador.security.CustomUserDetails;

public interface IUserContextService {
    CustomUserDetails getCurrentUserDetails();

    UUID getCurrentUserId();
    Optional<UUID> getCurrentUserIdOptional();
}
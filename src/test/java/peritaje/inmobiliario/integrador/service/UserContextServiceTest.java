package peritaje.inmobiliario.integrador.service;

import java.util.Optional;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import peritaje.inmobiliario.integrador.exception.ResourceNotFoundException;
import peritaje.inmobiliario.integrador.security.CustomUserDetails;

@ExtendWith(MockitoExtension.class)
class UserContextServiceTest {

    @InjectMocks
    private UserContextService userContextService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    private UUID testUserId;
    private CustomUserDetails customUserDetails;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        customUserDetails = new CustomUserDetails(testUserId, "test@example.com", Collections.emptyList());
    }

    @Test
    void getCurrentUserDetails_authenticatedUser_returnsUserDetails() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn(customUserDetails);

            CustomUserDetails result = userContextService.getCurrentUserDetails();

            assertNotNull(result);
            assertEquals(testUserId, result.getUserId());
            assertEquals("test@example.com", result.getUsername());
        }
    }

    @Test
    void getCurrentUserDetails_noAuthentication_throwsResourceNotFoundException() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> userContextService.getCurrentUserDetails());

            assertEquals("No authenticated user found in context.", exception.getMessage());
        }
    }

    @Test
    void getCurrentUserDetails_notAuthenticated_throwsResourceNotFoundException() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(false);

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> userContextService.getCurrentUserDetails());

            assertEquals("No authenticated user found in context.", exception.getMessage());
        }
    }

    @Test
    void getCurrentUserDetails_principalNotCustomUserDetails_throwsResourceNotFoundException() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn("someOtherPrincipal"); // No es CustomUserDetails

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> userContextService.getCurrentUserDetails());

            assertEquals("No authenticated user found in context.", exception.getMessage());
        }
    }

    @Test
    void getCurrentUserId_authenticatedUser_returnsUserId() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn(customUserDetails);

            UUID result = userContextService.getCurrentUserId();

            assertNotNull(result);
            assertEquals(testUserId, result);
        }
    }

    @Test
    void getCurrentUserId_noAuthenticatedUser_throwsResourceNotFoundException() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null); // Simula no autenticado

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> userContextService.getCurrentUserId());

            assertEquals("No authenticated user found in context.", exception.getMessage());
        }
    }

    @Test
    void getCurrentUserIdOptional_authenticatedUser_returnsOptionalOfUserId() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn(customUserDetails);

            Optional<UUID> result = userContextService.getCurrentUserIdOptional();

            assertTrue(result.isPresent());
            assertEquals(testUserId, result.get());
        }
    }

    @Test
    void getCurrentUserIdOptional_noAuthenticatedUser_returnsEmptyOptional() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null); // Simula no autenticado

            Optional<UUID> result = userContextService.getCurrentUserIdOptional();

            assertFalse(result.isPresent());
        }
    }

    @Test
    void getCurrentUserIdOptional_notAuthenticated_returnsEmptyOptional() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(false); // Simula no autenticado

            Optional<UUID> result = userContextService.getCurrentUserIdOptional();

            assertFalse(result.isPresent());
        }
    }

    @Test
    void getCurrentUserIdOptional_principalNotCustomUserDetails_returnsEmptyOptional() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn("someOtherPrincipal"); // No es CustomUserDetails

            Optional<UUID> result = userContextService.getCurrentUserIdOptional();

            assertFalse(result.isPresent());
        }
    }
}
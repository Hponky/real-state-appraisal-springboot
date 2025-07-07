package peritaje.inmobiliario.integrador.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import peritaje.inmobiliario.integrador.domain.Appraisal;
import peritaje.inmobiliario.integrador.dto.AppraisalDetailsDTO;
import peritaje.inmobiliario.integrador.exception.ResourceNotFoundException;
import peritaje.inmobiliario.integrador.repository.AppraisalRepository;

@ExtendWith(MockitoExtension.class)
public class AppraisalServiceTest {

    @Mock
    private AppraisalRepository appraisalRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private IUserContextService userContextService;

    @InjectMocks
    private AppraisalService appraisalService;

    private UUID userId;
    private Appraisal appraisal;
    private UUID appraisalId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        appraisalId = UUID.randomUUID();
        appraisal = new Appraisal();
        appraisal.setId(appraisalId);
        appraisal.setUserId(userId);
        appraisal.setStatus("PENDING");
        appraisal.setResultData(new HashMap<>());
    }

    @Test
    void getAppraisalsForCurrentUser_shouldReturnAppraisals_whenUserHasAppraisals() {
        when(userContextService.getCurrentUserId()).thenReturn(userId);
        List<Appraisal> appraisals = new ArrayList<>();
        appraisals.add(appraisal);
        when(appraisalRepository.findByUserId(userId)).thenReturn(appraisals);

        List<Appraisal> result = appraisalService.getAppraisalsForCurrentUser();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(appraisalId, result.get(0).getId());
        verify(userContextService, times(1)).getCurrentUserId();
        verify(appraisalRepository, times(1)).findByUserId(userId);
    }

    @Test
    void getAppraisalsForCurrentUser_shouldReturnEmptyList_whenUserHasNoAppraisals() {
        when(userContextService.getCurrentUserId()).thenReturn(userId);
        when(appraisalRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        List<Appraisal> result = appraisalService.getAppraisalsForCurrentUser();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userContextService, times(1)).getCurrentUserId();
        verify(appraisalRepository, times(1)).findByUserId(userId);
    }

    @Test
    void getAppraisalById_shouldReturnAppraisal_whenIdIsValid() {
        when(appraisalRepository.findById(appraisalId)).thenReturn(Optional.of(appraisal));

        Appraisal result = appraisalService.getAppraisalById(appraisalId.toString());

        assertNotNull(result);
        assertEquals(appraisalId, result.getId());
        verify(appraisalRepository, times(1)).findById(appraisalId);
    }

    @Test
    void getAppraisalById_shouldThrowResourceNotFoundException_whenAppraisalDoesNotExist() {
        UUID nonExistentId = UUID.randomUUID();
        when(appraisalRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            appraisalService.getAppraisalById(nonExistentId.toString());
        });

        assertEquals("Appraisal not found with ID: " + nonExistentId.toString(), thrown.getMessage());
        verify(appraisalRepository, times(1)).findById(nonExistentId);
    }

    @Test
    void getAppraisalById_shouldThrowIllegalArgumentException_whenIdIsInvalidFormat() {
        String invalidIdFormat = "invalid-uuid-format";

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            appraisalService.getAppraisalById(invalidIdFormat);
        });

        assertTrue(thrown.getMessage().contains("Invalid UUID string"));
        verifyNoInteractions(appraisalRepository);
    }

    @Test
    void getAppraisalByIdAndCurrentUser_shouldReturnAppraisal_whenIdAndUserMatch() {
        when(userContextService.getCurrentUserIdOptional()).thenReturn(Optional.of(userId));
        when(appraisalRepository.findByIdAndUserId(appraisalId, userId)).thenReturn(Optional.of(appraisal));

        Appraisal result = appraisalService.getAppraisalByIdAndCurrentUser(appraisalId.toString());

        assertNotNull(result);
        assertEquals(appraisalId, result.getId());
        verify(userContextService, times(1)).getCurrentUserIdOptional();
        verify(appraisalRepository, times(1)).findByIdAndUserId(appraisalId, userId);
    }

    @Test
    void getAppraisalByIdAndCurrentUser_shouldThrowResourceNotFoundException_whenAppraisalDoesNotExistForUser() {
        UUID otherUserId = UUID.randomUUID();
        when(userContextService.getCurrentUserIdOptional()).thenReturn(Optional.of(otherUserId));
        when(appraisalRepository.findByIdAndUserId(appraisalId, otherUserId)).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            appraisalService.getAppraisalByIdAndCurrentUser(appraisalId.toString());
        });

        assertEquals("Appraisal not found with ID: " + appraisalId.toString() + " for current user.", thrown.getMessage());
        verify(userContextService, times(1)).getCurrentUserIdOptional();
        verify(appraisalRepository, times(1)).findByIdAndUserId(appraisalId, otherUserId);
    }

    @Test
    void getAppraisalByIdAndCurrentUser_shouldThrowResourceNotFoundException_whenUserContextIsEmpty() {
        when(userContextService.getCurrentUserIdOptional()).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            appraisalService.getAppraisalByIdAndCurrentUser(appraisalId.toString());
        });

        assertEquals("User not authenticated or user ID not found.", thrown.getMessage());
        verify(userContextService, times(1)).getCurrentUserIdOptional();
        verifyNoInteractions(appraisalRepository);
    }

    @Test
    void getAppraisalByIdAndCurrentUser_shouldThrowIllegalArgumentException_whenIdIsInvalidFormat() {
        String invalidIdFormat = "invalid-uuid-format";
        when(userContextService.getCurrentUserIdOptional()).thenReturn(Optional.of(userId));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            appraisalService.getAppraisalByIdAndCurrentUser(invalidIdFormat);
        });

        assertTrue(thrown.getMessage().contains("Invalid UUID string"));
        verify(userContextService, times(1)).getCurrentUserIdOptional();
        verifyNoInteractions(appraisalRepository);
    }

    @Test
    void updateAppraisalStatus_shouldUpdateStatus_whenAppraisalExistsAndStatusIsValid() {
        String newStatus = "COMPLETED";
        when(appraisalRepository.findById(appraisalId)).thenReturn(Optional.of(appraisal));
        when(appraisalRepository.save(any(Appraisal.class))).thenReturn(appraisal);

        Appraisal result = appraisalService.updateAppraisalStatus(appraisalId, newStatus);

        assertNotNull(result);
        assertEquals(newStatus, result.getStatus());
        verify(appraisalRepository, times(1)).findById(appraisalId);
        verify(appraisalRepository, times(1)).save(appraisal);
    }

    @Test
    void updateAppraisalStatus_shouldThrowResourceNotFoundException_whenAppraisalDoesNotExist() {
        String newStatus = "COMPLETED";
        when(appraisalRepository.findById(appraisalId)).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            appraisalService.updateAppraisalStatus(appraisalId, newStatus);
        });

        assertEquals("Appraisal not found with ID: " + appraisalId.toString(), thrown.getMessage());
        verify(appraisalRepository, times(1)).findById(appraisalId);
        verify(appraisalRepository, never()).save(any(Appraisal.class));
    }

    @Test
    void updateAppraisalResultData_shouldUpdateResultData_whenAppraisalExistsAndDataIsValid() {
        AppraisalDetailsDTO dto = new AppraisalDetailsDTO("House", "123 Main St", "City", "Dept", "1", 100.0);
        Map<String, Object> resultDataMap = new HashMap<>();
        resultDataMap.put("propertyType", "House");
        resultDataMap.put("address", "123 Main St");

        when(appraisalRepository.findById(appraisalId)).thenReturn(Optional.of(appraisal));
        when(objectMapper.convertValue(eq(dto), any(TypeReference.class))).thenReturn(resultDataMap);
        when(appraisalRepository.save(any(Appraisal.class))).thenReturn(appraisal);

        Appraisal result = appraisalService.updateAppraisalResultData(appraisalId, dto);

        assertNotNull(result);
        assertEquals(resultDataMap, result.getResultData());
        verify(appraisalRepository, times(1)).findById(appraisalId);
        verify(objectMapper, times(1)).convertValue(eq(dto), any(TypeReference.class));
        verify(appraisalRepository, times(1)).save(appraisal);
    }

    @Test
    void updateAppraisalResultData_shouldThrowResourceNotFoundException_whenAppraisalDoesNotExist() {
        AppraisalDetailsDTO dto = new AppraisalDetailsDTO("House", "123 Main St", "City", "Dept", "1", 100.0);
        when(appraisalRepository.findById(appraisalId)).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            appraisalService.updateAppraisalResultData(appraisalId, dto);
        });

        assertEquals("Appraisal not found with ID: " + appraisalId.toString(), thrown.getMessage());
        verify(appraisalRepository, times(1)).findById(appraisalId);
        verify(objectMapper, never()).convertValue(any(), any(TypeReference.class));
        verify(appraisalRepository, never()).save(any(Appraisal.class));
    }
}
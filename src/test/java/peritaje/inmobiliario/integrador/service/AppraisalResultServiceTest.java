package peritaje.inmobiliario.integrador.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import peritaje.inmobiliario.integrador.domain.AppraisalResult;
import peritaje.inmobiliario.integrador.repository.AppraisalResultRepository;

@ExtendWith(MockitoExtension.class)
class AppraisalResultServiceTest {

    @Mock
    private AppraisalResultRepository appraisalResultRepository;

    @InjectMocks
    private AppraisalResultService appraisalResultService;

    private AppraisalResult appraisalResult1;
    private AppraisalResult appraisalResult2;

    @BeforeEach
    void setUp() {
        appraisalResult1 = new AppraisalResult();
        appraisalResult1.setId(1L);
        appraisalResult1.setUserId("user123");
        appraisalResult1.setAppraisalData("{\"address\":\"Calle Falsa 123\", \"appraisalValue\":100000.0}");

        appraisalResult2 = new AppraisalResult();
        appraisalResult2.setId(2L);
        appraisalResult2.setUserId("user123");
        appraisalResult2.setAppraisalData("{\"address\":\"Avenida Siempre Viva 742\", \"appraisalValue\":200000.0}");
    }

    @Test
    void saveAppraisalResult_success() {
        when(appraisalResultRepository.save(appraisalResult1)).thenReturn(appraisalResult1);

        AppraisalResult savedResult = appraisalResultService.saveAppraisalResult(appraisalResult1);

        assertNotNull(savedResult);
        assertEquals(appraisalResult1.getId(), savedResult.getId());
        verify(appraisalResultRepository, times(1)).save(appraisalResult1);
    }

    @Test
    void getAppraisalResultById_found() {
        when(appraisalResultRepository.findById(1L)).thenReturn(Optional.of(appraisalResult1));

        Optional<AppraisalResult> foundResult = appraisalResultService.getAppraisalResultById(1L);

        assertTrue(foundResult.isPresent());
        assertEquals(appraisalResult1.getId(), foundResult.get().getId());
        verify(appraisalResultRepository, times(1)).findById(1L);
    }

    @Test
    void getAppraisalResultById_notFound() {
        when(appraisalResultRepository.findById(3L)).thenReturn(Optional.empty());

        Optional<AppraisalResult> foundResult = appraisalResultService.getAppraisalResultById(3L);

        assertFalse(foundResult.isPresent());
        verify(appraisalResultRepository, times(1)).findById(3L);
    }

    @Test
    void getAppraisalResultsByUserId_found() {
        List<AppraisalResult> userResults = Arrays.asList(appraisalResult1, appraisalResult2);
        when(appraisalResultRepository.findByUserId("user123")).thenReturn(userResults);

        List<AppraisalResult> results = appraisalResultService.getAppraisalResultsByUserId("user123");

        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.contains(appraisalResult1));
        assertTrue(results.contains(appraisalResult2));
        verify(appraisalResultRepository, times(1)).findByUserId("user123");
    }

    @Test
    void getAppraisalResultsByUserId_notFound() {
        when(appraisalResultRepository.findByUserId("nonExistentUser")).thenReturn(Arrays.asList());

        List<AppraisalResult> results = appraisalResultService.getAppraisalResultsByUserId("nonExistentUser");

        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(appraisalResultRepository, times(1)).findByUserId("nonExistentUser");
    }
}
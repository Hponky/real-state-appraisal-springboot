package peritaje.inmobiliario.integrador.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;
import org.springframework.test.web.reactive.server.WebTestClient;

import peritaje.inmobiliario.integrador.domain.Appraisal;
import peritaje.inmobiliario.integrador.exception.GlobalExceptionHandler;
import peritaje.inmobiliario.integrador.exception.PdfGenerationException;
import peritaje.inmobiliario.integrador.exception.ResourceNotFoundException;
import peritaje.inmobiliario.integrador.service.AppraisalService;
import peritaje.inmobiliario.integrador.service.IPdfGenerationService;


@ExtendWith(MockitoExtension.class)
class AppraisalControllerIntegrationTest {

    @Mock
    private AppraisalService appraisalService;

    @Mock
    private IPdfGenerationService pdfGenerationService;

    @InjectMocks
    private AppraisalController appraisalController;

    private WebTestClient webTestClient;

    private Appraisal appraisal;
    private final String mockUserId = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(appraisalController)
                .controllerAdvice(new GlobalExceptionHandler())
                .apply(springSecurity())
                .build();
        
        UUID appraisalId = UUID.randomUUID();
        appraisal = new Appraisal();
        appraisal.setId(appraisalId);
        appraisal.setUserId(UUID.fromString(mockUserId));
        appraisal.setFormData(Map.of("key", "value"));
        appraisal.setStatus("COMPLETED");
        appraisal.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getAppraisalsForCurrentUser_authenticated_returnsAppraisals() {
        when(appraisalService.getAppraisalsForCurrentUser()).thenReturn(List.of(appraisal));

        webTestClient.mutateWith(mockUser()).get().uri("/api/appraisal/history")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(appraisal.getId().toString());
    }


    @Test
    void downloadPdf_validAppraisal_returnsPdf() throws IOException {
        byte[] pdfBytes = "PDF content".getBytes();
        appraisal.setResultData(Map.of("some", "data"));
        when(appraisalService.getAppraisalById(appraisal.getId().toString())).thenReturn(appraisal);
        when(pdfGenerationService.generatePdf(anyString(), any())).thenReturn(pdfBytes);

        webTestClient.mutateWith(mockUser()).get().uri("/api/appraisal/download-pdf?appraisalId=" + appraisal.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectHeader().valueEquals("Content-Disposition", "attachment; filename=\"peritaje-inmobiliario.pdf\"")
                .expectBody(byte[].class).isEqualTo(pdfBytes);
    }

    @Test
    void downloadPdf_appraisalNotFound_returnsNotFound() {
        String randomId = UUID.randomUUID().toString();
        when(appraisalService.getAppraisalById(randomId)).thenThrow(new ResourceNotFoundException("Peritaje no encontrado con id: " + randomId));

        webTestClient.mutateWith(mockUser()).get().uri("/api/appraisal/download-pdf?appraisalId=" + randomId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Recurso no encontrado")
                .jsonPath("$.msg").isEqualTo("Peritaje no encontrado con id: " + randomId);
    }

    @Test
    void downloadPdf_appraisalHasNoResultData_returnsNotFound() {
        appraisal.setResultData(null);
        String expectedMessage = "Appraisal result data not found for ID: " + appraisal.getId().toString();
        when(appraisalService.getAppraisalById(appraisal.getId().toString())).thenThrow(new ResourceNotFoundException(expectedMessage));

        webTestClient.mutateWith(mockUser()).get().uri("/api/appraisal/download-pdf?appraisalId=" + appraisal.getId().toString())
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Recurso no encontrado")
                .jsonPath("$.msg").isEqualTo(expectedMessage);
    }

    @Test
    void downloadPdf_pdfGenerationFails_returnsInternalServerError() throws IOException {
        appraisal.setResultData(Map.of("some", "data"));
        when(appraisalService.getAppraisalById(appraisal.getId().toString())).thenReturn(appraisal);
        when(pdfGenerationService.generatePdf(anyString(), any())).thenThrow(new PdfGenerationException("Fallo en la generacion del PDF"));

        webTestClient.mutateWith(mockUser()).get().uri("/api/appraisal/download-pdf?appraisalId=" + appraisal.getId().toString())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Error interno del servidor")
                .jsonPath("$.error").isEqualTo("Fallo en la generacion del PDF");
    }

}

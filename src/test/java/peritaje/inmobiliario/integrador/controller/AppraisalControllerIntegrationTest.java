package peritaje.inmobiliario.integrador.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import peritaje.inmobiliario.integrador.config.TestSecurityConfig;
import peritaje.inmobiliario.integrador.domain.Appraisal;
import peritaje.inmobiliario.integrador.exception.PdfGenerationException;
import peritaje.inmobiliario.integrador.exception.ResourceNotFoundException;
import peritaje.inmobiliario.integrador.service.AppraisalService;
import peritaje.inmobiliario.integrador.service.IPdfGenerationService;
import peritaje.inmobiliario.integrador.service.IUserContextService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class AppraisalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppraisalService appraisalService;

    @MockBean
    private IPdfGenerationService pdfGenerationService;

    @MockBean
    private IUserContextService userContextService;

    private UUID testUserId;
    private Appraisal userAppraisal;
    private Appraisal otherUserAppraisal;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        userAppraisal = new Appraisal();
        userAppraisal.setId(UUID.randomUUID());
        userAppraisal.setUserId(testUserId);
        userAppraisal.setResultData(Map.of("key", "value"));

        otherUserAppraisal = new Appraisal();
        otherUserAppraisal.setId(UUID.randomUUID());
        otherUserAppraisal.setUserId(UUID.randomUUID());
        otherUserAppraisal.setResultData(Map.of("key", "value"));

        // Mock UserContextService for authenticated tests
        when(userContextService.getCurrentUserId()).thenReturn(testUserId);
    }

    // --- Pruebas para /api/appraisal/history ---

    @Test
    @WithMockUser
    void getAppraisalsForCurrentUser_authenticated_returnsUsersAppraisals() throws Exception {
        when(appraisalService.getAppraisalsForCurrentUser()).thenReturn(List.of(userAppraisal));

        mockMvc.perform(get("/api/appraisal/history")
                .accept(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(userAppraisal.getId().toString()));
    }

    @Test
    void getAppraisalsForCurrentUser_unauthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/appraisal/history")
                .accept(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getAppraisalsForCurrentUser_authenticatedNoAppraisals_returnsEmptyList() throws Exception {
        when(appraisalService.getAppraisalsForCurrentUser()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/appraisal/history")
                .accept(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // --- Pruebas para /api/appraisal/download-pdf ---

    @Test
    @WithMockUser
    void downloadPdf_authenticatedOwnAppraisal_returnsPdf() throws Exception {
        when(appraisalService.getAppraisalById(userAppraisal.getId().toString())).thenReturn(userAppraisal);
        when(pdfGenerationService.generatePdf(any(), any())).thenReturn("pdf-bytes".getBytes());

        mockMvc.perform(get("/api/appraisal/download-pdf")
                .param("appraisalId", userAppraisal.getId().toString())
                .accept(MediaType.APPLICATION_PDF).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"peritaje-inmobiliario.pdf\""));
    }

    @Test
    @WithMockUser
    void downloadPdf_authenticatedOtherUsersAppraisal_returnsNotFound_IDORPrevention() throws Exception {
        when(appraisalService.getAppraisalById(otherUserAppraisal.getId().toString()))
                .thenThrow(new ResourceNotFoundException("Appraisal not found"));

        mockMvc.perform(get("/api/appraisal/download-pdf")
                .param("appraisalId", otherUserAppraisal.getId().toString())
                .accept(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void downloadPdf_unauthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/appraisal/download-pdf")
                .param("appraisalId", userAppraisal.getId().toString())
                .accept(MediaType.APPLICATION_PDF).with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void downloadPdf_unauthenticated_appraisalNotFound_returnsUnauthorized() throws Exception {
        UUID nonExistentAppraisalId = UUID.randomUUID();
        when(appraisalService.getAppraisalById(nonExistentAppraisalId.toString()))
                .thenThrow(new ResourceNotFoundException("Appraisal not found"));

        mockMvc.perform(get("/api/appraisal/download-pdf")
                .param("appraisalId", nonExistentAppraisalId.toString())
                .accept(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void downloadPdf_appraisalNotFound_returnsNotFound() throws Exception {
        UUID nonExistentAppraisalId = UUID.randomUUID();
        when(appraisalService.getAppraisalById(nonExistentAppraisalId.toString()))
                .thenThrow(new ResourceNotFoundException("Appraisal not found"));

        mockMvc.perform(get("/api/appraisal/download-pdf")
                .param("appraisalId", nonExistentAppraisalId.toString())
                .accept(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void downloadPdf_appraisalHasNoResultData_returnsNotFound() throws Exception {
        Appraisal appraisalWithoutData = new Appraisal();
        appraisalWithoutData.setId(UUID.randomUUID());
        appraisalWithoutData.setUserId(testUserId);
        appraisalWithoutData.setResultData(null);

        when(appraisalService.getAppraisalById(appraisalWithoutData.getId().toString())).thenReturn(appraisalWithoutData);

        mockMvc.perform(get("/api/appraisal/download-pdf")
                .param("appraisalId", appraisalWithoutData.getId().toString())
                .accept(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void downloadPdf_pdfGenerationFails_returnsInternalServerError() throws Exception {
        when(appraisalService.getAppraisalById(userAppraisal.getId().toString())).thenReturn(userAppraisal);
        when(pdfGenerationService.generatePdf(any(), any())).thenThrow(new PdfGenerationException("Failed to generate PDF"));

        mockMvc.perform(get("/api/appraisal/download-pdf")
                .param("appraisalId", userAppraisal.getId().toString())
                .accept(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isInternalServerError());
    }
}

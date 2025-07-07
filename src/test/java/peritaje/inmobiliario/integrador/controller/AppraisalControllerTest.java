package peritaje.inmobiliario.integrador.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import peritaje.inmobiliario.integrador.domain.Appraisal;
import peritaje.inmobiliario.integrador.exception.PdfGenerationException;
import peritaje.inmobiliario.integrador.exception.ResourceNotFoundException;
import peritaje.inmobiliario.integrador.service.AppraisalService;
import peritaje.inmobiliario.integrador.service.IPdfGenerationService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
public class AppraisalControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AppraisalService appraisalService;

    @Mock
    private IPdfGenerationService pdfGenerationService;

    @InjectMocks
    private AppraisalController appraisalController;

    private Appraisal appraisal;
    private String appraisalId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(appraisalController).build();
        appraisalId = UUID.randomUUID().toString();
        appraisal = new Appraisal();
        appraisal.setId(UUID.fromString(appraisalId));
        appraisal.setResultData(Map.of("cliente", "Test Client"));
    }

    @Test
    void getAppraisalsForCurrentUser_shouldReturnOkAndListOfAppraisals() throws Exception {
        when(appraisalService.getAppraisalsForCurrentUser()).thenReturn(List.of(appraisal));

        mockMvc.perform(get("/api/appraisal/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(appraisalId)));
    }
    
    @Test
    void getAppraisalsForCurrentUser_shouldReturnOkAndEmptyList() throws Exception {
        when(appraisalService.getAppraisalsForCurrentUser()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/appraisal/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void downloadPdf_shouldReturnPdfBytes() throws Exception {
        when(appraisalService.getAppraisalById(appraisalId)).thenReturn(appraisal);
        when(pdfGenerationService.generatePdf(any(String.class), any(Map.class))).thenReturn(new byte[10]);

        mockMvc.perform(get("/api/appraisal/download-pdf").param("appraisalId", appraisalId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"filename\"; filename=\"peritaje-inmobiliario.pdf\""));
    }

    @Test
    void downloadPdf_whenAppraisalNotFound_shouldReturnNotFound() throws Exception {
        when(appraisalService.getAppraisalById(appraisalId)).thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/appraisal/download-pdf").param("appraisalId", appraisalId))
                .andExpect(status().isNotFound());
    }

    @Test
    void downloadPdf_whenResultDataIsNull_shouldReturnNotFound() throws Exception {
        appraisal.setResultData(null);
        when(appraisalService.getAppraisalById(appraisalId)).thenReturn(appraisal);

        mockMvc.perform(get("/api/appraisal/download-pdf").param("appraisalId", appraisalId))
                .andExpect(status().isNotFound());
    }

    @Test
    void downloadPdf_whenPdfGenerationFails_shouldReturnInternalServerError() throws Exception {
        when(appraisalService.getAppraisalById(appraisalId)).thenReturn(appraisal);
        when(pdfGenerationService.generatePdf(any(String.class), any(Map.class))).thenThrow(new PdfGenerationException("PDF error"));

        mockMvc.perform(get("/api/appraisal/download-pdf").param("appraisalId", appraisalId))
                .andExpect(status().isInternalServerError());
    }
}
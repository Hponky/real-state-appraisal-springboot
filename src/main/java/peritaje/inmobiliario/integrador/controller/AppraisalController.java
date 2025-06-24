package peritaje.inmobiliario.integrador.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import peritaje.inmobiliario.integrador.domain.Appraisal;
import peritaje.inmobiliario.integrador.service.AppraisalService;
import peritaje.inmobiliario.integrador.service.PdfGenerationService;

@RestController
@RequestMapping("/api/appraisal")
public class AppraisalController {

    private final AppraisalService appraisalService;
    private final PdfGenerationService pdfGenerationService;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(AppraisalController.class);

    public AppraisalController(AppraisalService appraisalService,
            PdfGenerationService pdfGenerationService,
            ObjectMapper objectMapper) {
        this.appraisalService = appraisalService;
        this.pdfGenerationService = pdfGenerationService;
        this.objectMapper = objectMapper;
        logger.info("AppraisalController initialized.");
    }

    @GetMapping("/history")
    public ResponseEntity<List<Appraisal>> getAppraisalsForCurrentUser() {
        List<Appraisal> results = appraisalService.getAppraisalsForCurrentUser();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/download-pdf")
    public ResponseEntity<byte[]> downloadPdf(@RequestParam("appraisalId") String appraisalId) {
        try {
            logger.info("Received request for PDF generation for appraisalId: {}", appraisalId);
            Appraisal appraisal = appraisalService.getAppraisalByIdAndCurrentUser(appraisalId)
                    .orElse(null);
            if (appraisal == null || appraisal.getResultData() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new byte[0]);
            }

            Map<String, Object> dataModel = appraisal.getResultData();

            byte[] pdfBytes = pdfGenerationService.generatePdf("pdf/appraisal-template", dataModel);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "peritaje-inmobiliario.pdf");
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (Exception e) {
            logger.error("Error generating PDF", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new byte[0]);
        }
    }

}

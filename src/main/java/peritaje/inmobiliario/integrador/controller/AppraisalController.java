package peritaje.inmobiliario.integrador.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import peritaje.inmobiliario.integrador.domain.Appraisal;
import peritaje.inmobiliario.integrador.exception.ResourceNotFoundException;
import peritaje.inmobiliario.integrador.service.AppraisalService;
import peritaje.inmobiliario.integrador.service.IPdfGenerationService;

@RestController
@RequestMapping("/api/appraisal")
public class AppraisalController {

    private final AppraisalService appraisalService;
    private final IPdfGenerationService pdfGenerationService;
    private static final Logger logger = LoggerFactory.getLogger(AppraisalController.class);

    public AppraisalController(AppraisalService appraisalService,
            IPdfGenerationService pdfGenerationService) {
        this.appraisalService = appraisalService;
        this.pdfGenerationService = pdfGenerationService;
        logger.info("AppraisalController initialized.");
    }

    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Appraisal>> getAppraisalsForCurrentUser() {
        List<Appraisal> results = appraisalService.getAppraisalsForCurrentUser();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/download-pdf")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadPdf(@RequestParam("appraisalId") String appraisalId) throws IOException {
        logger.info("Received request for PDF generation for appraisalId: {}", appraisalId);
        Appraisal appraisal = appraisalService.getAppraisalById(appraisalId);

        if (appraisal.getResultData() == null) {
            throw new ResourceNotFoundException("Appraisal result data not found for ID: " + appraisalId);
        }

        Map<String, Object> dataModel = appraisal.getResultData();

        byte[] pdfBytes = pdfGenerationService.generatePdf("pdf/appraisal-template", dataModel);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("peritaje-inmobiliario.pdf").build());
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

}

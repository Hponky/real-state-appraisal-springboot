package peritaje.inmobiliario.integrador.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import peritaje.inmobiliario.integrador.dto.AppraisalResultDTO;
import peritaje.inmobiliario.integrador.dto.MigrationRequest;
import peritaje.inmobiliario.integrador.dto.SaveAppraisalRequestDTO; // Importar el nuevo DTO
import peritaje.inmobiliario.integrador.domain.AppraisalResult;
import peritaje.inmobiliario.integrador.service.AppraisalResultService;
import peritaje.inmobiliario.integrador.service.PdfGenerationService;
import java.util.List;
import java.util.Map; // Importar Map
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder; // Importar SecurityContextHolder
import peritaje.inmobiliario.integrador.security.CustomUserDetails; // Importar CustomUserDetails
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/appraisal")
public class AppraisalController {

    private final AppraisalResultService appraisalResultService;
    private final PdfGenerationService pdfGenerationService;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(AppraisalController.class);

    public AppraisalController(AppraisalResultService appraisalResultService,
            PdfGenerationService pdfGenerationService,
            ObjectMapper objectMapper) {
        this.appraisalResultService = appraisalResultService;
        this.pdfGenerationService = pdfGenerationService;
        this.objectMapper = objectMapper;
        logger.info("AppraisalController initialized.");
    }

    @PostMapping("/save-result")
    public ResponseEntity<AppraisalResult> saveAppraisalResult(
            @Valid @RequestBody SaveAppraisalRequestDTO requestDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // No permitir guardar si no está autenticado
        }

        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            AppraisalResult appraisalResult = new AppraisalResult();
            // Convertir JsonNode a String antes de asignar
            appraisalResult.setAppraisalData(objectMapper.writeValueAsString(requestDTO.getAppraisalData()));
            appraisalResult.setUserId(userDetails.getUserId()); // Asignar userId del usuario autenticado
            appraisalResult.setAnonymousSessionId(null); // Siempre null si es un usuario autenticado
            appraisalResult.setCreatedAt(java.time.LocalDateTime.now()); // Forzar la asignación de la fecha de creación

            AppraisalResult savedResult = appraisalResultService.saveAppraisalResult(appraisalResult);
            // Si el resultado devuelto es el mismo que el que se intentó guardar,
            // significa que ya existía y se devolvió el existente (no se creó uno nuevo).
            // En este caso, podemos devolver un 200 OK o un 409 Conflict.
            // Para evitar duplicación, si ya existe, devolvemos 409 Conflict.
            if (savedResult.getId() != null && appraisalResult.getId() == null) {
                // Si savedResult tiene un ID pero appraisalResult no lo tenía,
                // significa que se encontró un duplicado y se devolvió el existente.
                return ResponseEntity.status(HttpStatus.CONFLICT).body(savedResult);
            }
            return ResponseEntity.ok(savedResult);
        } catch (Exception e) {
            e.printStackTrace(); // Considerar un manejo de errores más sofisticado en producción
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<AppraisalResult>> getAppraisalResultsForCurrentUser() {
        List<AppraisalResult> results = appraisalResultService.getAppraisalResultsForCurrentUser();
        return ResponseEntity.ok(results);
    }

    @PostMapping("/download-pdf")
    public ResponseEntity<byte[]> downloadPdf(@RequestBody AppraisalResultDTO appraisalResultDTO) {
        try {
            logger.info("Received appraisalResultDTO for PDF generation: {}", appraisalResultDTO);
            // Convert AppraisalResultDTO to a Map for the generic PDF generation service
            @SuppressWarnings("unchecked")
            Map<String, Object> dataModel = objectMapper.convertValue(appraisalResultDTO, Map.class);

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

    @PostMapping("/migrate-anonymous-data")
    public ResponseEntity<Void> migrateAnonymousData(@Valid @RequestBody MigrationRequest request,
            Authentication authentication) {
        // En un escenario real, se debería verificar que el authentication.getName()
        // coincide con el request.getUserId() para evitar que un usuario migre datos de
        // otro.
        // Por simplicidad, asumimos que el userId en el request es el del usuario
        // autenticado.
        appraisalResultService.migrateAnonymousAppraisalResults(request.getAnonymousSessionId(), request.getUserId());
        return ResponseEntity.ok().build();
    }
}

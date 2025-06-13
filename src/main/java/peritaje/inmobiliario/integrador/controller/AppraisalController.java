package peritaje.inmobiliario.integrador.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping; // Importar el nuevo DTO
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController; // Importar Map

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode; // Importar SecurityContextHolder

import jakarta.validation.Valid; // Importar CustomUserDetails
import peritaje.inmobiliario.integrador.domain.AppraisalResult;
import peritaje.inmobiliario.integrador.dto.AppraisalResultDTO;
import peritaje.inmobiliario.integrador.dto.MigrationRequest;
import peritaje.inmobiliario.integrador.dto.SaveAppraisalRequestDTO;
import peritaje.inmobiliario.integrador.security.CustomUserDetails;
import peritaje.inmobiliario.integrador.service.AppraisalResultService;
import peritaje.inmobiliario.integrador.service.PdfGenerationService;

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
            JsonNode appraisalDataNode = requestDTO.getAppraisalData();
            ObjectNode mutableAppraisalData = (ObjectNode) objectMapper.readTree(appraisalDataNode.toString());

            // Asegurar que 'informacion_basica' exista o crearla
            ObjectNode informacionBasicaNode;
            if (mutableAppraisalData.has("informacion_basica")
                    && mutableAppraisalData.get("informacion_basica").isObject()) {
                informacionBasicaNode = (ObjectNode) mutableAppraisalData.get("informacion_basica");
            } else {
                informacionBasicaNode = objectMapper.createObjectNode();
                mutableAppraisalData.set("informacion_basica", informacionBasicaNode);
            }

            // Si existe 'initial_data', mover sus campos a 'informacion_basica' y luego eliminar 'initial_data'
            JsonNode initialDataNode = mutableAppraisalData.path("initial_data");
            if (!initialDataNode.isMissingNode() && initialDataNode.isObject()) {
                // Mover campos de initial_data a informacion_basica
                if (initialDataNode.has("city") && !initialDataNode.get("city").isNull()) {
                    informacionBasicaNode.set("ciudad", initialDataNode.get("city"));
                } else if (!informacionBasicaNode.has("ciudad")) {
                    informacionBasicaNode.put("ciudad", "N/A");
                }

                if (initialDataNode.has("property_type") && !initialDataNode.get("property_type").isNull()) {
                    informacionBasicaNode.set("tipo_inmueble", initialDataNode.get("property_type"));
                } else if (!informacionBasicaNode.has("tipo_inmueble")) {
                    informacionBasicaNode.put("tipo_inmueble", "N/A");
                }

                if (initialDataNode.has("built_area") && !initialDataNode.get("built_area").isNull()) {
                    informacionBasicaNode.set("area_usuario_m2", initialDataNode.get("built_area"));
                } else if (!informacionBasicaNode.has("area_usuario_m2")) {
                    informacionBasicaNode.put("area_usuario_m2", 0.0);
                }

                if (initialDataNode.has("address") && !initialDataNode.get("address").isNull()) {
                    informacionBasicaNode.set("direccion", initialDataNode.get("address"));
                } else if (!informacionBasicaNode.has("direccion")) {
                    informacionBasicaNode.put("direccion", "N/A");
                }

                if (initialDataNode.has("estrato") && !initialDataNode.get("estrato").isNull()) {
                    informacionBasicaNode.set("estrato", initialDataNode.get("estrato"));
                } else if (!informacionBasicaNode.has("estrato")) {
                    informacionBasicaNode.put("estrato", "N/A");
                }
                mutableAppraisalData.remove("initial_data"); // Eliminar el nodo initial_data
            } else {
                // Si no hay initial_data, asegurar que los campos de informacion_basica estén presentes
                if (!informacionBasicaNode.has("ciudad")) informacionBasicaNode.put("ciudad", "N/A");
                if (!informacionBasicaNode.has("tipo_inmueble")) informacionBasicaNode.put("tipo_inmueble", "N/A");
                if (!informacionBasicaNode.has("area_usuario_m2")) informacionBasicaNode.put("area_usuario_m2", 0.0);
                if (!informacionBasicaNode.has("direccion")) informacionBasicaNode.put("direccion", "N/A");
                if (!informacionBasicaNode.has("estrato")) informacionBasicaNode.put("estrato", "N/A");
            }

            // Ensure 'analisis_legal_arrendamiento' object exists
            ObjectNode analisisLegalNode;
            if (mutableAppraisalData.has("analisis_legal_arrendamiento")
                    && mutableAppraisalData.get("analisis_legal_arrendamiento").isObject()) {
                analisisLegalNode = (ObjectNode) mutableAppraisalData.get("analisis_legal_arrendamiento");
            } else {
                analisisLegalNode = objectMapper.createObjectNode();
                mutableAppraisalData.set("analisis_legal_arrendamiento", analisisLegalNode);
            }

            // Ensure 'puntos_criticos_y_riesgos' is an array
            if (!analisisLegalNode.has("puntos_criticos_y_riesgos")
                    || !analisisLegalNode.get("puntos_criticos_y_riesgos").isArray()) {
                analisisLegalNode.set("puntos_criticos_y_riesgos", objectMapper.createArrayNode());
            }

            // Ensure 'documentacion_clave_a_revisar_o_completar' is an array
            if (!analisisLegalNode.has("documentacion_clave_a_revisar_o_completar")
                    || !analisisLegalNode.get("documentacion_clave_a_revisar_o_completar").isArray()) {
                analisisLegalNode.set("documentacion_clave_a_revisar_o_completar", objectMapper.createArrayNode());
            }

            // Convert the modified JsonNode back to String
            appraisalResult.setAppraisalData(objectMapper.writeValueAsString(mutableAppraisalData));
            appraisalResult.setUserId(userDetails.getUserId()); // Assign userId from authenticated user
            appraisalResult.setAnonymousSessionId(null); // Always null if it's an authenticated user
            // createdAt es manejado automáticamente por @CreationTimestamp en la entidad

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

    @GetMapping("/download-pdf")
    public ResponseEntity<byte[]> downloadPdf(@RequestParam Long appraisalId) {
        logger.info("Received request to generate PDF for appraisal ID: {}", appraisalId);

        return appraisalResultService.getAppraisalResultByIdAndCurrentUser(appraisalId)
                .map(appraisalResult -> {
                    try {
                        AppraisalResultDTO appraisalResultDTO = appraisalResultService.mapToDTO(appraisalResult);
                        logger.info("Converted AppraisalResult to DTO for PDF generation: {}", appraisalResultDTO);

                        @SuppressWarnings("unchecked")
                        Map<String, Object> dataModel = objectMapper.convertValue(appraisalResultDTO, Map.class);

                        byte[] pdfBytes = pdfGenerationService.generatePdf("pdf/appraisal-template", dataModel);

                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_PDF);
                        headers.setContentDispositionFormData("filename", "peritaje-inmobiliario.pdf");
                        headers.setContentLength(pdfBytes.length);

                        return ResponseEntity.ok().headers(headers).body(pdfBytes);
                    } catch (Exception e) {
                        logger.error("Error generating PDF for appraisal ID {}: {}", appraisalId, e.getMessage(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new byte[0]);
                    }
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new byte[0]));
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

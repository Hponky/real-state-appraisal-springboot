package peritaje.inmobiliario.integrador.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import peritaje.inmobiliario.integrador.domain.AppraisalResult;
import peritaje.inmobiliario.integrador.repository.AppraisalResultRepository;
import peritaje.inmobiliario.integrador.security.CustomUserDetails;

@Service
public class AppraisalResultService {

    private final AppraisalResultRepository appraisalResultRepository;
    private final ObjectMapper objectMapper; // Inyectar ObjectMapper

    public AppraisalResultService(AppraisalResultRepository appraisalResultRepository, ObjectMapper objectMapper) {
        this.appraisalResultRepository = appraisalResultRepository;
        this.objectMapper = objectMapper;
    }

    public AppraisalResult saveAppraisalResult(AppraisalResult appraisalResult) {
        // Obtener el userId del contexto de seguridad si el usuario está autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            userId = userDetails.getUserId();
            appraisalResult.setUserId(userId);
            appraisalResult.setAnonymousSessionId(null); // Si se guarda con usuario, no es anónimo
        }

        // Extraer requestId del JSON y asignarlo a la entidad
        try {
            JsonNode rootNode = objectMapper.readTree(appraisalResult.getAppraisalData());
            JsonNode requestIdNode = rootNode.path("informacion_basica").path("requestId");
            if (!requestIdNode.isMissingNode() && requestIdNode.isTextual()) {
                appraisalResult.setRequestId(requestIdNode.asText());
            }
        } catch (Exception e) {
            System.err.println("Error al parsear requestId del JSON: " + e.getMessage());
            // Manejar el error, quizás lanzar una excepción o loguear
        }

        // Verificar si ya existe un resultado con los mismos datos de tasación para
        // este usuario
        if (userId != null && appraisalResult.getAppraisalData() != null) {
            Optional<AppraisalResult> existingResult = appraisalResultRepository.findByAppraisalDataAndUserId(
                    appraisalResult.getAppraisalData(), userId);
            if (existingResult.isPresent()) {
                return existingResult.get(); // Devolver el resultado existente si ya fue guardado
            }
        }
        return appraisalResultRepository.save(appraisalResult);
    }

    public Optional<AppraisalResult> getAppraisalResultById(Long id) {
        return appraisalResultRepository.findById(id);
    }

    public List<AppraisalResult> getAppraisalResultsByUserId(String userId) {
        return appraisalResultRepository.findByUserId(userId);
    }

    public List<AppraisalResult> getAppraisalResultsForCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return appraisalResultRepository.findByUserId(userDetails.getUserId());
        }
        return List.of(); // Retorna una lista vacía si no hay usuario autenticado
    }

    public Optional<AppraisalResult> getAppraisalResultByIdAndCurrentUser(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return appraisalResultRepository.findByIdAndUserId(id, userDetails.getUserId());
        }
        return Optional.empty();
    }

    public Optional<AppraisalResult> getAppraisalResultByRequestIdAndCurrentUser(String requestId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return appraisalResultRepository.findByRequestIdAndUserId(requestId, userDetails.getUserId());
        }
        return Optional.empty();
    }

    public List<AppraisalResult> getAppraisalResultsByAnonymousSessionId(String anonymousSessionId) {
        return appraisalResultRepository.findByAnonymousSessionId(anonymousSessionId);
    }

    @Transactional
    public void migrateAnonymousAppraisalResults(String anonymousSessionId, String userId) {
        List<AppraisalResult> anonymousResults = appraisalResultRepository.findByAnonymousSessionId(anonymousSessionId);
        for (AppraisalResult result : anonymousResults) {
            result.setUserId(userId);
            result.setAnonymousSessionId(null); // Clear anonymous session ID after migration
            appraisalResultRepository.save(result);
        }
    }
}
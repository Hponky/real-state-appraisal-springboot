package peritaje.inmobiliario.integrador.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import peritaje.inmobiliario.integrador.dto.AppraisalDetailsDTO;
import peritaje.inmobiliario.integrador.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import peritaje.inmobiliario.integrador.domain.Appraisal;
import peritaje.inmobiliario.integrador.repository.AppraisalRepository;

@Service
public class AppraisalService {

    private final AppraisalRepository appraisalRepository;
    private final ObjectMapper objectMapper;
    private final IUserContextService userContextService;

    public AppraisalService(AppraisalRepository appraisalRepository, ObjectMapper objectMapper,
            IUserContextService userContextService) {
        this.appraisalRepository = appraisalRepository;
        this.objectMapper = objectMapper;
        this.userContextService = userContextService;
    }

    public List<Appraisal> getAppraisalsForCurrentUser() {
        UUID userId = userContextService.getCurrentUserId();
        return appraisalRepository.findByUserId(userId);
    }

    public Appraisal getAppraisalById(String id) {
        return appraisalRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal not found with ID: " + id));
    }

    public Appraisal getAppraisalByIdAndCurrentUser(String id) {
        UUID userId = userContextService.getCurrentUserIdOptional()
                .orElseThrow(() -> new ResourceNotFoundException("User not authenticated or user ID not found."));
        return appraisalRepository.findByIdAndUserId(UUID.fromString(id), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal not found with ID: " + id + " for current user."));
    }

    @Transactional
    public Appraisal updateAppraisalStatus(UUID id, String status) {
        Appraisal appraisal = appraisalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal not found with ID: " + id));
        appraisal.setStatus(status);
        return appraisalRepository.save(appraisal);
    }

    @Transactional
    public Appraisal updateAppraisalResultData(UUID id, AppraisalDetailsDTO appraisalDetailsDTO) {
        Appraisal appraisal = appraisalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal not found with ID: " + id));
        Map<String, Object> resultData = objectMapper.convertValue(appraisalDetailsDTO,
                new TypeReference<Map<String, Object>>() {
                });
        appraisal.setResultData(resultData);
        return appraisalRepository.save(appraisal);
    }
}
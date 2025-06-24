package peritaje.inmobiliario.integrador.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import peritaje.inmobiliario.integrador.domain.Appraisal;
import peritaje.inmobiliario.integrador.repository.AppraisalRepository;
import peritaje.inmobiliario.integrador.security.CustomUserDetails;

@Service
public class AppraisalService {

    private final AppraisalRepository appraisalRepository;

    public AppraisalService(AppraisalRepository appraisalRepository) {
        this.appraisalRepository = appraisalRepository;
    }

    public List<Appraisal> getAppraisalsForCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return appraisalRepository.findByUserId(userDetails.getUserId());
        }
        return List.of();
    }

    public Optional<Appraisal> getAppraisalByIdAndCurrentUser(String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return appraisalRepository.findByIdAndUserId(UUID.fromString(id), userDetails.getUserId());
        }
        return Optional.empty();
    }

    @Transactional
    public Appraisal saveAppraisal(Appraisal appraisal) {
        return appraisalRepository.save(appraisal);
    }

    @Transactional
    public Optional<Appraisal> updateAppraisalStatus(UUID id, String status) {
        return appraisalRepository.findById(id).map(appraisal -> {
            appraisal.setStatus(status);
            return appraisalRepository.save(appraisal);
        });
    }

    @Transactional
    public Optional<Appraisal> updateAppraisalResultData(UUID id, Map<String, Object> resultData) {
        return appraisalRepository.findById(id).map(appraisal -> {
            appraisal.setResultData(resultData);
            return appraisalRepository.save(appraisal);
        });
    }
}
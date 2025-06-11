package peritaje.inmobiliario.integrador.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import peritaje.inmobiliario.integrador.domain.AppraisalResult;

@Repository
public interface AppraisalResultRepository extends JpaRepository<AppraisalResult, Long> {
    List<AppraisalResult> findByUserId(String userId);

    List<AppraisalResult> findByAnonymousSessionId(String anonymousSessionId);

    Optional<AppraisalResult> findByIdAndUserId(Long id, String userId);

    Optional<AppraisalResult> findByAppraisalDataAndUserId(String appraisalData, String userId);

    Optional<AppraisalResult> findByRequestIdAndUserId(String requestId, String userId);
}
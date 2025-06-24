package peritaje.inmobiliario.integrador.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import peritaje.inmobiliario.integrador.domain.Appraisal;

@Repository
public interface AppraisalRepository extends JpaRepository<Appraisal, UUID> {

    List<Appraisal> findByUserId(UUID userId);

    Optional<Appraisal> findByIdAndUserId(UUID id, UUID userId);

}
package peritaje.inmobiliario.integrador.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp; // Importar CreationTimestamp

@Entity
@Table(name = "appraisal_results")
public class AppraisalResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId; // Supabase user ID

    @Column(name = "anonymous_session_id")
    private String anonymousSessionId; // ID para sesiones anónimas

    @Column(name = "request_id", unique = true)
    private String requestId; // UUID from frontend

    @Column(name = "appraisal_data", columnDefinition = "TEXT")
    private String appraisalData; // JSON string of appraisal data

    @CreationTimestamp // Anotación para que Hibernate establezca la fecha de creación automáticamente
    @Column(name = "created_at", nullable = false, updatable = false) // Asegurar que no sea nulo y no se actualice
    private LocalDateTime createdAt;

    // Constructors
    public AppraisalResult() {
        // El campo createdAt será manejado por @CreationTimestamp
    }

    public AppraisalResult(String userId, String anonymousSessionId, String requestId, String appraisalData) {
        this.userId = userId;
        this.anonymousSessionId = anonymousSessionId;
        this.requestId = requestId;
        this.appraisalData = appraisalData;
        // createdAt ya no se inicializa aquí, lo hace @CreationTimestamp
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAnonymousSessionId() {
        return anonymousSessionId;
    }

    public void setAnonymousSessionId(String anonymousSessionId) {
        this.anonymousSessionId = anonymousSessionId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getAppraisalData() {
        return appraisalData;
    }

    public void setAppraisalData(String appraisalData) {
        this.appraisalData = appraisalData;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
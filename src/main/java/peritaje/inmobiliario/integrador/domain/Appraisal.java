package peritaje.inmobiliario.integrador.domain;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Convert;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import peritaje.inmobiliario.integrador.util.JsonMapUserType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "appraisals")
public class Appraisal {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "anonymous_session_id")
    private String anonymousSessionId;

    @Convert(converter = JsonMapUserType.class)
    @Column(name = "form_data")
    private Map<String, Object> formData;

    @Convert(converter = JsonMapUserType.class)
    @Column(name = "result_data")
    private Map<String, Object> resultData;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "status")
    private String status;

}
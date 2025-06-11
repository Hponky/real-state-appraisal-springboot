package peritaje.inmobiliario.integrador.dto;

import jakarta.validation.constraints.NotBlank;

public class MigrationRequest {

    @NotBlank(message = "Anonymous session ID cannot be blank")
    private String anonymousSessionId;

    @NotBlank(message = "User ID cannot be blank")
    private String userId;

    // Constructors
    public MigrationRequest() {
    }

    public MigrationRequest(String anonymousSessionId, String userId) {
        this.anonymousSessionId = anonymousSessionId;
        this.userId = userId;
    }

    // Getters and Setters
    public String getAnonymousSessionId() {
        return anonymousSessionId;
    }

    public void setAnonymousSessionId(String anonymousSessionId) {
        this.anonymousSessionId = anonymousSessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
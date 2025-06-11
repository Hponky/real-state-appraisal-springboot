package peritaje.inmobiliario.integrador.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveAppraisalRequestDTO {
    private String userId;
    private String anonymousSessionId;
    private String appraisalData; // JSON completo como String
}
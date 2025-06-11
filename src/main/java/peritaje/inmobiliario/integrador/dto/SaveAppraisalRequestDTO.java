package peritaje.inmobiliario.integrador.dto;

import com.fasterxml.jackson.databind.JsonNode; // Importar JsonNode
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveAppraisalRequestDTO {
    private String userId;
    private String anonymousSessionId;
    private JsonNode appraisalData; // JSON completo como JsonNode
}
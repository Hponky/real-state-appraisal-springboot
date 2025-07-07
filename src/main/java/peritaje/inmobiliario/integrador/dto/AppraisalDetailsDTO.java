package peritaje.inmobiliario.integrador.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppraisalDetailsDTO {
    private String propertyType;
    private String address;
    private String city;
    private String department;
    private String estrato;
    private Double builtArea;
}
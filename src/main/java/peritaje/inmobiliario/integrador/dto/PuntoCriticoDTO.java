package peritaje.inmobiliario.integrador.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PuntoCriticoDTO {
    private String aspecto_legal_relevante;
    private String descripcion_implicacion_riesgo;
}
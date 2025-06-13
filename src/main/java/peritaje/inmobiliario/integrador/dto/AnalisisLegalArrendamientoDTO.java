package peritaje.inmobiliario.integrador.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalisisLegalArrendamientoDTO {
    private String requestId;
    private String tipo_uso_principal_analizado;
    private String viabilidad_general_preliminar;
    @JsonProperty("puntos_criticos_y_riesgos")
    private List<PuntoCriticoDTO> puntos_criticos_y_riesgos;
    @JsonProperty("documentacion_clave_a_revisar_o_completar")
    private List<DocumentoClaveDTO> documentacion_clave_a_revisar_o_completar;
    private List<String> consideraciones_contractuales_sugeridas;
    private String resumen_ejecutivo_legal;
}
package peritaje.inmobiliario.integrador.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppraisalResultDTO {
    @JsonProperty("informacion_basica")
    private InformacionBasica informacion_basica;
    @JsonProperty("analisis_mercado")
    private AnalisisMercado analisis_mercado;
    @JsonProperty("valoracion_arriendo_actual")
    private ValoracionArriendoActual valoracion_arriendo_actual;
    @JsonProperty("potencial_valorizacion_con_mejoras_explicado")
    private PotencialValorizacionConMejorasExplicado potencial_valorizacion_con_mejoras_explicado;

    @JsonProperty("analisis_cualitativo_arriendo")
    private AnalisisCualitativoArriendo analisis_cualitativo_arriendo;
    @JsonProperty("recomendaciones_proximos_pasos")
    private List<String> recomendaciones_proximos_pasos;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InformacionBasica {
        private String requestId;
        private String ciudad;
        private String tipo_inmueble;
        private String estrato;
        private Double area_usuario_m2;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalisisMercado {
        private RangoArriendoReferenciasCop rango_arriendo_referencias_cop;
        private String observacion_mercado;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RangoArriendoReferenciasCop {
        private Double min;
        private Double max;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValoracionArriendoActual {
        private Double estimacion_canon_mensual_cop;
        private String justificacion_estimacion_actual;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PotencialValorizacionConMejorasExplicado {
        private Double canon_potencial_total_estimado_cop;
        private String comentario_estrategia_valorizacion;
        private List<MejoraConImpactoDetallado> mejoras_con_impacto_detallado;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MejoraConImpactoDetallado {
        private String recomendacion_tecnica_evaluada;
        private String justificacion_tecnica_original_relevancia;
        private Double incremento_estimado_canon_cop;
        private String justificacion_estimacion_incremento_economico;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalisisCualitativoArriendo {
        private List<String> factores_positivos_potencial;
        private List<String> factores_a_considerar_o_mejorar;
        private String comentario_mercado_general_ciudad;
    }
}
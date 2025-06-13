package peritaje.inmobiliario.integrador.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import peritaje.inmobiliario.integrador.dto.AnalisisLegalArrendamientoDTO;
import peritaje.inmobiliario.integrador.dto.AppraisalResultDTO;
import peritaje.inmobiliario.integrador.dto.DocumentoClaveDTO;
import peritaje.inmobiliario.integrador.dto.PuntoCriticoDTO;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import peritaje.inmobiliario.integrador.domain.AppraisalResult;
import peritaje.inmobiliario.integrador.repository.AppraisalResultRepository;
import peritaje.inmobiliario.integrador.security.CustomUserDetails;

@Service
public class AppraisalResultService {

    private final AppraisalResultRepository appraisalResultRepository;
    private final ObjectMapper objectMapper; // Inyectar ObjectMapper

    public AppraisalResultService(AppraisalResultRepository appraisalResultRepository, ObjectMapper objectMapper) {
        this.appraisalResultRepository = appraisalResultRepository;
        this.objectMapper = objectMapper;
    }

    public AppraisalResult saveAppraisalResult(AppraisalResult appraisalResult) {
        // Obtener el userId del contexto de seguridad si el usuario está autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            userId = userDetails.getUserId();
            appraisalResult.setUserId(userId);
            appraisalResult.setAnonymousSessionId(null); // Si se guarda con usuario, no es anónimo
        }

        // Extraer requestId del JSON y asignarlo a la entidad
        try {
            JsonNode rootNode = objectMapper.readTree(appraisalResult.getAppraisalData());
            JsonNode requestIdNode = rootNode.path("informacion_basica").path("requestId");
            if (!requestIdNode.isMissingNode() && requestIdNode.isTextual()) {
                appraisalResult.setRequestId(requestIdNode.asText());
            }
        } catch (Exception e) {
            System.err.println("Error al parsear requestId del JSON: " + e.getMessage());
            // Manejar el error, quizás lanzar una excepción o loguear
        }

        // Verificar si ya existe un resultado con los mismos datos de tasación para
        // este usuario
        if (userId != null && appraisalResult.getAppraisalData() != null) {
            Optional<AppraisalResult> existingResult = appraisalResultRepository.findByAppraisalDataAndUserId(
                    appraisalResult.getAppraisalData(), userId);
            if (existingResult.isPresent()) {
                return existingResult.get(); // Devolver el resultado existente si ya fue guardado
            }
        }
        return appraisalResultRepository.save(appraisalResult);
    }

    public AppraisalResult getAppraisalResultByRequestId(String requestId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return appraisalResultRepository.findByRequestIdAndUserId(requestId, userDetails.getUserId()).orElse(null);
        }
        return null; // O lanzar una excepción si no hay usuario autenticado
    }

    public Optional<AppraisalResult> getAppraisalResultById(Long id) {
        return appraisalResultRepository.findById(id);
    }

    public List<AppraisalResult> getAppraisalResultsByUserId(String userId) {
        return appraisalResultRepository.findByUserId(userId);
    }

    public List<AppraisalResult> getAppraisalResultsForCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            List<AppraisalResult> results = appraisalResultRepository.findByUserId(userDetails.getUserId());
            results.forEach(result -> {
                System.out.println("DEBUG: AppraisalResult ID: " + result.getId()
                        + ", Raw appraisalData from DB: " + result.getAppraisalData());
                try {
                    JsonNode rootNode = objectMapper.readTree(result.getAppraisalData());
                    JsonNode initialDataNode = rootNode.path("initial_data");
                    if (!initialDataNode.isMissingNode()) {
                        System.out.println("DEBUG: Extracted initial_data from DB: " + initialDataNode.toString());
                    } else {
                        System.out.println("DEBUG: initial_data node is missing in appraisalData from DB for ID: "
                                + result.getId());
                    }
                } catch (Exception e) {
                    System.err.println("DEBUG: Error parsing appraisalData for logging initial_data for ID: "
                            + result.getId() + " - " + e.getMessage());
                }
            });
            return results;
        }
        return List.of(); // Retorna una lista vacía si no hay usuario autenticado
    }

    public Optional<AppraisalResult> getAppraisalResultByIdAndCurrentUser(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return appraisalResultRepository.findByIdAndUserId(id, userDetails.getUserId());
        }
        return Optional.empty();
    }

    public Optional<AppraisalResult> getAppraisalResultByRequestIdAndCurrentUser(String requestId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return appraisalResultRepository.findByRequestIdAndUserId(requestId, userDetails.getUserId());
        }
        return Optional.empty();
    }

    public List<AppraisalResult> getAppraisalResultsByAnonymousSessionId(String anonymousSessionId) {
        return appraisalResultRepository.findByAnonymousSessionId(anonymousSessionId);
    }

    @Transactional
    public void migrateAnonymousAppraisalResults(String anonymousSessionId, String userId) {
        List<AppraisalResult> anonymousResults = appraisalResultRepository.findByAnonymousSessionId(anonymousSessionId);
        for (AppraisalResult result : anonymousResults) {
            result.setUserId(userId);
            result.setAnonymousSessionId(null); // Clear anonymous session ID after migration
            appraisalResultRepository.save(result);
        }
    }

    public AppraisalResultDTO mapToDTO(AppraisalResult appraisalResult) {
        AppraisalResultDTO dto = new AppraisalResultDTO();
        try {
            String rawAppraisalData = appraisalResult.getAppraisalData();
            JsonNode rootNode = objectMapper.readTree(rawAppraisalData);

            // Populate InformacionBasica
            JsonNode initialDataNode = rootNode.path("initial_data");
            AppraisalResultDTO.InformacionBasica infoBasica = new AppraisalResultDTO.InformacionBasica();
            infoBasica.setRequestId(appraisalResult.getRequestId()); // Obtener requestId directamente de la entidad
            if (!initialDataNode.isMissingNode()) {
                infoBasica.setCiudad(initialDataNode.path("ciudad").asText(null));
                infoBasica.setTipo_inmueble(initialDataNode.path("tipo_inmueble").asText(null));
                infoBasica.setEstrato(initialDataNode.path("estrato").asText(null));
                infoBasica.setArea_usuario_m2(initialDataNode.path("area_usuario_m2").asDouble(0.0));
            }
            dto.setInformacion_basica(infoBasica);

            // Populate AnalisisMercado
            JsonNode analisisMercadoNode = rootNode.path("analisis_mercado");
            if (!analisisMercadoNode.isMissingNode()) {
                AppraisalResultDTO.AnalisisMercado analisisMercado = new AppraisalResultDTO.AnalisisMercado();
                JsonNode rangoArriendoNode = analisisMercadoNode.path("rango_arriendo_referencias_cop");
                if (!rangoArriendoNode.isMissingNode()) {
                    AppraisalResultDTO.RangoArriendoReferenciasCop rangoArriendo = new AppraisalResultDTO.RangoArriendoReferenciasCop();
                    rangoArriendo.setMin(rangoArriendoNode.path("min").asDouble(0.0));
                    rangoArriendo.setMax(rangoArriendoNode.path("max").asDouble(0.0));
                    analisisMercado.setRango_arriendo_referencias_cop(rangoArriendo);
                }
                analisisMercado.setObservacion_mercado(analisisMercadoNode.path("observacion_mercado").asText(null));
                dto.setAnalisis_mercado(analisisMercado);
            }

            // Populate ValoracionArriendoActual
            JsonNode valoracionArriendoActualNode = rootNode.path("valoracion_arriendo_actual");
            if (!valoracionArriendoActualNode.isMissingNode()) {
                AppraisalResultDTO.ValoracionArriendoActual valoracionArriendoActual = new AppraisalResultDTO.ValoracionArriendoActual();
                valoracionArriendoActual.setEstimacion_canon_mensual_cop(
                        valoracionArriendoActualNode.path("estimacion_canon_mensual_cop").asDouble(0.0));
                valoracionArriendoActual.setJustificacion_estimacion_actual(
                        valoracionArriendoActualNode.path("justificacion_estimacion_actual").asText(null));
                dto.setValoracion_arriendo_actual(valoracionArriendoActual);
            }

            // Populate PotencialValorizacionConMejorasExplicado
            JsonNode potencialValorizacionNode = rootNode.path("potencial_valorizacion_con_mejoras_explicado");
            if (!potencialValorizacionNode.isMissingNode()) {
                AppraisalResultDTO.PotencialValorizacionConMejorasExplicado potencialValorizacion = new AppraisalResultDTO.PotencialValorizacionConMejorasExplicado();
                potencialValorizacion.setCanon_potencial_total_estimado_cop(
                        potencialValorizacionNode.path("canon_potencial_total_estimado_cop").asDouble(0.0));
                potencialValorizacion.setComentario_estrategia_valorizacion(
                        potencialValorizacionNode.path("comentario_estrategia_valorizacion").asText(null));

                JsonNode mejorasNode = potencialValorizacionNode.path("mejoras_con_impacto_detallado");
                if (mejorasNode.isArray()) {
                    List<AppraisalResultDTO.MejoraConImpactoDetallado> mejoras = new ArrayList<>();
                    for (JsonNode mejoraNode : mejorasNode) {
                        AppraisalResultDTO.MejoraConImpactoDetallado mejora = new AppraisalResultDTO.MejoraConImpactoDetallado();
                        mejora.setRecomendacion_tecnica_evaluada(
                                mejoraNode.path("recomendacion_tecnica_evaluada").asText(null));
                        mejora.setJustificacion_tecnica_original_relevancia(
                                mejoraNode.path("justificacion_tecnica_original_relevancia").asText(null));
                        mejora.setIncremento_estimado_canon_cop(
                                mejoraNode.path("incremento_estimado_canon_cop").asDouble(0.0));
                        mejora.setJustificacion_estimacion_incremento_economico(
                                mejoraNode.path("justificacion_estimacion_incremento_economico").asText(null));
                        mejoras.add(mejora);
                    }
                    potencialValorizacion.setMejoras_con_impacto_detallado(mejoras);
                }
                dto.setPotencial_valorizacion_con_mejoras_explicado(potencialValorizacion);
            }

            // Populate AnalisisCualitativoArriendo
            JsonNode analisisCualitativoNode = rootNode.path("analisis_cualitativo_arriendo");
            if (!analisisCualitativoNode.isMissingNode()) {
                AppraisalResultDTO.AnalisisCualitativoArriendo analisisCualitativo = new AppraisalResultDTO.AnalisisCualitativoArriendo();

                JsonNode factoresPositivosNode = analisisCualitativoNode.path("factores_positivos_potencial");
                if (factoresPositivosNode.isArray()) {
                    List<String> factoresPositivos = new ArrayList<>();
                    for (JsonNode factor : factoresPositivosNode) {
                        factoresPositivos.add(factor.asText());
                    }
                    analisisCualitativo.setFactores_positivos_potencial(factoresPositivos);
                }

                JsonNode factoresConsiderarNode = analisisCualitativoNode.path("factores_a_considerar_o_mejorar");
                if (factoresConsiderarNode.isArray()) {
                    List<String> factoresConsiderar = new ArrayList<>();
                    for (JsonNode factor : factoresConsiderarNode) {
                        factoresConsiderar.add(factor.asText());
                    }
                    analisisCualitativo.setFactores_a_considerar_o_mejorar(factoresConsiderar);
                }
                analisisCualitativo.setComentario_mercado_general_ciudad(
                        analisisCualitativoNode.path("comentario_mercado_general_ciudad").asText(null));
                dto.setAnalisis_cualitativo_arriendo(analisisCualitativo);
            }

            // Populate recomendaciones_proximos_pasos
            JsonNode recomendacionesNode = rootNode.path("recomendaciones_proximos_pasos");
            if (recomendacionesNode.isArray()) {
                List<String> recomendaciones = new ArrayList<>();
                for (JsonNode recomendacion : recomendacionesNode) {
                    recomendaciones.add(recomendacion.asText());
                }
                dto.setRecomendaciones_proximos_pasos(recomendaciones);
            }

            // Populate AnalisisLegalArrendamientoDTO
            JsonNode analisisLegalNode = rootNode.path("analisis_legal_arrendamiento");
            if (!analisisLegalNode.isMissingNode()) {
                AnalisisLegalArrendamientoDTO analisisLegal = new AnalisisLegalArrendamientoDTO();
                analisisLegal.setRequestId(analisisLegalNode.path("requestId").asText(null));
                analisisLegal.setTipo_uso_principal_analizado(
                        analisisLegalNode.path("tipo_uso_principal_analizado").asText(null));
                analisisLegal.setViabilidad_general_preliminar(
                        analisisLegalNode.path("viabilidad_general_preliminar").asText(null));
                analisisLegal
                        .setResumen_ejecutivo_legal(analisisLegalNode.path("resumen_ejecutivo_legal").asText(null));

                JsonNode puntosCriticosNode = analisisLegalNode.path("puntos_criticos_y_riesgos");
                if (puntosCriticosNode.isArray()) {
                    List<PuntoCriticoDTO> puntosCriticos = new ArrayList<>();
                    for (JsonNode puntoNode : puntosCriticosNode) {
                        PuntoCriticoDTO punto = new PuntoCriticoDTO();
                        punto.setTitulo(puntoNode.path("aspecto_legal_relevante").asText(null));
                        punto.setDescripcion(puntoNode.path("descripcion_implicacion_riesgo").asText(null));
                        puntosCriticos.add(punto);
                    }
                    analisisLegal.setPuntos_criticos_y_riesgos(puntosCriticos);
                }

                JsonNode documentosClaveNode = analisisLegalNode.path("documentacion_clave_a_revisar_o_completar");
                if (documentosClaveNode.isArray()) {
                    List<DocumentoClaveDTO> documentosClave = new ArrayList<>();
                    for (JsonNode docNode : documentosClaveNode) {
                        DocumentoClaveDTO documento = new DocumentoClaveDTO();
                        documento.setNombre(docNode.path("documento").asText(null));
                        documento.setEstado(docNode.path("importancia_para_arrendamiento").asText(null));
                        documentosClave.add(documento);
                    }
                    analisisLegal.setDocumentacion_clave_a_revisar_o_completar(documentosClave);
                }

                JsonNode consideracionesContractualesNode = analisisLegalNode
                        .path("consideraciones_contractuales_sugeridas");
                if (consideracionesContractualesNode.isArray()) {
                    List<String> consideraciones = new ArrayList<>();
                    for (JsonNode consideracion : consideracionesContractualesNode) {
                        consideraciones.add(consideracion.asText());
                    }
                    analisisLegal.setConsideraciones_contractuales_sugeridas(consideraciones);
                }
                dto.setAnalisisLegalArrendamiento(analisisLegal);
            }

        } catch (Exception e) {
            System.err.println("Error al parsear appraisalData a AppraisalResultDTO: " + e.getMessage());
            // Dependiendo del manejo de errores deseado, podrías lanzar una excepción
            // personalizada o devolver un DTO parcial/nulo.
        }
        return dto;
    }
}
package peritaje.inmobiliario.integrador.service;

import peritaje.inmobiliario.integrador.dto.AppraisalResultDTO;

public interface HtmlGenerationService {
    String generateHtmlContent(AppraisalResultDTO appraisalResultDTO);
}
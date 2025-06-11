package peritaje.inmobiliario.integrador.service;

import java.util.Map;

public interface HtmlGenerationService {
    String generateHtmlContent(String templateName, Map<String, Object> dataModel);
}

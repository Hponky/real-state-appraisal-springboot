package peritaje.inmobiliario.integrador.service;

import java.io.IOException;
import java.util.Map;

public interface IPdfGenerationService {
    byte[] generatePdf(String templateName, Map<String, Object> dataModel) throws IOException;
}
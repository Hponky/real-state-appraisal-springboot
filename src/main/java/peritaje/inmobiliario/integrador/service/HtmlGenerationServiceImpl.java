package peritaje.inmobiliario.integrador.service;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import peritaje.inmobiliario.integrador.dto.AppraisalResultDTO;

@Service
public class HtmlGenerationServiceImpl implements HtmlGenerationService {

    private final TemplateEngine templateEngine;

    public HtmlGenerationServiceImpl(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public String generateHtmlContent(AppraisalResultDTO appraisalResultDTO) {
        Context context = new Context();
        context.setVariable("appraisal", appraisalResultDTO);
        // Asume que la plantilla se encuentra en
        // src/main/resources/templates/pdf/appraisal-template.html
        return templateEngine.process("pdf/appraisal-template", context);
    }
}
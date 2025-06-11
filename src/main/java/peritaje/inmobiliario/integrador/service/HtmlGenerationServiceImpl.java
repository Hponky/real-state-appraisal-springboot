package peritaje.inmobiliario.integrador.service;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.util.Map;

@Service
public class HtmlGenerationServiceImpl implements HtmlGenerationService {

    private final TemplateEngine templateEngine;

    public HtmlGenerationServiceImpl(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public String generateHtmlContent(String templateName, Map<String, Object> dataModel) {
        Context context = new Context();
        context.setVariables(dataModel);
        return templateEngine.process(templateName, context);
    }
}

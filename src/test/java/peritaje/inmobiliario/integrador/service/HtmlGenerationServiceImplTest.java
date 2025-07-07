package peritaje.inmobiliario.integrador.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateInputException;

@ExtendWith(MockitoExtension.class)
public class HtmlGenerationServiceImplTest {

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private HtmlGenerationServiceImpl htmlGenerationService;

    private String templateName;
    private Map<String, Object> dataModel;

    @BeforeEach
    void setUp() {
        templateName = "testTemplate";
        dataModel = new HashMap<>();
        dataModel.put("key", "value");
    }

    @Test
    void generateHtmlContent_shouldReturnHtml_whenTemplateAndDataAreValid() {
        String expectedHtml = "<html><body>Hello, value!</body></html>";
        when(templateEngine.process(eq(templateName), any(Context.class))).thenReturn(expectedHtml);

        String result = htmlGenerationService.generateHtmlContent(templateName, dataModel);

        assertNotNull(result);
        assertEquals(expectedHtml, result);
        verify(templateEngine, times(1)).process(eq(templateName), any(Context.class));
    }

    @Test
    void generateHtmlContent_shouldThrowException_whenTemplateIsInvalid() {
        when(templateEngine.process(eq(templateName), any(Context.class)))
                .thenThrow(new TemplateInputException("Template not found"));

        Exception exception = assertThrows(TemplateInputException.class, () -> {
            htmlGenerationService.generateHtmlContent(templateName, dataModel);
        });

        assertTrue(exception.getMessage().contains("Template not found"));
        verify(templateEngine, times(1)).process(eq(templateName), any(Context.class));
    }

    @Test
    void generateHtmlContent_shouldHandleEmptyDataModel() {
        dataModel = Collections.emptyMap();
        String expectedHtml = "<html><body>No data.</body></html>";
        when(templateEngine.process(eq(templateName), any(Context.class))).thenReturn(expectedHtml);

        String result = htmlGenerationService.generateHtmlContent(templateName, dataModel);

        assertNotNull(result);
        assertEquals(expectedHtml, result);
        verify(templateEngine, times(1)).process(eq(templateName), any(Context.class));
    }
}
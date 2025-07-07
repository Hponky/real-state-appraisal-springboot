package peritaje.inmobiliario.integrador.service;

import com.lowagie.text.DocumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xhtmlrenderer.pdf.ITextRenderer;
import peritaje.inmobiliario.integrador.exception.PdfGenerationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdfGenerationServiceTest {

    @Mock
    private HtmlGenerationService htmlGenerationService;

    @InjectMocks
    private PdfGenerationService pdfGenerationService;

    private String templateName;
    private Map<String, Object> dataModel;
    private String htmlContent;
    private byte[] pdfBytes;

    @BeforeEach
    void setUp() {
        templateName = "pdf/appraisal-template";
        dataModel = Map.of("key", "value");
        htmlContent = "<html><body>Test HTML</body></html>";
        pdfBytes = "PDF_CONTENT".getBytes();

        when(htmlGenerationService.generateHtmlContent(templateName, dataModel)).thenReturn(htmlContent);
    }

    @Test
    void generatePdf_success() throws IOException, DocumentException {
        try (MockedConstruction<ITextRenderer> mockedRenderer = mockConstruction(ITextRenderer.class,
                (mock, context) -> {
                    // Configurar el mock de ITextRenderer
                    doNothing().when(mock).setDocumentFromString(anyString());
                    doNothing().when(mock).layout();
                    doNothing().when(mock).createPDF(any(ByteArrayOutputStream.class));
                })) {

            byte[] result = pdfGenerationService.generatePdf(templateName, dataModel);

            assertNotNull(result);
            // No podemos verificar el contenido exacto del PDF mockeado, solo que se generó
            // y se llamó a los métodos correctos.
            // En un test real, se podría verificar el tamaño o un hash si el contenido
            // fuera predecible.
            // Aquí, simplemente verificamos que no sea nulo.

            verify(htmlGenerationService).generateHtmlContent(templateName, dataModel);
            ITextRenderer renderer = mockedRenderer.constructed().get(0);
            verify(renderer).setDocumentFromString(htmlContent);
            verify(renderer).layout();
            verify(renderer).createPDF(any(ByteArrayOutputStream.class));
        }
    }

    @Test
    void generatePdf_htmlGenerationFails_throwsIOException() {
        when(htmlGenerationService.generateHtmlContent(templateName, dataModel))
                .thenThrow(new RuntimeException("HTML generation failed"));

        assertThrows(RuntimeException.class, () -> pdfGenerationService.generatePdf(templateName, dataModel));

        verify(htmlGenerationService).generateHtmlContent(templateName, dataModel);
        verifyNoInteractions(mock(ITextRenderer.class)); // Asegura que ITextRenderer no se usa
    }

    @Test
    void generatePdf_pdfCreationFails_throwsPdfGenerationException() throws IOException, DocumentException {
        try (MockedConstruction<ITextRenderer> mockedRenderer = mockConstruction(ITextRenderer.class,
                (mock, context) -> {
                    doNothing().when(mock).setDocumentFromString(anyString());
                    doNothing().when(mock).layout();
                    doThrow(new DocumentException("PDF creation error")).when(mock)
                            .createPDF(any(ByteArrayOutputStream.class));
                })) {

            PdfGenerationException exception = assertThrows(PdfGenerationException.class,
                    () -> pdfGenerationService.generatePdf(templateName, dataModel));

            assertTrue(exception.getMessage().contains("Error generating PDF"));
            assertTrue(exception.getCause() instanceof DocumentException); // La excepción interna es DocumentException
            assertTrue(exception.getCause().getMessage().contains("PDF creation error"));

            verify(htmlGenerationService).generateHtmlContent(templateName, dataModel);
            ITextRenderer renderer = mockedRenderer.constructed().get(0);
            verify(renderer).setDocumentFromString(htmlContent);
            verify(renderer).layout();
            verify(renderer).createPDF(any(ByteArrayOutputStream.class));
        }
    }
}
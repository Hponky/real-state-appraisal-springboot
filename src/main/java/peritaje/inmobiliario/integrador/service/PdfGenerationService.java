package peritaje.inmobiliario.integrador.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@Service
public class PdfGenerationService {

        private final HtmlGenerationService htmlGenerationService;

        @Autowired
        public PdfGenerationService(HtmlGenerationService htmlGenerationService) {
                this.htmlGenerationService = htmlGenerationService;
        }

        public byte[] generatePdf(String templateName, Map<String, Object> dataModel) throws IOException {
                String htmlContent = htmlGenerationService.generateHtmlContent(templateName, dataModel);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ITextRenderer renderer = new ITextRenderer();
                renderer.setDocumentFromString(htmlContent);
                renderer.layout();
                try {
                        renderer.createPDF(baos);
                } catch (com.lowagie.text.DocumentException e) {
                        throw new IOException("Error generating PDF: " + e.getMessage(), e);
                }

                return baos.toByteArray();
        }
}

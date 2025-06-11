package peritaje.inmobiliario.integrador.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;
import peritaje.inmobiliario.integrador.dto.AppraisalResultDTO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

@Service
public class PdfGenerationService {

        private final HtmlGenerationService htmlGenerationService;

        @Autowired
        public PdfGenerationService(HtmlGenerationService htmlGenerationService) {
                this.htmlGenerationService = htmlGenerationService;
        }

        public byte[] generatePdf(AppraisalResultDTO appraisalResultDTO) throws IOException {
                String htmlContent = htmlGenerationService.generateHtmlContent(appraisalResultDTO);

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
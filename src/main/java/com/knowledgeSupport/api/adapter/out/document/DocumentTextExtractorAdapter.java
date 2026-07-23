package com.knowledgeSupport.api.adapter.out.document;

import com.knowledgeSupport.api.application.port.out.DocumentExtractionException;
import com.knowledgeSupport.api.application.port.out.DocumentTextExtractorPort;
import com.knowledgeSupport.api.application.port.out.ExtractedPage;
import com.knowledgeSupport.api.domain.model.enums.DocumentType;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Outbound adapter: turns PDF/DOCX bytes into per-page text. PDFBox for PDF (one stripper
 * call per page, so a page number survives into the chunk); POI for DOCX (the OOXML has no
 * real pagination, so it comes back as a single page with {@code page = null}).
 */
@Component
public class DocumentTextExtractorAdapter implements DocumentTextExtractorPort {

    @Override
    public List<ExtractedPage> extract(byte[] content, DocumentType type) {
        try {
            return switch (type) {
                case PDF -> extractPdf(content);
                case DOCX -> extractDocx(content);
            };
        } catch (IOException | RuntimeException e) {
            throw new DocumentExtractionException(
                    "Não foi possível ler o arquivo " + type + ": " + e.getMessage(), e);
        }
    }

    private List<ExtractedPage> extractPdf(byte[] content) throws IOException {
        List<ExtractedPage> pages = new ArrayList<>();
        try (PDDocument pdf = Loader.loadPDF(content)) {
            PDFTextStripper stripper = new PDFTextStripper();
            int pageCount = pdf.getNumberOfPages();
            for (int page = 1; page <= pageCount; page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                pages.add(new ExtractedPage(page, stripper.getText(pdf)));
            }
        }
        return pages;
    }

    private List<ExtractedPage> extractDocx(byte[] content) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(content));
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return List.of(new ExtractedPage(null, extractor.getText()));
        }
    }
}

package com.knowledgeSupport.api.adapter.out.document;

import com.knowledgeSupport.api.application.port.out.DocumentExtractionException;
import com.knowledgeSupport.api.application.port.out.ExtractedPage;
import com.knowledgeSupport.api.domain.model.enums.DocumentType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentTextExtractorAdapterTest {

    private final DocumentTextExtractorAdapter adapter = new DocumentTextExtractorAdapter();

    @Test
    void extract_pdf_returnsOnePageEntryPerPageWithItsOwnText() throws Exception {
        byte[] pdf = buildPdf("Texto da pagina um", "Texto da pagina dois");

        List<ExtractedPage> pages = adapter.extract(pdf, DocumentType.PDF);

        assertEquals(2, pages.size());
        assertEquals(1, pages.get(0).page());
        assertTrue(pages.get(0).text().contains("Texto da pagina um"));
        assertEquals(2, pages.get(1).page());
        assertTrue(pages.get(1).text().contains("Texto da pagina dois"));
    }

    @Test
    void extract_docx_returnsSinglePageWithNullPageNumber() throws Exception {
        byte[] docx = buildDocx("Texto do manual em docx");

        List<ExtractedPage> pages = adapter.extract(docx, DocumentType.DOCX);

        assertEquals(1, pages.size());
        assertEquals(null, pages.get(0).page());
        assertTrue(pages.get(0).text().contains("Texto do manual em docx"));
    }

    @Test
    void extract_corruptedPdf_throwsDocumentExtractionException() {
        byte[] garbage = "isso nao e um pdf".getBytes();

        assertThrows(DocumentExtractionException.class, () -> adapter.extract(garbage, DocumentType.PDF));
    }

    @Test
    void extract_corruptedDocx_throwsDocumentExtractionException() {
        byte[] garbage = "isso nao e um docx".getBytes();

        assertThrows(DocumentExtractionException.class, () -> adapter.extract(garbage, DocumentType.DOCX));
    }

    private byte[] buildPdf(String... pageTexts) throws Exception {
        try (PDDocument document = new PDDocument()) {
            for (String text : pageTexts) {
                PDPage page = new PDPage();
                document.addPage(page);
                try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                    stream.beginText();
                    stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    stream.newLineAtOffset(50, 700);
                    stream.showText(text);
                    stream.endText();
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }

    private byte[] buildDocx(String text) throws Exception {
        try (XWPFDocument document = new XWPFDocument()) {
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText(text);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.write(out);
            return out.toByteArray();
        }
    }
}

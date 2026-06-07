package com.zamp.vendoronboarding.document;

import com.zamp.vendoronboarding.service.DocumentStorageService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentTextExtractionServiceTest {

    @TempDir
    Path tempDir;

    private DocumentStorageService documentStorageService;
    private DocumentTextExtractionService extractionService;

    @BeforeEach
    void setUp() throws Exception {
        Files.createDirectories(tempDir);
        documentStorageService = new DocumentStorageService(tempDir.toString());
        extractionService = new DocumentTextExtractionService(documentStorageService);
    }

    @Test
    void extract_validPdf_returnsCleanedText() throws Exception {
        UUID workflowRunId = UUID.randomUUID();
        Path runDirectory = tempDir.resolve(workflowRunId.toString());
        Files.createDirectories(runDirectory);
        Path pdfPath = runDirectory.resolve("sample.pdf");
        writePdfWithText(pdfPath, "Hello   PDF   extraction");

        String storagePath = workflowRunId + "/sample.pdf";
        DocumentTextExtractionResult result = extractionService.extract(storagePath);

        assertTrue(result.success());
        assertEquals("Hello PDF extraction", result.extractedText());
        assertEquals(20, result.characterCount());
        assertEquals(null, result.errorMessage());
    }

    @Test
    void extract_missingFile_returnsGracefulFailure() {
        DocumentTextExtractionResult result = extractionService.extract("missing/run/document.pdf");

        assertFalse(result.success());
        assertEquals("Stored document file not found.", result.errorMessage());
    }

    @Test
    void extract_nonPdfFile_returnsGracefulFailure() throws Exception {
        UUID workflowRunId = UUID.randomUUID();
        Path runDirectory = tempDir.resolve(workflowRunId.toString());
        Files.createDirectories(runDirectory);
        Path textPath = runDirectory.resolve("notes.txt");
        Files.writeString(textPath, "not a pdf");

        DocumentTextExtractionResult result = extractionService.extract(workflowRunId + "/notes.txt");

        assertFalse(result.success());
        assertEquals("File is not a valid PDF document.", result.errorMessage());
    }

    private void writePdfWithText(Path targetPath, String text) throws Exception {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText(text);
                contentStream.endText();
            }
            document.save(targetPath.toFile());
        }
    }
}

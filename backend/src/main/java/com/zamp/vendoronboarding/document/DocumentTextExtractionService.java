package com.zamp.vendoronboarding.document;

import com.zamp.vendoronboarding.service.DocumentStorageService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class DocumentTextExtractionService {

    private final DocumentStorageService documentStorageService;

    public DocumentTextExtractionService(DocumentStorageService documentStorageService) {
        this.documentStorageService = documentStorageService;
    }

    public DocumentTextExtractionResult extract(String storagePath) {
        Path filePath;
        try {
            filePath = documentStorageService.resolveAbsolutePath(storagePath);
        } catch (IllegalArgumentException ex) {
            return DocumentTextExtractionResult.failure(ex.getMessage());
        }

        if (!Files.exists(filePath)) {
            return DocumentTextExtractionResult.failure("Stored document file not found.");
        }

        try {
            byte[] fileBytes = Files.readAllBytes(filePath);
            if (!isPdf(filePath, fileBytes)) {
                return DocumentTextExtractionResult.failure("File is not a valid PDF document.");
            }

            try (PDDocument document = Loader.loadPDF(fileBytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String rawText = stripper.getText(document);
                String cleanedText = cleanWhitespace(rawText);
                return DocumentTextExtractionResult.success(cleanedText);
            }
        } catch (IOException ex) {
            return DocumentTextExtractionResult.failure("Failed to extract text from PDF: " + ex.getMessage());
        }
    }

    private boolean isPdf(Path filePath, byte[] fileBytes) {
        if (fileBytes.length >= 4) {
            String header = new String(fileBytes, 0, 4);
            if ("%PDF".equals(header)) {
                return true;
            }
        }
        String filename = filePath.getFileName().toString().toLowerCase();
        return filename.endsWith(".pdf");
    }

    String cleanWhitespace(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.replaceAll("\\s+", " ").trim();
    }
}

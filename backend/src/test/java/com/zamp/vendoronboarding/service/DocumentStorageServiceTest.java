package com.zamp.vendoronboarding.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentStorageServiceTest {

    @TempDir
    Path tempDir;

    private DocumentStorageService documentStorageService;

    @BeforeEach
    void setUp() throws Exception {
        documentStorageService = new DocumentStorageService(tempDir.toString());
        documentStorageService.ensureUploadDirectoryExists();
    }

    @Test
    void storePdf_validPdf_storesFileAndReturnsMetadata() {
        MockMultipartFile file = new MockMultipartFile(
                "taxRegistration",
                "tax-registration.pdf",
                "application/pdf",
                "%PDF-1.4 test".getBytes()
        );

        StoredDocumentFile stored = documentStorageService.storePdf(file, UUID.randomUUID());

        assertEquals("tax-registration.pdf", stored.originalFilename());
        assertEquals("application/pdf", stored.contentType());
        assertTrue(stored.fileSize() > 0);
        assertTrue(stored.storagePath() != null && !stored.storagePath().isBlank());
    }

    @Test
    void storePdf_nonPdfFile_throwsIllegalArgumentException() {
        MockMultipartFile file = new MockMultipartFile(
                "taxRegistration",
                "notes.txt",
                "text/plain",
                "hello".getBytes()
        );

        assertThrows(IllegalArgumentException.class,
                () -> documentStorageService.storePdf(file, UUID.randomUUID()));
    }

    @Test
    void storePdf_emptyFile_throwsIllegalArgumentException() {
        MockMultipartFile file = new MockMultipartFile(
                "taxRegistration",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        assertThrows(IllegalArgumentException.class,
                () -> documentStorageService.storePdf(file, UUID.randomUUID()));
    }
}

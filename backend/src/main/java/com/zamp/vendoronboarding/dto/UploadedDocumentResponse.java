package com.zamp.vendoronboarding.dto;

import com.zamp.vendoronboarding.entity.enums.DocumentType;

import java.time.Instant;

public record UploadedDocumentResponse(
        DocumentType documentType,
        String originalFilename,
        String contentType,
        Long fileSize,
        Instant uploadedAt,
        String extractedText,
        DocumentExtractionResponse extraction
) {
}

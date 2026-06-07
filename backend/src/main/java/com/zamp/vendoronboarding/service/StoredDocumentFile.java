package com.zamp.vendoronboarding.service;

public record StoredDocumentFile(
        String storagePath,
        String originalFilename,
        String contentType,
        long fileSize
) {
}

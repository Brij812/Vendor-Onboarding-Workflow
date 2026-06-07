package com.zamp.vendoronboarding.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class DocumentStorageService {

    private final Path uploadsRoot;

    public DocumentStorageService(@Value("${app.uploads.directory:./uploads}") String uploadsDirectory) {
        this.uploadsRoot = Paths.get(uploadsDirectory).toAbsolutePath().normalize();
    }

    @PostConstruct
    void ensureUploadDirectoryExists() throws IOException {
        Files.createDirectories(uploadsRoot);
    }

    public Path resolveAbsolutePath(String storagePath) {
        if (storagePath == null || storagePath.isBlank()) {
            throw new IllegalArgumentException("Storage path is required.");
        }
        Path resolved = uploadsRoot.resolve(storagePath).normalize();
        if (!resolved.startsWith(uploadsRoot)) {
            throw new IllegalArgumentException("Invalid storage path.");
        }
        return resolved;
    }

    public StoredDocumentFile storePdf(MultipartFile file, UUID workflowRunId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty.");
        }
        if (!isPdf(file)) {
            throw new IllegalArgumentException("Only PDF documents are supported.");
        }

        String originalFilename = sanitizeFilename(file.getOriginalFilename());
        Path runDirectory = uploadsRoot.resolve(workflowRunId.toString());
        try {
            Files.createDirectories(runDirectory);
            String storedFilename = UUID.randomUUID() + "_" + originalFilename;
            Path targetPath = runDirectory.resolve(storedFilename).normalize();
            if (!targetPath.startsWith(runDirectory)) {
                throw new IllegalArgumentException("Invalid file path.");
            }
            file.transferTo(targetPath);

            String relativePath = uploadsRoot.relativize(targetPath).toString().replace('\\', '/');
            String contentType = file.getContentType() != null ? file.getContentType() : "application/pdf";
            return new StoredDocumentFile(relativePath, originalFilename, contentType, file.getSize());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store uploaded document", ex);
        }
    }

    private boolean isPdf(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null && contentType.equalsIgnoreCase("application/pdf")) {
            return true;
        }
        String filename = file.getOriginalFilename();
        return filename != null && filename.toLowerCase().endsWith(".pdf");
    }

    public long deleteStoredFiles(java.util.List<String> storagePaths) {
        if (storagePaths == null || storagePaths.isEmpty()) {
            return 0;
        }

        long deleted = 0;
        for (String storagePath : storagePaths) {
            if (storagePath == null || storagePath.isBlank()) {
                continue;
            }
            try {
                Path absolutePath = resolveAbsolutePath(storagePath);
                if (Files.deleteIfExists(absolutePath)) {
                    deleted++;
                }
            } catch (RuntimeException | java.io.IOException ex) {
                // Best-effort cleanup for demo reset; DB rows are already removed.
            }
        }
        return deleted;
    }

    private String sanitizeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "document.pdf";
        }
        String filename = Paths.get(originalFilename).getFileName().toString().trim();
        if (filename.isBlank()) {
            return "document.pdf";
        }
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}

package com.zamp.vendoronboarding.repository;

import com.zamp.vendoronboarding.entity.DocumentExtraction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentExtractionRepository extends JpaRepository<DocumentExtraction, UUID> {

    Optional<DocumentExtraction> findByUploadedDocument_Id(UUID uploadedDocumentId);

    List<DocumentExtraction> findByWorkflowRun_IdOrderByCreatedAtAsc(UUID workflowRunId);

    void deleteByWorkflowRun_Id(UUID workflowRunId);
}

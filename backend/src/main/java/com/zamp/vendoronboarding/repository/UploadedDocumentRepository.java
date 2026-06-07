package com.zamp.vendoronboarding.repository;

import com.zamp.vendoronboarding.entity.UploadedDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UploadedDocumentRepository extends JpaRepository<UploadedDocument, UUID> {

    List<UploadedDocument> findByWorkflowRun_IdOrderByUploadedAtAsc(UUID workflowRunId);
}

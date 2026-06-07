package com.zamp.vendoronboarding.repository;

import com.zamp.vendoronboarding.entity.AuditSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuditSummaryRepository extends JpaRepository<AuditSummary, UUID> {

    Optional<AuditSummary> findByWorkflowRun_Id(UUID workflowRunId);

    void deleteByWorkflowRun_Id(UUID workflowRunId);
}

package com.zamp.vendoronboarding.repository;

import com.zamp.vendoronboarding.entity.WorkflowRun;
import com.zamp.vendoronboarding.entity.enums.DecisionStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkflowRunRepository extends JpaRepository<WorkflowRun, UUID> {

    Optional<WorkflowRun> findByDisplayRunId(String displayRunId);

    @EntityGraph(attributePaths = {"vendorSubmission"})
    List<WorkflowRun> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"vendorSubmission"})
    Optional<WorkflowRun> findWithVendorSubmissionById(UUID id);

    @EntityGraph(attributePaths = {"vendorSubmission"})
    Optional<WorkflowRun> findWithVendorSubmissionByDisplayRunId(String displayRunId);

    @EntityGraph(attributePaths = {"vendorSubmission"})
    List<WorkflowRun> findByFinalDecisionStatusOrderByCreatedAtDesc(DecisionStatus finalDecisionStatus);
}

package com.zamp.vendoronboarding.repository;

import com.zamp.vendoronboarding.entity.WorkflowStepLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkflowStepLogRepository extends JpaRepository<WorkflowStepLog, UUID> {

    List<WorkflowStepLog> findByWorkflowRun_IdOrderByStepOrderAsc(UUID workflowRunId);

    @Query("SELECT s FROM WorkflowStepLog s JOIN FETCH s.workflowRun WHERE s.id = :id")
    Optional<WorkflowStepLog> findByIdWithWorkflowRun(@Param("id") UUID id);
}

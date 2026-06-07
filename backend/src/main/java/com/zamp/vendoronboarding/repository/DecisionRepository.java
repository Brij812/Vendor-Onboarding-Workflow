package com.zamp.vendoronboarding.repository;

import com.zamp.vendoronboarding.entity.Decision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DecisionRepository extends JpaRepository<Decision, UUID> {

    Optional<Decision> findByWorkflowRun_Id(UUID workflowRunId);

    void deleteByWorkflowRun_Id(UUID workflowRunId);
}

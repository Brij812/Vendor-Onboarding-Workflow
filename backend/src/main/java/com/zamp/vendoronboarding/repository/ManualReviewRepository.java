package com.zamp.vendoronboarding.repository;

import com.zamp.vendoronboarding.entity.ManualReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ManualReviewRepository extends JpaRepository<ManualReview, UUID> {

    Optional<ManualReview> findByWorkflowRun_Id(UUID workflowRunId);

    void deleteByWorkflowRun_Id(UUID workflowRunId);
}

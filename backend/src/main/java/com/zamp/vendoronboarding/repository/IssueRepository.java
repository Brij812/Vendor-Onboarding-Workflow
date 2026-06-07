package com.zamp.vendoronboarding.repository;

import com.zamp.vendoronboarding.entity.Issue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IssueRepository extends JpaRepository<Issue, UUID> {

    List<Issue> findByWorkflowRun_IdOrderByCreatedAtAsc(UUID workflowRunId);

    void deleteByWorkflowRun_Id(UUID workflowRunId);
}

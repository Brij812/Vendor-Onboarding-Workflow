package com.zamp.vendoronboarding.repository;

import com.zamp.vendoronboarding.entity.Communication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CommunicationRepository extends JpaRepository<Communication, UUID> {

    Optional<Communication> findFirstByWorkflowRun_IdOrderByCreatedAtDesc(UUID workflowRunId);

    void deleteByWorkflowRun_Id(UUID workflowRunId);
}

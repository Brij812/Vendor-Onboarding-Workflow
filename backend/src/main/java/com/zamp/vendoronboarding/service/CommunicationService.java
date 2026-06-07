package com.zamp.vendoronboarding.service;

import com.zamp.vendoronboarding.communication.CommunicationDraft;
import com.zamp.vendoronboarding.dto.CommunicationResponse;
import com.zamp.vendoronboarding.entity.Communication;
import com.zamp.vendoronboarding.entity.WorkflowRun;
import com.zamp.vendoronboarding.repository.CommunicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class CommunicationService {

    private final CommunicationRepository communicationRepository;

    public CommunicationService(CommunicationRepository communicationRepository) {
        this.communicationRepository = communicationRepository;
    }

    @Transactional
    public Communication persistCommunication(WorkflowRun workflowRun, CommunicationDraft draft) {
        Communication communication = new Communication();
        communication.setWorkflowRun(workflowRun);
        communication.setCommunicationType(draft.communicationType());
        communication.setSubject(draft.subject());
        communication.setBody(draft.body());
        communication.setGenerationMethod(draft.generationMethod());
        communication.setRawLlmResponse(draft.rawLlmResponse());
        return communicationRepository.save(communication);
    }

    @Transactional(readOnly = true)
    public Optional<CommunicationResponse> findResponseByWorkflowRunId(UUID workflowRunId) {
        return communicationRepository.findFirstByWorkflowRun_IdOrderByCreatedAtDesc(workflowRunId)
                .map(this::toResponse);
    }

    private CommunicationResponse toResponse(Communication communication) {
        return new CommunicationResponse(
                communication.getCommunicationType(),
                communication.getSubject(),
                communication.getBody(),
                communication.getGenerationMethod()
        );
    }
}

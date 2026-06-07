package com.zamp.vendoronboarding.controller;

import com.zamp.vendoronboarding.dto.ManualReviewRequest;
import com.zamp.vendoronboarding.dto.ManualReviewResponse;
import com.zamp.vendoronboarding.dto.WorkflowRerunResponse;
import com.zamp.vendoronboarding.dto.WorkflowRunDetailResponse;
import com.zamp.vendoronboarding.dto.WorkflowRunSummaryResponse;
import com.zamp.vendoronboarding.service.ManualReviewService;
import com.zamp.vendoronboarding.service.WorkflowRerunService;
import com.zamp.vendoronboarding.service.WorkflowRunService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/workflow-runs")
public class WorkflowRunController {

    private final WorkflowRunService workflowRunService;
    private final ManualReviewService manualReviewService;
    private final WorkflowRerunService workflowRerunService;

    public WorkflowRunController(WorkflowRunService workflowRunService,
                                   ManualReviewService manualReviewService,
                                   WorkflowRerunService workflowRerunService) {
        this.workflowRunService = workflowRunService;
        this.manualReviewService = manualReviewService;
        this.workflowRerunService = workflowRerunService;
    }

    @GetMapping
    public List<WorkflowRunSummaryResponse> listWorkflowRuns() {
        return workflowRunService.findAllSummaries();
    }

    @GetMapping("/review-queue")
    public List<WorkflowRunSummaryResponse> listReviewQueueRuns() {
        return workflowRunService.findReviewQueueSummaries();
    }

    @GetMapping("/{id}")
    public WorkflowRunDetailResponse getWorkflowRun(@PathVariable String id) {
        return workflowRunService.getRunDetail(id);
    }

    @PostMapping("/{id}/manual-review")
    public ManualReviewResponse saveManualReview(@PathVariable String id,
                                                 @Valid @RequestBody ManualReviewRequest request) {
        return manualReviewService.saveManualReview(id, request);
    }

    @PostMapping("/{id}/rerun")
    public WorkflowRerunResponse rerunWorkflowRun(@PathVariable String id) {
        return workflowRerunService.rerunWorkflow(id);
    }
}

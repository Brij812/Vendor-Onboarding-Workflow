package com.zamp.vendoronboarding.controller;

import com.zamp.vendoronboarding.dto.ManualReviewRequest;
import com.zamp.vendoronboarding.dto.ManualReviewResponse;
import com.zamp.vendoronboarding.dto.VendorSubmissionDetailResponse;
import com.zamp.vendoronboarding.dto.WorkflowRerunResponse;
import com.zamp.vendoronboarding.dto.WorkflowRunDetailResponse;
import com.zamp.vendoronboarding.dto.WorkflowRunSummaryResponse;
import com.zamp.vendoronboarding.dto.WorkflowStepLogResponse;
import com.zamp.vendoronboarding.entity.enums.DecisionStatus;
import com.zamp.vendoronboarding.entity.enums.ReviewerOutcome;
import com.zamp.vendoronboarding.entity.enums.RunStatus;
import com.zamp.vendoronboarding.entity.enums.StepStatus;
import com.zamp.vendoronboarding.service.ManualReviewService;
import com.zamp.vendoronboarding.service.WorkflowRerunService;
import com.zamp.vendoronboarding.service.WorkflowRunService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkflowRunController.class)
class WorkflowRunControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkflowRunService workflowRunService;

    @MockBean
    private ManualReviewService manualReviewService;

    @MockBean
    private WorkflowRerunService workflowRerunService;

    @Test
    void getWorkflowRun_returnsDetail() throws Exception {
        UUID runId = UUID.randomUUID();
        when(workflowRunService.getRunDetail("RUN-0001")).thenReturn(sampleDetail(runId));

        mockMvc.perform(get("/api/workflow-runs/RUN-0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workflowRunId", is(runId.toString())))
                .andExpect(jsonPath("$.displayRunId", is("RUN-0001")))
                .andExpect(jsonPath("$.runStatus", is("PENDING")))
                .andExpect(jsonPath("$.steps", hasSize(1)))
                .andExpect(jsonPath("$.issues", hasSize(0)));
    }

    @Test
    void listWorkflowRuns_returnsSummaries() throws Exception {
        UUID runId = UUID.randomUUID();
        when(workflowRunService.findAllSummaries()).thenReturn(List.of(
                new WorkflowRunSummaryResponse(
                        runId,
                        "RUN-0001",
                        "BrightLayer Technologies Pvt Ltd",
                        RunStatus.PENDING,
                        null,
                        null,
                        null,
                        "Final decision generated: PENDING with risk score 25.",
                        Instant.parse("2026-06-04T12:00:00Z"),
                        null
                )
        ));

        mockMvc.perform(get("/api/workflow-runs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].vendorName", is("BrightLayer Technologies Pvt Ltd")))
                .andExpect(jsonPath("$[0].mainReason", is("Final decision generated: PENDING with risk score 25.")));
    }

    @Test
    void listReviewQueue_returnsPendingRuns() throws Exception {
        UUID runId = UUID.randomUUID();
        when(workflowRunService.findReviewQueueSummaries()).thenReturn(List.of(
                new WorkflowRunSummaryResponse(
                        runId,
                        "RUN-0002",
                        "Acme Corp",
                        RunStatus.COMPLETED,
                        null,
                        DecisionStatus.PENDING,
                        40,
                        "Procurement manual review required.",
                        Instant.parse("2026-06-04T12:00:00Z"),
                        Instant.parse("2026-06-04T12:05:00Z")
                )
        ));

        mockMvc.perform(get("/api/workflow-runs/review-queue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].finalDecisionStatus", is("PENDING")));
    }

    @Test
    void saveManualReview_returnsSavedReview() throws Exception {
        UUID runId = UUID.randomUUID();
        Instant reviewedAt = Instant.parse("2026-06-04T13:00:00Z");
        when(manualReviewService.saveManualReview(eq("RUN-0001"), any(ManualReviewRequest.class)))
                .thenReturn(new ManualReviewResponse(
                        runId,
                        ReviewerOutcome.APPROVED_AFTER_REVIEW,
                        "Vendor clarified bank details.",
                        reviewedAt
                ));

        mockMvc.perform(post("/api/workflow-runs/RUN-0001/manual-review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reviewerOutcome": "APPROVED_AFTER_REVIEW",
                                  "reviewerNote": "Vendor clarified bank details."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewerOutcome", is("APPROVED_AFTER_REVIEW")))
                .andExpect(jsonPath("$.reviewerNote", is("Vendor clarified bank details.")));
    }

    @Test
    void rerunWorkflow_returnsRunId() throws Exception {
        UUID runId = UUID.randomUUID();
        when(workflowRerunService.rerunWorkflow("RUN-0001"))
                .thenReturn(new WorkflowRerunResponse(runId));

        mockMvc.perform(post("/api/workflow-runs/RUN-0001/rerun"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.runId", is(runId.toString())));
    }

    private WorkflowRunDetailResponse sampleDetail(UUID runId) {
        return new WorkflowRunDetailResponse(
                runId,
                "RUN-0001",
                RunStatus.PENDING,
                null,
                new VendorSubmissionDetailResponse(
                        "BrightLayer Technologies Pvt Ltd",
                        "brightlayer technologies private limited",
                        "India",
                        "https://brightlayer.in",
                        "finance@brightlayer.in",
                        "29ABCDE1234F1Z5",
                        "BrightLayer Technologies Pvt Ltd",
                        "brightlayer technologies private limited",
                        "India",
                        "HDFC0001234",
                        "8821",
                        "Software Services"
                ),
                List.of(new WorkflowStepLogResponse(
                        "INTAKE_AGENT",
                        1,
                        StepStatus.PENDING,
                        "Receives and normalizes vendor submission data.",
                        null,
                        null,
                        null,
                        null,
                        null
                )),
                List.of(),
                List.of(),
                null,
                null,
                null,
                null
        );
    }
}

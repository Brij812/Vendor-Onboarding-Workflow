package com.zamp.vendoronboarding.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamp.vendoronboarding.dto.CreateVendorSubmissionRequest;
import com.zamp.vendoronboarding.dto.CreateVendorSubmissionResponse;
import com.zamp.vendoronboarding.entity.enums.RunStatus;
import com.zamp.vendoronboarding.service.VendorSubmissionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VendorSubmissionController.class)
class VendorSubmissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VendorSubmissionService vendorSubmissionService;

    @Test
    void reviewSubmission_returnsCreatedResponse() throws Exception {
        UUID submissionId = UUID.randomUUID();
        UUID runId = UUID.randomUUID();
        when(vendorSubmissionService.createSubmissionAndInitializeWorkflow(any()))
                .thenReturn(new CreateVendorSubmissionResponse(
                        submissionId,
                        runId,
                        "RUN-0001",
                        RunStatus.RUNNING,
                        "Workflow started. Poll GET /api/workflow-runs/{id} for live progress."
                ));

        CreateVendorSubmissionRequest request = validRequest();
        mockMvc.perform(post("/api/vendor-submissions/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.displayRunId", is("RUN-0001")))
                .andExpect(jsonPath("$.status", is("RUNNING")))
                .andExpect(jsonPath("$.message", is("Workflow started. Poll GET /api/workflow-runs/{id} for live progress.")));
    }

    @Test
    void reviewSubmissionWithDocuments_returnsCreatedResponse() throws Exception {
        UUID submissionId = UUID.randomUUID();
        UUID runId = UUID.randomUUID();
        when(vendorSubmissionService.createSubmissionAndInitializeWorkflow(any(), any()))
                .thenReturn(new CreateVendorSubmissionResponse(
                        submissionId,
                        runId,
                        "RUN-0002",
                        RunStatus.RUNNING,
                        "Workflow started. Poll GET /api/workflow-runs/{id} for live progress."
                ));

        MockMultipartFile taxRegistration = new MockMultipartFile(
                "taxRegistration",
                "tax.pdf",
                "application/pdf",
                "%PDF-1.4".getBytes()
        );

        mockMvc.perform(multipart("/api/vendor-submissions/review")
                        .file(taxRegistration)
                        .param("legalName", "BrightLayer Technologies Pvt Ltd")
                        .param("country", "India")
                        .param("website", "https://brightlayer.in")
                        .param("contactEmail", "finance@brightlayer.in")
                        .param("taxId", "29ABCDE1234F1Z5")
                        .param("bankAccountHolderName", "BrightLayer Technologies Pvt Ltd")
                        .param("bankCountry", "India")
                        .param("bankCode", "HDFC0001234")
                        .param("bankAccountLast4", "8821")
                        .param("businessCategory", "Software Services")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.displayRunId", is("RUN-0002")))
                .andExpect(jsonPath("$.status", is("RUNNING")));
    }

    @Test
    void reviewSubmission_missingRequiredField_returnsBadRequest() throws Exception {
        CreateVendorSubmissionRequest request = new CreateVendorSubmissionRequest(
                "",
                "India",
                "https://brightlayer.in",
                "finance@brightlayer.in",
                "29ABCDE1234F1Z5",
                "BrightLayer Technologies Pvt Ltd",
                "India",
                "HDFC0001234",
                "8821",
                "Software Services"
        );

        mockMvc.perform(post("/api/vendor-submissions/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.errors.legalName", is("legalName is required")));
    }

    private CreateVendorSubmissionRequest validRequest() {
        return new CreateVendorSubmissionRequest(
                "BrightLayer Technologies Pvt Ltd",
                "India",
                "https://brightlayer.in",
                "finance@brightlayer.in",
                "29ABCDE1234F1Z5",
                "BrightLayer Technologies Pvt Ltd",
                "India",
                "HDFC0001234",
                "8821",
                "Software Services"
        );
    }
}

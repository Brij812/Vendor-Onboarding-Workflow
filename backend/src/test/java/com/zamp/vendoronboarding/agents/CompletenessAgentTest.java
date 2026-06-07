package com.zamp.vendoronboarding.agents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamp.vendoronboarding.entity.VendorSubmission;
import com.zamp.vendoronboarding.entity.enums.DocumentType;
import com.zamp.vendoronboarding.entity.enums.IssueSeverity;
import com.zamp.vendoronboarding.entity.enums.StepStatus;
import com.zamp.vendoronboarding.workflow.AgentResult;
import com.zamp.vendoronboarding.workflow.WorkflowContext;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompletenessAgentTest {

    private final CompletenessAgent completenessAgent = new CompletenessAgent(new ObjectMapper());

    @Test
    void execute_allFieldsAndDocumentsPresent_returnsCompleted() throws Exception {
        WorkflowContext context = new WorkflowContext(UUID.randomUUID(), IntakeAgentTest.approvedVendor());
        context.setUploadedDocumentTypes(EnumSet.allOf(DocumentType.class));

        AgentResult result = completenessAgent.execute(context);

        assertEquals(StepStatus.COMPLETED, result.status());
        assertEquals("All required vendor fields and documents are present.", result.summary());
        assertTrue(result.issues().isEmpty());

        JsonNode snapshot = new ObjectMapper().readTree(result.outputSnapshot());
        assertEquals(0, snapshot.get("issueCount").asInt());
        assertTrue(snapshot.get("missingFields").isEmpty());
        assertTrue(snapshot.get("missingDocuments").isEmpty());
    }

    @Test
    void execute_missingTaxIdAndBusinessCategory_returnsWarningWithCodes() {
        VendorSubmission submission = IntakeAgentTest.approvedVendor();
        submission.setTaxId(null);
        submission.setBusinessCategory("");
        WorkflowContext context = new WorkflowContext(UUID.randomUUID(), submission);
        context.setUploadedDocumentTypes(EnumSet.allOf(DocumentType.class));

        AgentResult result = completenessAgent.execute(context);

        assertEquals(StepStatus.WARNING, result.status());
        assertEquals("Found 2 missing required field(s).", result.summary());
        assertEquals(2, result.issues().size());
        assertTrue(result.issues().stream().anyMatch(issue -> "MISSING_TAX_ID".equals(issue.code())));
        assertTrue(result.issues().stream().anyMatch(issue -> "MISSING_BUSINESS_CATEGORY".equals(issue.code())));
        assertEquals(2, context.getIssues().size());
    }

    @Test
    void execute_missingDocuments_returnsDocumentIssueCodes() {
        WorkflowContext context = new WorkflowContext(UUID.randomUUID(), IntakeAgentTest.approvedVendor());

        AgentResult result = completenessAgent.execute(context);

        assertEquals(StepStatus.WARNING, result.status());
        assertEquals("Found 4 missing document(s).", result.summary());
        assertTrue(result.issues().stream().anyMatch(issue ->
                "MISSING_TAX_REGISTRATION_DOCUMENT".equals(issue.code())
                        && issue.severity() == IssueSeverity.MEDIUM));
        assertTrue(result.issues().stream().anyMatch(issue ->
                "MISSING_BANK_PROOF_DOCUMENT".equals(issue.code())));
        assertTrue(result.issues().stream().anyMatch(issue ->
                "MISSING_COMPANY_REGISTRATION_DOCUMENT".equals(issue.code())));
        assertTrue(result.issues().stream().anyMatch(issue ->
                "MISSING_COMPLIANCE_DECLARATION_DOCUMENT".equals(issue.code())));
    }
}

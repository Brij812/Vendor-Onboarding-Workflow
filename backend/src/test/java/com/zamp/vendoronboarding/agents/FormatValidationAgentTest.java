package com.zamp.vendoronboarding.agents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamp.vendoronboarding.entity.VendorSubmission;
import com.zamp.vendoronboarding.entity.enums.IssueSeverity;
import com.zamp.vendoronboarding.entity.enums.StepStatus;
import com.zamp.vendoronboarding.workflow.AgentResult;
import com.zamp.vendoronboarding.workflow.WorkflowContext;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormatValidationAgentTest {

    private final FormatValidationAgent formatValidationAgent = new FormatValidationAgent(new ObjectMapper());

    @Test
    void execute_validApprovedVendor_returnsCompleted() throws Exception {
        WorkflowContext context = new WorkflowContext(UUID.randomUUID(), IntakeAgentTest.approvedVendor());

        AgentResult result = formatValidationAgent.execute(context);

        assertEquals(StepStatus.COMPLETED, result.status());
        assertTrue(result.issues().isEmpty());

        JsonNode snapshot = new ObjectMapper().readTree(result.outputSnapshot());
        assertTrue(snapshot.get("emailValid").asBoolean());
        assertTrue(snapshot.get("taxIdFormatValid").asBoolean());
        assertTrue(snapshot.get("bankCodeFormatValid").asBoolean());
        assertEquals(0, snapshot.get("issueCount").asInt());
    }

    @Test
    void execute_invalidGstin_returnsHighSeverityWarning() {
        VendorSubmission submission = IntakeAgentTest.approvedVendor();
        submission.setTaxId("INVALIDGSTIN");
        WorkflowContext context = new WorkflowContext(UUID.randomUUID(), submission);

        AgentResult result = formatValidationAgent.execute(context);

        assertEquals(StepStatus.WARNING, result.status());
        assertEquals(1, result.issues().size());
        assertEquals("INVALID_GSTIN_FORMAT", result.issues().get(0).code());
        assertEquals(IssueSeverity.HIGH, result.issues().get(0).severity());
    }

    @Test
    void execute_invalidIfscAndBankLast4_returnsWarnings() {
        VendorSubmission submission = IntakeAgentTest.approvedVendor();
        submission.setBankCode("BADCODE");
        submission.setBankAccountLast4("12AB");
        WorkflowContext context = new WorkflowContext(UUID.randomUUID(), submission);

        AgentResult result = formatValidationAgent.execute(context);

        assertEquals(StepStatus.WARNING, result.status());
        assertEquals(2, result.issues().size());
        assertTrue(result.issues().stream().anyMatch(issue -> "INVALID_IFSC_FORMAT".equals(issue.code())));
        assertTrue(result.issues().stream().anyMatch(issue -> "INVALID_BANK_ACCOUNT_LAST4".equals(issue.code())));
    }
}

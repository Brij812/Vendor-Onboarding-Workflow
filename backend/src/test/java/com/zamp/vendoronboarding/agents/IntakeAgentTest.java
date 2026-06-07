package com.zamp.vendoronboarding.agents;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.zamp.vendoronboarding.entity.VendorSubmission;
import com.zamp.vendoronboarding.entity.enums.StepStatus;
import com.zamp.vendoronboarding.service.NormalizationService;
import com.zamp.vendoronboarding.workflow.AgentResult;
import com.zamp.vendoronboarding.workflow.WorkflowContext;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntakeAgentTest {

    private final IntakeAgent intakeAgent = new IntakeAgent(new NormalizationService(), new ObjectMapper());

    @Test
    void execute_normalizesSubmissionAndReturnsCompletedSnapshot() throws Exception {
        VendorSubmission submission = approvedVendor();
        WorkflowContext context = new WorkflowContext(UUID.randomUUID(), submission);

        AgentResult result = intakeAgent.execute(context);

        assertEquals(StepStatus.COMPLETED, result.status());
        assertEquals("Vendor submission received and normalized.", result.summary());
        assertTrue(result.issues().isEmpty());

        JsonNode snapshot = new ObjectMapper().readTree(result.outputSnapshot());
        assertEquals("BrightLayer Technologies Pvt. Ltd.", snapshot.get("legalName").asText());
        assertEquals("brightlayer technologies private limited", snapshot.get("normalizedLegalName").asText());
        assertEquals("India", snapshot.get("country").asText());
        assertEquals("brightlayer technologies private limited", context.getNormalizedLegalName());
    }

    static VendorSubmission approvedVendor() {
        VendorSubmission submission = new VendorSubmission();
        submission.setLegalName("BrightLayer Technologies Pvt. Ltd.");
        submission.setCountry("india");
        submission.setWebsite("https://brightlayer.in");
        submission.setContactEmail("finance@brightlayer.in");
        submission.setTaxId("29ABCDE1234F1Z5");
        submission.setBankAccountHolderName("BrightLayer Technologies Pvt Ltd");
        submission.setBankCountry("INDIA");
        submission.setBankCode("HDFC0001234");
        submission.setBankAccountLast4("8821");
        submission.setBusinessCategory("Software Services");
        return submission;
    }
}

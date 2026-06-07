package com.zamp.vendoronboarding.agents;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamp.vendoronboarding.entity.ExistingVendor;
import com.zamp.vendoronboarding.entity.VendorSubmission;
import com.zamp.vendoronboarding.entity.enums.IssueSeverity;
import com.zamp.vendoronboarding.entity.enums.StepStatus;
import com.zamp.vendoronboarding.entity.enums.VendorStatus;
import com.zamp.vendoronboarding.service.ExistingVendorLookupService;
import com.zamp.vendoronboarding.workflow.AgentResult;
import com.zamp.vendoronboarding.workflow.WorkflowContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DuplicateRiskAgentTest {

    @Mock
    private ExistingVendorLookupService existingVendorLookupService;

    private DuplicateRiskAgent agent;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        agent = new DuplicateRiskAgent(existingVendorLookupService, new ObjectMapper());
    }

    @Test
    void execute_uniqueVendor_returnsCompleted() {
        VendorSubmission submission = uniqueSubmission();
        when(existingVendorLookupService.findAll()).thenReturn(List.of());
        when(existingVendorLookupService.findByTaxId(submission.getTaxId())).thenReturn(Optional.empty());
        when(existingVendorLookupService.findByBankAccountLast4(submission.getBankAccountLast4())).thenReturn(List.of());
        when(existingVendorLookupService.findBlockedVendors()).thenReturn(List.of());
        when(existingVendorLookupService.findActiveVendors()).thenReturn(List.of());

        AgentResult result = agent.execute(new WorkflowContext(UUID.randomUUID(), submission));

        assertEquals(StepStatus.COMPLETED, result.status());
        assertTrue(result.issues().isEmpty());
    }

    @Test
    void execute_duplicateTaxId_returnsCriticalWarning() {
        VendorSubmission submission = uniqueSubmission();
        submission.setTaxId("27PQRSX9876K1Z2");
        ExistingVendor existing = existingVendor("Nova Logistics LLP", "27PQRSX9876K1Z2", "1190", VendorStatus.ACTIVE);

        when(existingVendorLookupService.findAll()).thenReturn(List.of(existing));
        when(existingVendorLookupService.findByTaxId("27PQRSX9876K1Z2")).thenReturn(Optional.of(existing));
        when(existingVendorLookupService.findByBankAccountLast4(submission.getBankAccountLast4())).thenReturn(List.of());
        when(existingVendorLookupService.findBlockedVendors()).thenReturn(List.of());
        when(existingVendorLookupService.findActiveVendors()).thenReturn(List.of(existing));

        AgentResult result = agent.execute(new WorkflowContext(UUID.randomUUID(), submission));

        assertEquals(StepStatus.WARNING, result.status());
        assertTrue(result.issues().stream().anyMatch(issue ->
                "DUPLICATE_TAX_ID".equals(issue.code())
                        && issue.severity() == IssueSeverity.CRITICAL
                        && "existing_vendor_registry".equals(issue.evidenceSource())
                        && issue.evidenceText().contains("Nova Logistics LLP")));
    }

    @Test
    void execute_blockedVendorMatch_returnsCriticalWarning() {
        VendorSubmission submission = uniqueSubmission();
        submission.setTaxId("12BADXX9999Z1Z9");
        submission.setBankAccountLast4("7712");
        ExistingVendor blocked = existingVendor("Blackstone Imports", "12BADXX9999Z1Z9", "7712", VendorStatus.BLOCKED);

        when(existingVendorLookupService.findAll()).thenReturn(List.of(blocked));
        when(existingVendorLookupService.findByTaxId("12BADXX9999Z1Z9")).thenReturn(Optional.of(blocked));
        when(existingVendorLookupService.findByBankAccountLast4("7712")).thenReturn(List.of(blocked));
        when(existingVendorLookupService.findBlockedVendors()).thenReturn(List.of(blocked));
        when(existingVendorLookupService.findActiveVendors()).thenReturn(List.of());

        AgentResult result = agent.execute(new WorkflowContext(UUID.randomUUID(), submission));

        assertEquals(StepStatus.WARNING, result.status());
        assertTrue(result.issues().stream().anyMatch(issue ->
                "BLOCKED_VENDOR_MATCH".equals(issue.code()) && issue.severity() == IssueSeverity.CRITICAL));
    }

    private VendorSubmission uniqueSubmission() {
        VendorSubmission submission = IntakeAgentTest.approvedVendor();
        submission.setTaxId("29NEWAA1234F1Z5");
        submission.setBankAccountLast4("9834");
        submission.setNormalizedLegalName("brightlayer technologies private limited");
        return submission;
    }

    private ExistingVendor existingVendor(String legalName, String taxId, String last4, VendorStatus status) {
        ExistingVendor vendor = new ExistingVendor();
        vendor.setId(UUID.randomUUID());
        vendor.setLegalName(legalName);
        vendor.setNormalizedLegalName(legalName.toLowerCase());
        vendor.setCountry("India");
        vendor.setTaxId(taxId);
        vendor.setBankAccountLast4(last4);
        vendor.setStatus(status);
        return vendor;
    }
}

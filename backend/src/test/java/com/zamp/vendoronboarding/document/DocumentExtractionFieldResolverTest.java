package com.zamp.vendoronboarding.document;

import com.zamp.vendoronboarding.entity.DocumentExtraction;
import com.zamp.vendoronboarding.entity.enums.DocumentType;
import com.zamp.vendoronboarding.entity.enums.ExtractionMethod;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DocumentExtractionFieldResolverTest {

    @Test
    void resolveTaxId_prefersTaxRegistrationDocument() {
        DocumentExtraction taxDoc = extraction(DocumentType.TAX_REGISTRATION, null, "TAX-123", null, null);
        DocumentExtraction bankDoc = extraction(DocumentType.BANK_PROOF, null, "BANK-999", null, null);

        assertEquals("TAX-123", DocumentExtractionFieldResolver.resolveTaxId(List.of(bankDoc, taxDoc)));
    }

    @Test
    void resolveBankAccountHolderName_usesBankProofDocument() {
        DocumentExtraction bankDoc = extraction(DocumentType.BANK_PROOF, null, null, "BrightLayer Technologies Pvt Ltd", null);
        DocumentExtraction taxDoc = extraction(DocumentType.TAX_REGISTRATION, null, null, "Other Name", null);

        assertEquals("BrightLayer Technologies Pvt Ltd",
                DocumentExtractionFieldResolver.resolveBankAccountHolderName(List.of(taxDoc, bankDoc)));
    }

    @Test
    void resolveLegalEntityName_prefersCompanyRegistrationThenTaxRegistration() {
        DocumentExtraction taxDoc = extraction(DocumentType.TAX_REGISTRATION, "Tax Name", null, null, null);
        DocumentExtraction companyDoc = extraction(DocumentType.COMPANY_REGISTRATION, "Company Name", null, null, null);

        assertEquals("Company Name",
                DocumentExtractionFieldResolver.resolveLegalEntityName(List.of(taxDoc, companyDoc)));
    }

    @Test
    void resolveCountry_returnsFirstNonNullLlmValue() {
        DocumentExtraction extraction = extraction(DocumentType.COMPLIANCE_DECLARATION, null, null, null, "India");

        assertEquals("India", DocumentExtractionFieldResolver.resolveCountry(List.of(extraction)));
    }

    @Test
    void resolveTaxId_ignoresFallbackRows() {
        DocumentExtraction fallback = extraction(DocumentType.TAX_REGISTRATION, null, "TAX-123", null, null);
        fallback.setExtractionMethod(ExtractionMethod.FALLBACK);

        assertNull(DocumentExtractionFieldResolver.resolveTaxId(List.of(fallback)));
    }

    private DocumentExtraction extraction(DocumentType documentType,
                                          String legalEntityName,
                                          String taxId,
                                          String bankAccountHolderName,
                                          String country) {
        DocumentExtraction extraction = new DocumentExtraction();
        extraction.setExtractionMethod(ExtractionMethod.LLM);
        extraction.setDocumentType(documentType);
        extraction.setLegalEntityName(legalEntityName);
        extraction.setTaxId(taxId);
        extraction.setBankAccountHolderName(bankAccountHolderName);
        extraction.setCountry(country);
        return extraction;
    }
}

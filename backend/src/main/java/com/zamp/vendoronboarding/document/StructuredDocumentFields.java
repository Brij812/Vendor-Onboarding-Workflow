package com.zamp.vendoronboarding.document;

import com.zamp.vendoronboarding.entity.enums.DocumentType;

import java.time.LocalDate;

public record StructuredDocumentFields(
        String legalEntityName,
        String taxId,
        String bankAccountHolderName,
        String country,
        DocumentType documentType,
        LocalDate documentDate,
        Double confidenceScore
) {
}

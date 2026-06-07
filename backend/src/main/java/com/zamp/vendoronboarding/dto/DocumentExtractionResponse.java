package com.zamp.vendoronboarding.dto;

import com.zamp.vendoronboarding.entity.enums.DocumentType;
import com.zamp.vendoronboarding.entity.enums.ExtractionMethod;

import java.time.LocalDate;

public record DocumentExtractionResponse(
        ExtractionMethod extractionMethod,
        DocumentType documentType,
        String legalEntityName,
        String taxId,
        String bankAccountHolderName,
        String country,
        LocalDate documentDate,
        Double confidenceScore
) {
}

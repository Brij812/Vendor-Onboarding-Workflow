package com.zamp.vendoronboarding.document;

import com.zamp.vendoronboarding.entity.DocumentExtraction;
import com.zamp.vendoronboarding.entity.enums.DocumentType;
import com.zamp.vendoronboarding.entity.enums.ExtractionMethod;

import java.util.List;
import java.util.function.Function;

public final class DocumentExtractionFieldResolver {

    private DocumentExtractionFieldResolver() {
    }

    public static String resolveLegalEntityName(List<DocumentExtraction> extractions) {
        String value = resolveByDocumentType(extractions, DocumentType.COMPANY_REGISTRATION,
                DocumentExtraction::getLegalEntityName);
        if (value != null) {
            return value;
        }
        return resolveByDocumentType(extractions, DocumentType.TAX_REGISTRATION,
                DocumentExtraction::getLegalEntityName);
    }

    public static String resolveTaxId(List<DocumentExtraction> extractions) {
        String value = resolveByDocumentType(extractions, DocumentType.TAX_REGISTRATION,
                DocumentExtraction::getTaxId);
        if (value != null) {
            return value;
        }
        return firstNonNull(extractions, DocumentExtraction::getTaxId);
    }

    public static String resolveBankAccountHolderName(List<DocumentExtraction> extractions) {
        return resolveByDocumentType(extractions, DocumentType.BANK_PROOF,
                DocumentExtraction::getBankAccountHolderName);
    }

    public static String resolveCountry(List<DocumentExtraction> extractions) {
        return firstNonNull(extractions, DocumentExtraction::getCountry);
    }

    public static boolean hasStructuredData(List<DocumentExtraction> extractions) {
        if (extractions == null || extractions.isEmpty()) {
            return false;
        }
        return extractions.stream().anyMatch(DocumentExtractionFieldResolver::hasStructuredFields);
    }

    private static boolean hasStructuredFields(DocumentExtraction extraction) {
        if (extraction.getExtractionMethod() != ExtractionMethod.LLM) {
            return false;
        }
        return extraction.getLegalEntityName() != null
                || extraction.getTaxId() != null
                || extraction.getBankAccountHolderName() != null
                || extraction.getCountry() != null;
    }

    private static String resolveByDocumentType(List<DocumentExtraction> extractions,
                                                  DocumentType documentType,
                                                  Function<DocumentExtraction, String> getter) {
        if (extractions == null) {
            return null;
        }
        for (DocumentExtraction extraction : extractions) {
            if (extraction.getExtractionMethod() != ExtractionMethod.LLM) {
                continue;
            }
            if (extraction.getDocumentType() == documentType) {
                String value = getter.apply(extraction);
                if (value != null && !value.isBlank()) {
                    return value.trim();
                }
            }
        }
        return null;
    }

    private static String firstNonNull(List<DocumentExtraction> extractions,
                                       Function<DocumentExtraction, String> getter) {
        if (extractions == null) {
            return null;
        }
        for (DocumentExtraction extraction : extractions) {
            if (extraction.getExtractionMethod() != ExtractionMethod.LLM) {
                continue;
            }
            String value = getter.apply(extraction);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}

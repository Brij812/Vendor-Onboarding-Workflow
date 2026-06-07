package com.zamp.vendoronboarding.service;

import com.zamp.vendoronboarding.entity.VendorSubmission;
import com.zamp.vendoronboarding.util.NormalizationRules;
import org.springframework.stereotype.Service;

@Service
public class NormalizationService {

    public String normalizeName(String value) {
        return NormalizationRules.normalizeName(value);
    }

    public String normalizeCountry(String value) {
        return NormalizationRules.normalizeCountry(value);
    }

    public void applyToSubmission(VendorSubmission submission) {
        submission.setLegalName(trimToNull(submission.getLegalName()));
        submission.setBankAccountHolderName(trimToNull(submission.getBankAccountHolderName()));
        submission.setCountry(normalizeCountry(submission.getCountry()));
        submission.setBankCountry(normalizeCountry(submission.getBankCountry()));
        submission.setNormalizedLegalName(normalizeName(submission.getLegalName()));
        submission.setNormalizedBankAccountHolderName(normalizeName(submission.getBankAccountHolderName()));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

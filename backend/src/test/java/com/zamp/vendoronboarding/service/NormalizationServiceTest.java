package com.zamp.vendoronboarding.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NormalizationServiceTest {

    private final NormalizationService normalizationService = new NormalizationService();

    @Test
    void normalizeName_expandsPvtLtdAndRemovesPunctuation() {
        String result = normalizationService.normalizeName("BrightLayer Technologies Pvt. Ltd.");
        assertEquals("brightlayer technologies private limited", result);
    }

    @Test
    void normalizeCountry_titleCasesIndia() {
        assertEquals("India", normalizationService.normalizeCountry("india"));
        assertEquals("India", normalizationService.normalizeCountry("INDIA"));
        assertEquals("India", normalizationService.normalizeCountry("India"));
    }
}

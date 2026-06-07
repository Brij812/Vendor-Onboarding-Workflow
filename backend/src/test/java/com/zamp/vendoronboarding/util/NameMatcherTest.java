package com.zamp.vendoronboarding.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NameMatcherTest {

    @Test
    void namesConsistent_exactNormalizedMatch() {
        assertTrue(NameMatcher.namesConsistent(
                "brightlayer technologies private limited",
                "brightlayer technologies private limited"
        ));
    }

    @Test
    void namesConsistent_strongContainmentPasses() {
        assertTrue(NameMatcher.namesConsistent(
                "brightlayer technologies private limited",
                "brightlayer technologies"
        ));
    }

    @Test
    void namesConsistent_acmeMismatchFails() {
        assertFalse(NameMatcher.namesConsistent(
                "acme cloud services private limited",
                "acme consulting llp"
        ));
    }

    @Test
    void isSimilarNormalizedName_detectsSimilarNames() {
        assertTrue(NameMatcher.isSimilarNormalizedName(
                "nova logistics llp",
                "nova logistics"
        ));
    }
}

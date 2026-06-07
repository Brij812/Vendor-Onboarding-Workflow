package com.zamp.vendoronboarding.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainUtilsTest {

    @Test
    void extractEmailDomain_returnsHost() {
        assertEquals("brightlayer.in", DomainUtils.extractEmailDomain("finance@brightlayer.in"));
    }

    @Test
    void extractWebsiteDomain_stripsSchemeAndWww() {
        assertEquals("brightlayer.in", DomainUtils.extractWebsiteDomain("https://www.brightlayer.in"));
    }

    @Test
    void isPublicEmailDomain_detectsGmail() {
        assertTrue(DomainUtils.isPublicEmailDomain("gmail.com"));
        assertFalse(DomainUtils.isPublicEmailDomain("brightlayer.in"));
    }

    @Test
    void domainsMatch_comparesBusinessDomains() {
        assertTrue(DomainUtils.domainsMatch("finance.brightlayer.in", "brightlayer.in"));
        assertFalse(DomainUtils.domainsMatch("gmail.com", "brightlayer.in"));
    }
}

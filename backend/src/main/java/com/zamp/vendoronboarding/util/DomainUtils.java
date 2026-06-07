package com.zamp.vendoronboarding.util;

import java.net.URI;
import java.util.Set;

public final class DomainUtils {

    private static final Set<String> PUBLIC_EMAIL_DOMAINS = Set.of(
            "gmail.com",
            "yahoo.com",
            "outlook.com",
            "hotmail.com",
            "proton.me"
    );

    private DomainUtils() {
    }

    public static String extractEmailDomain(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            return null;
        }
        String domain = email.substring(email.indexOf('@') + 1).trim().toLowerCase();
        return domain.isEmpty() ? null : domain;
    }

    public static String extractWebsiteDomain(String website) {
        if (website == null || website.isBlank()) {
            return null;
        }

        String trimmed = website.trim();
        try {
            if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
                trimmed = "https://" + trimmed;
            }
            URI uri = URI.create(trimmed);
            String host = uri.getHost();
            if (host == null) {
                return null;
            }
            host = host.toLowerCase();
            if (host.startsWith("www.")) {
                host = host.substring(4);
            }
            return host;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public static boolean isPublicEmailDomain(String domain) {
        return domain != null && PUBLIC_EMAIL_DOMAINS.contains(domain.toLowerCase());
    }

    public static boolean domainsMatch(String emailDomain, String websiteDomain) {
        if (emailDomain == null || websiteDomain == null) {
            return false;
        }

        String email = emailDomain.toLowerCase();
        String website = websiteDomain.toLowerCase();

        if (email.equals(website)) {
            return true;
        }

        return email.endsWith("." + website) || website.endsWith("." + email);
    }
}

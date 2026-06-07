package com.zamp.vendoronboarding.util;

public final class NormalizationRules {

    private NormalizationRules() {
    }

    public static String normalizeName(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim()
                .toLowerCase()
                .replace('.', ' ')
                .replace(',', ' ')
                .replaceAll("\\s+", " ")
                .trim();

        normalized = normalized.replace("pvt ltd", "private limited");
        normalized = normalized.replace("pvt  ltd", "private limited");
        normalized = normalized.replaceAll("\\bpvt\\b", "private");
        normalized = normalized.replaceAll("\\bltd\\b", "limited");
        normalized = normalized.replaceAll("\\bco\\b", "company");
        normalized = normalized.replaceAll("\\s+", " ").trim();

        return normalized;
    }

    public static String normalizeCountry(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String trimmed = value.trim();
        if (!trimmed.matches("[A-Za-z\\s]+")) {
            return trimmed;
        }

        String lower = trimmed.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}

package com.zamp.vendoronboarding.util;

public final class FormatValidators {

    private static final String EMAIL_PATTERN = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";
    private static final String BANK_LAST4_PATTERN = "^\\d{4}$";
    private static final String GSTIN_PATTERN = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$";
    private static final String IFSC_PATTERN = "^[A-Z]{4}0[A-Z0-9]{6}$";

    private FormatValidators() {
    }

    public static boolean isValidEmail(String value) {
        return value != null && !value.isBlank() && value.trim().matches(EMAIL_PATTERN);
    }

    public static boolean isValidWebsite(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String trimmed = value.trim();
        return trimmed.startsWith("http://") || trimmed.startsWith("https://");
    }

    public static boolean isValidBankAccountLast4(String value) {
        return value != null && !value.isBlank() && value.trim().matches(BANK_LAST4_PATTERN);
    }

    public static boolean isValidGstin(String value) {
        return value != null && !value.isBlank() && value.trim().toUpperCase().matches(GSTIN_PATTERN);
    }

    public static boolean isValidIfsc(String value) {
        return value != null && !value.isBlank() && value.trim().toUpperCase().matches(IFSC_PATTERN);
    }

    public static boolean isIndia(String country) {
        return country != null && "India".equalsIgnoreCase(country.trim());
    }
}

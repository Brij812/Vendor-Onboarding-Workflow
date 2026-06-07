package com.zamp.vendoronboarding.util;

public final class NameNormalizer {

    private NameNormalizer() {
    }

    public static String normalize(String value) {
        return NormalizationRules.normalizeName(value);
    }
}

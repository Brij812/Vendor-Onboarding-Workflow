package com.zamp.vendoronboarding.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class NameMatcher {

    private NameMatcher() {
    }

    public static boolean namesConsistent(String normalizedLegal, String normalizedBank) {
        if (isBlank(normalizedLegal) || isBlank(normalizedBank)) {
            return true;
        }

        String legal = normalizedLegal.trim();
        String bank = normalizedBank.trim();

        if (legal.equals(bank)) {
            return true;
        }

        String shorter = legal.length() <= bank.length() ? legal : bank;
        String longer = legal.length() <= bank.length() ? bank : legal;
        int shorterTokenCount = tokenCount(shorter);

        if (shorterTokenCount >= 2 && longer.contains(shorter)) {
            return true;
        }

        return false;
    }

    public static boolean isSimilarNormalizedName(String first, String second) {
        if (isBlank(first) || isBlank(second)) {
            return false;
        }

        String a = first.trim();
        String b = second.trim();

        if (a.equals(b)) {
            return true;
        }

        if (a.contains(b) || b.contains(a)) {
            return true;
        }

        return tokenOverlapRatio(a, b) >= 0.6;
    }

    private static double tokenOverlapRatio(String first, String second) {
        Set<String> firstTokens = new HashSet<>(Arrays.asList(first.split("\\s+")));
        Set<String> secondTokens = new HashSet<>(Arrays.asList(second.split("\\s+")));
        firstTokens.remove("");
        secondTokens.remove("");

        if (firstTokens.isEmpty() || secondTokens.isEmpty()) {
            return 0.0;
        }

        long overlap = firstTokens.stream().filter(secondTokens::contains).count();
        int smallerSize = Math.min(firstTokens.size(), secondTokens.size());
        return (double) overlap / smallerSize;
    }

    private static int tokenCount(String value) {
        if (isBlank(value)) {
            return 0;
        }
        return (int) Arrays.stream(value.trim().split("\\s+"))
                .filter(token -> !token.isBlank())
                .count();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

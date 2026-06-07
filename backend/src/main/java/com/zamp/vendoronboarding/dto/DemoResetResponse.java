package com.zamp.vendoronboarding.dto;

public record DemoResetResponse(
        long deletedRuns,
        long deletedSubmissions,
        long deletedFiles
) {
}

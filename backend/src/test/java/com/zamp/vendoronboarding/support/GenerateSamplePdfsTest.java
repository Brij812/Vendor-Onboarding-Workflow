package com.zamp.vendoronboarding.support;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

class GenerateSamplePdfsTest {

    @Test
    void generateSamplePdfsForFrontend() throws Exception {
        Path targetDirectory = Paths.get("..", "frontend", "public", "samples").normalize().toAbsolutePath();
        SamplePdfGenerator.writeSamplePdfs(targetDirectory);
    }
}

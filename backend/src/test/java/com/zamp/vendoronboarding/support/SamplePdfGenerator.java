package com.zamp.vendoronboarding.support;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Generates minimal sample PDFs for frontend demo scenarios.
 */
public final class SamplePdfGenerator {

    private SamplePdfGenerator() {
    }

    public static void writeSamplePdfs(Path targetDirectory) throws Exception {
        Files.createDirectories(targetDirectory);

        writePdf(
                targetDirectory.resolve("tax-registration.pdf"),
                "Tax Registration Certificate",
                "Legal Entity: Nexora Grid Solutions Pvt Ltd",
                "Tax ID: 29NEXOR5678P1Z2",
                "Country: India"
        );
        writePdf(
                targetDirectory.resolve("bank-proof.pdf"),
                "Bank Account Proof",
                "Account Holder: Nexora Grid Solutions Pvt Ltd",
                "Bank Country: India",
                "Account Last 4: 8473"
        );
        writePdf(
                targetDirectory.resolve("company-registration.pdf"),
                "Company Registration Certificate",
                "Registered Name: Nexora Grid Solutions Pvt Ltd",
                "Country of Registration: India"
        );
        writePdf(
                targetDirectory.resolve("compliance-declaration.pdf"),
                "Compliance Declaration",
                "Declarant: Nexora Grid Solutions Pvt Ltd",
                "Country: India",
                "Tax ID: 29NEXOR5678P1Z2"
        );
    }

    private static void writePdf(Path targetPath, String... lines) throws Exception {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(72, 720);
                for (String line : lines) {
                    contentStream.showText(line);
                    contentStream.newLineAtOffset(0, -18);
                }
                contentStream.endText();
            }
            document.save(targetPath.toFile());
        }
    }
}

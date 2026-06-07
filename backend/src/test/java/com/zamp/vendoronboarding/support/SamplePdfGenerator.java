package com.zamp.vendoronboarding.support;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Generates minimal sample PDFs for frontend demo scenarios.
 */
public final class SamplePdfGenerator {

    public record SampleVendorDocuments(
            String legalName,
            String taxId,
            String bankAccountHolderName,
            String bankAccountLast4,
            String country
    ) {
    }

    public static final SampleVendorDocuments NEXORA = new SampleVendorDocuments(
            "Nexora Grid Solutions Pvt Ltd",
            "29NEXOR5678P1Z2",
            "Nexora Grid Solutions Pvt Ltd",
            "8473",
            "India"
    );

    public static final SampleVendorDocuments BRIGHTLAYER = new SampleVendorDocuments(
            "BrightLayer Technologies Pvt Ltd",
            "29ABCDE1234F1Z5",
            "BrightLayer Technologies Pvt Ltd",
            "4567",
            "India"
    );

    public static final SampleVendorDocuments HELIX_BANK_MISMATCH = new SampleVendorDocuments(
            "Helix Data Systems Pvt Ltd",
            "29HELIX5678P1Z3",
            "Helix Data Services",
            "9834",
            "India"
    );

    public static final SampleVendorDocuments BLACKSTONE = new SampleVendorDocuments(
            "Blackstone Imports",
            "12BADXX9999Z1Z9",
            "Blackstone Imports",
            "8834",
            "India"
    );

    private static final List<SampleSet> SAMPLE_SETS = List.of(
            new SampleSet("nexora", NEXORA),
            new SampleSet("brightlayer", BRIGHTLAYER),
            new SampleSet("helix-bank-mismatch", HELIX_BANK_MISMATCH),
            new SampleSet("blackstone", BLACKSTONE)
    );

    private record SampleSet(String folderName, SampleVendorDocuments documents) {
    }

    private SamplePdfGenerator() {
    }

    public static void writeSamplePdfs(Path targetDirectory) throws Exception {
        Files.createDirectories(targetDirectory);
        for (SampleSet sampleSet : SAMPLE_SETS) {
            writeVendorSamplePdfs(targetDirectory.resolve(sampleSet.folderName()), sampleSet.documents());
        }
    }

    public static void writeVendorSamplePdfs(Path targetDirectory, SampleVendorDocuments vendor) throws Exception {
        Files.createDirectories(targetDirectory);

        writePdf(
                targetDirectory.resolve("tax-registration.pdf"),
                "Tax Registration Certificate",
                "Legal Entity: " + vendor.legalName(),
                "Tax ID: " + vendor.taxId(),
                "Country: " + vendor.country()
        );
        writePdf(
                targetDirectory.resolve("bank-proof.pdf"),
                "Bank Account Proof",
                "Account Holder: " + vendor.bankAccountHolderName(),
                "Bank Country: " + vendor.country(),
                "Account Last 4: " + vendor.bankAccountLast4()
        );
        writePdf(
                targetDirectory.resolve("company-registration.pdf"),
                "Company Registration Certificate",
                "Registered Name: " + vendor.legalName(),
                "Country of Registration: " + vendor.country()
        );
        writePdf(
                targetDirectory.resolve("compliance-declaration.pdf"),
                "Compliance Declaration",
                "Declarant: " + vendor.legalName(),
                "Country: " + vendor.country(),
                "Tax ID: " + vendor.taxId()
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

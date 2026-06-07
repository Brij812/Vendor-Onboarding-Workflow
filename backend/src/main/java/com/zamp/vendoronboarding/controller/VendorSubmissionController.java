package com.zamp.vendoronboarding.controller;

import com.zamp.vendoronboarding.dto.CreateVendorSubmissionMultipartRequest;
import com.zamp.vendoronboarding.dto.CreateVendorSubmissionRequest;
import com.zamp.vendoronboarding.dto.CreateVendorSubmissionResponse;
import com.zamp.vendoronboarding.service.VendorSubmissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/vendor-submissions")
public class VendorSubmissionController {

    private final VendorSubmissionService vendorSubmissionService;

    public VendorSubmissionController(VendorSubmissionService vendorSubmissionService) {
        this.vendorSubmissionService = vendorSubmissionService;
    }

    @PostMapping(value = "/review", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CreateVendorSubmissionResponse reviewSubmission(
            @Valid @RequestBody CreateVendorSubmissionRequest request) {
        return vendorSubmissionService.createSubmissionAndInitializeWorkflow(request);
    }

    @PostMapping(value = "/review", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CreateVendorSubmissionResponse reviewSubmissionWithDocuments(
            @Valid @ModelAttribute CreateVendorSubmissionMultipartRequest request,
            @RequestPart(value = "taxRegistration", required = false) MultipartFile taxRegistration,
            @RequestPart(value = "bankProof", required = false) MultipartFile bankProof,
            @RequestPart(value = "companyRegistration", required = false) MultipartFile companyRegistration,
            @RequestPart(value = "complianceDeclaration", required = false) MultipartFile complianceDeclaration) {
        return vendorSubmissionService.createSubmissionAndInitializeWorkflow(
                request.toJsonRequest(),
                vendorSubmissionService.mapDocumentUploads(
                        taxRegistration,
                        bankProof,
                        companyRegistration,
                        complianceDeclaration
                )
        );
    }
}

package com.zamp.vendoronboarding.controller;

import com.zamp.vendoronboarding.dto.ExistingVendorResponse;
import com.zamp.vendoronboarding.service.ExistingVendorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/existing-vendors")
public class ExistingVendorController {

    private final ExistingVendorService existingVendorService;

    public ExistingVendorController(ExistingVendorService existingVendorService) {
        this.existingVendorService = existingVendorService;
    }

    @GetMapping
    public List<ExistingVendorResponse> listExistingVendors() {
        return existingVendorService.findAll();
    }
}

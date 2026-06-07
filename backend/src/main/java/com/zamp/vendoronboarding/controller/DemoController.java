package com.zamp.vendoronboarding.controller;

import com.zamp.vendoronboarding.dto.DemoReseedResponse;
import com.zamp.vendoronboarding.dto.DemoResetResponse;
import com.zamp.vendoronboarding.service.DemoResetService;
import com.zamp.vendoronboarding.service.ExistingVendorSeedService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Dev/demo-only endpoints for resetting workflow data and reseeding existing vendors.
 */
@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final DemoResetService demoResetService;
    private final ExistingVendorSeedService existingVendorSeedService;
    private final boolean demoEnabled;

    public DemoController(DemoResetService demoResetService,
                          ExistingVendorSeedService existingVendorSeedService,
                          @Value("${app.demo.enabled:true}") boolean demoEnabled) {
        this.demoResetService = demoResetService;
        this.existingVendorSeedService = existingVendorSeedService;
        this.demoEnabled = demoEnabled;
    }

    @PostMapping("/reset-runs")
    public DemoResetResponse resetRuns() {
        ensureEnabled();
        return demoResetService.resetAllRuns();
    }

    @PostMapping("/reseed-existing-vendors")
    public DemoReseedResponse reseedExistingVendors() {
        ensureEnabled();
        long seeded = existingVendorSeedService.reseedAll();
        return new DemoReseedResponse(seeded);
    }

    private void ensureEnabled() {
        if (!demoEnabled) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Demo endpoints are disabled.");
        }
    }
}

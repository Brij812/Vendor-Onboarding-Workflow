package com.zamp.vendoronboarding.controller;

import com.zamp.vendoronboarding.dto.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api")
public class HealthController {

    private static final String APP_NAME = "Vendor Onboarding AI Workflow Engine";

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse(APP_NAME, "UP", Instant.now());
    }
}

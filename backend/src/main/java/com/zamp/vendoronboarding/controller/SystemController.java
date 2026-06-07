package com.zamp.vendoronboarding.controller;

import com.zamp.vendoronboarding.dto.DatabaseStatusResponse;
import com.zamp.vendoronboarding.service.DatabaseStatusService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    private final DatabaseStatusService databaseStatusService;

    public SystemController(DatabaseStatusService databaseStatusService) {
        this.databaseStatusService = databaseStatusService;
    }

    @GetMapping("/database-status")
    public DatabaseStatusResponse databaseStatus() {
        return databaseStatusService.getStatus();
    }
}

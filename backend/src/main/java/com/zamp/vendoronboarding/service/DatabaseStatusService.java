package com.zamp.vendoronboarding.service;

import com.zamp.vendoronboarding.dto.DatabaseStatusResponse;
import com.zamp.vendoronboarding.repository.ExistingVendorRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class DatabaseStatusService {

    private final JdbcTemplate jdbcTemplate;
    private final ExistingVendorRepository existingVendorRepository;

    public DatabaseStatusService(JdbcTemplate jdbcTemplate,
                                 ExistingVendorRepository existingVendorRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.existingVendorRepository = existingVendorRepository;
    }

    public DatabaseStatusResponse getStatus() {
        boolean connected = isDatabaseConnected();
        long vendorCount = connected ? existingVendorRepository.count() : 0L;
        return new DatabaseStatusResponse(connected, vendorCount, Instant.now());
    }

    private boolean isDatabaseConnected() {
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return result != null && result == 1;
        } catch (Exception ex) {
            return false;
        }
    }
}

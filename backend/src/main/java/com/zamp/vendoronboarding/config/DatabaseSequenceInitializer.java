package com.zamp.vendoronboarding.config;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSequenceInitializer {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSequenceInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    void ensureRunNumberSequence() {
        jdbcTemplate.execute(
                "CREATE SEQUENCE IF NOT EXISTS workflow_run_number_seq START WITH 1 INCREMENT BY 1"
        );
    }
}

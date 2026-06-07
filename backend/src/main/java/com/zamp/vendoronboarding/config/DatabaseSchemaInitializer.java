package com.zamp.vendoronboarding.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Applies lightweight schema fixes that Hibernate ddl-auto does not handle,
 * such as updating PostgreSQL check constraints when enum values are extended.
 */
@Component
public class DatabaseSchemaInitializer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSchemaInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    void applySchemaFixes() {
        updateWorkflowStepLogStatusConstraint();
    }

    private void updateWorkflowStepLogStatusConstraint() {
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE workflow_step_logs DROP CONSTRAINT IF EXISTS workflow_step_logs_status_check"
            );
            jdbcTemplate.execute("""
                    ALTER TABLE workflow_step_logs
                    ADD CONSTRAINT workflow_step_logs_status_check
                    CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'WARNING', 'FAILED', 'SKIPPED'))
                    """);
            log.info("Updated workflow_step_logs status check constraint to include SKIPPED");
        } catch (Exception ex) {
            log.warn("Could not update workflow_step_logs status constraint: {}", ex.getMessage());
        }
    }
}

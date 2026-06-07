package com.zamp.vendoronboarding.service;

import com.zamp.vendoronboarding.entity.WorkflowRun;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class WorkflowRunIdGenerator {

    private final JdbcTemplate jdbcTemplate;

    public WorkflowRunIdGenerator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long nextRunNumber() {
        Long value = jdbcTemplate.queryForObject(
                "SELECT nextval('workflow_run_number_seq')",
                Long.class
        );
        return value != null ? value : 1L;
    }

    public void assignRunIdentifiers(WorkflowRun run) {
        long runNumber = nextRunNumber();
        run.setRunNumber(runNumber);
        run.setDisplayRunId(String.format("RUN-%04d", runNumber));
    }
}

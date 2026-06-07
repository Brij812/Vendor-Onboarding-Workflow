package com.zamp.vendoronboarding.controller;

import com.zamp.vendoronboarding.dto.DatabaseStatusResponse;
import com.zamp.vendoronboarding.service.DatabaseStatusService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SystemController.class)
class SystemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DatabaseStatusService databaseStatusService;

    @Test
    void databaseStatus_returnsConnectedStatus() throws Exception {
        when(databaseStatusService.getStatus()).thenReturn(
                new DatabaseStatusResponse(true, 4L, Instant.parse("2026-06-04T12:00:00Z"))
        );

        mockMvc.perform(get("/api/system/database-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.databaseConnected", is(true)))
                .andExpect(jsonPath("$.existingVendorCount", is(4)))
                .andExpect(jsonPath("$.timestamp", is("2026-06-04T12:00:00Z")));
    }
}

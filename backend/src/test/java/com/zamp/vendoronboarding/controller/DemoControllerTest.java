package com.zamp.vendoronboarding.controller;

import com.zamp.vendoronboarding.dto.DemoReseedResponse;
import com.zamp.vendoronboarding.dto.DemoResetResponse;
import com.zamp.vendoronboarding.service.DemoResetService;
import com.zamp.vendoronboarding.service.ExistingVendorSeedService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DemoController.class)
class DemoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DemoResetService demoResetService;

    @MockBean
    private ExistingVendorSeedService existingVendorSeedService;

    @Test
    void resetRuns_returnsCounts() throws Exception {
        when(demoResetService.resetAllRuns()).thenReturn(new DemoResetResponse(3, 3, 5));

        mockMvc.perform(post("/api/demo/reset-runs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deletedRuns", is(3)))
                .andExpect(jsonPath("$.deletedSubmissions", is(3)))
                .andExpect(jsonPath("$.deletedFiles", is(5)));
    }

    @Test
    void reseedExistingVendors_returnsSeededCount() throws Exception {
        when(existingVendorSeedService.reseedAll()).thenReturn(4L);

        mockMvc.perform(post("/api/demo/reseed-existing-vendors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seededVendors", is(4)));
    }
}

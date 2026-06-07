package com.zamp.vendoronboarding.controller;

import com.zamp.vendoronboarding.service.DemoResetService;
import com.zamp.vendoronboarding.service.ExistingVendorSeedService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DemoController.class)
@TestPropertySource(properties = "app.demo.enabled=false")
class DemoControllerDisabledTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DemoResetService demoResetService;

    @MockBean
    private ExistingVendorSeedService existingVendorSeedService;

    @Test
    void resetRuns_whenDisabled_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/demo/reset-runs"))
                .andExpect(status().isForbidden());
    }

    @Test
    void reseedExistingVendors_whenDisabled_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/demo/reseed-existing-vendors"))
                .andExpect(status().isForbidden());
    }
}

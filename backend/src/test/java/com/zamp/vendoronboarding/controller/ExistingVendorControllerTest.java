package com.zamp.vendoronboarding.controller;

import com.zamp.vendoronboarding.dto.ExistingVendorResponse;
import com.zamp.vendoronboarding.entity.enums.VendorStatus;
import com.zamp.vendoronboarding.service.ExistingVendorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExistingVendorController.class)
class ExistingVendorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExistingVendorService existingVendorService;

    @Test
    void listExistingVendors_returnsSeededVendor() throws Exception {
        UUID id = UUID.randomUUID();
        when(existingVendorService.findAll()).thenReturn(List.of(
                new ExistingVendorResponse(
                        id,
                        "BrightLayer Technologies Pvt Ltd",
                        "India",
                        "29ABCDE1234F1Z5",
                        "8821",
                        VendorStatus.ACTIVE,
                        null
                )
        ));

        mockMvc.perform(get("/api/existing-vendors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].legalName", is("BrightLayer Technologies Pvt Ltd")))
                .andExpect(jsonPath("$[0].status", is("ACTIVE")));
    }
}

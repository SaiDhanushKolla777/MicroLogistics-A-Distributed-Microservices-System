package com.micrologistics.dashboard.controller;

import com.micrologistics.dashboard.dto.DashboardDto;
import com.micrologistics.dashboard.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DashboardControllerTest {

    private DashboardService dashboardService;
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        dashboardService = mock(DashboardService.class);
        DashboardController controller = new DashboardController(dashboardService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getDashboard_ReturnsOk() throws Exception {
        when(dashboardService.getCurrentDashboard()).thenReturn(DashboardDto.builder().build());

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk());
    }
}

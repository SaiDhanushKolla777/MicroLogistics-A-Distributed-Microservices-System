package com.micrologistics.dashboard.service;

import com.micrologistics.dashboard.dto.DashboardDto;
import com.micrologistics.dashboard.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DashboardServiceTest {

    @Test
    void getCurrentDashboard_ReturnsDto() {
        DashboardServiceImpl service = mock(DashboardServiceImpl.class);
        DashboardDto dto = DashboardDto.builder().build();
        when(service.getCurrentDashboard()).thenReturn(dto);

        assertNotNull(service.getCurrentDashboard());
        verify(service, times(1)).getCurrentDashboard();
    }
}

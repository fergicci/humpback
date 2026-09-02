package studio.humpback.backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import studio.humpback.backend.model.DashboardSnapshot;
import studio.humpback.backend.security.JwtTokenProvider;
import studio.humpback.backend.service.DashboardService;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void getDashboardReturnsSnapshotPayload() throws Exception {
        DashboardSnapshot bookingsSnapshot = DashboardSnapshot.builder()
                .generatedAt(Instant.parse("2026-02-20T20:00:00Z"))
                .calendarYear(2026)
                .calendarMonth(2)
                .counters(Map.of("today", 2L, "month", 11L, "year", 42L, "total", 120L))
                .byType(Map.of("REHARSAL", 50L))
                .bookingsByDay(Map.of("2026-02-20", 2L))
                .build();

        DashboardSnapshot contactsSnapshot = DashboardSnapshot.builder()
                .counters(Map.of("unread", 3L))
                .build();

        when(dashboardService.getBookingsSnapshot()).thenReturn(bookingsSnapshot);
        when(dashboardService.getContactsSnapshot()).thenReturn(contactsSnapshot);

        mockMvc.perform(get("/api/v1/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.calendarYear").value(2026))
                .andExpect(jsonPath("$.data.calendarMonth").value(2))
                .andExpect(jsonPath("$.data.counters.today").value(2))
                .andExpect(jsonPath("$.data.unreadContacts").value(3))
                .andExpect(jsonPath("$.data.byType.REHARSAL").value(50))
                .andExpect(jsonPath("$.data.bookingsByDay.2026-02-20").value(2));
    }
}

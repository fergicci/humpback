package studio.humpback.backend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import studio.humpback.backend.dto.ApiResponse;
import studio.humpback.backend.dto.DashboardResponse;
import studio.humpback.backend.model.DashboardSnapshot;
import studio.humpback.backend.service.DashboardService;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'READER', 'ROLE_ADMIN', 'ROLE_READER')")
    public ApiResponse<DashboardResponse> getDashboard() {
        DashboardSnapshot bookingsSnapshot = dashboardService.getBookingsSnapshot();
        DashboardSnapshot contactsSnapshot = dashboardService.getContactsSnapshot();

        Long unreadContacts = contactsSnapshot.getCounters() == null
                ? 0L
                : contactsSnapshot.getCounters().getOrDefault("unread", 0L);

        DashboardResponse response = DashboardResponse.builder()
                .generatedAt(bookingsSnapshot.getGeneratedAt())
                .calendarYear(bookingsSnapshot.getCalendarYear())
                .calendarMonth(bookingsSnapshot.getCalendarMonth())
                .counters(bookingsSnapshot.getCounters())
                .unreadContacts(unreadContacts)
                .byType(bookingsSnapshot.getByType())
                .bookingsByDay(bookingsSnapshot.getBookingsByDay())
                .build();

        return ApiResponse.success(response);
    }
}

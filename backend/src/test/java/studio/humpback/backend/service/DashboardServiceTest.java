package studio.humpback.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import studio.humpback.backend.model.Booking;
import studio.humpback.backend.model.BookingType;
import studio.humpback.backend.model.DashboardSnapshot;
import studio.humpback.backend.repository.BookingRepository;
import studio.humpback.backend.repository.ContactRepository;
import studio.humpback.backend.repository.DashboardSnapshotRepository;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private DashboardSnapshotRepository dashboardSnapshotRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void refreshDashboardSnapshotCalculatesAndPersistsAggregates() {
        when(bookingRepository.countByBookingAtBetween(any(), any()))
                .thenReturn(2L, 5L, 9L);
        when(bookingRepository.count()).thenReturn(14L);
        when(bookingRepository.findAll()).thenReturn(List.of(
                Booking.builder().bookingType(BookingType.REHARSAL).build(),
                Booking.builder().bookingType(BookingType.RECORDING).build(),
                Booking.builder().bookingType(BookingType.RECORDING).build()));
        when(bookingRepository.findByBookingAtBetween(any(), any())).thenReturn(List.of(
                Booking.builder().bookingAt(Instant.parse("2026-02-01T10:00:00Z")).build(),
                Booking.builder().bookingAt(Instant.parse("2026-02-01T18:00:00Z")).build(),
                Booking.builder().bookingAt(Instant.parse("2026-02-03T12:00:00Z")).build()));
        when(dashboardSnapshotRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DashboardSnapshot snapshot = dashboardService.refreshDashboardSnapshot();

        assertThat(snapshot.getId()).isEqualTo("bookings");
        assertThat(snapshot.getCounters()).containsEntry("today", 2L);
        assertThat(snapshot.getCounters()).containsEntry("month", 5L);
        assertThat(snapshot.getCounters()).containsEntry("year", 9L);
        assertThat(snapshot.getCounters()).containsEntry("total", 14L);
        assertThat(snapshot.getByType()).containsEntry("REHARSAL", 1L);
        assertThat(snapshot.getByType()).containsEntry("RECORDING", 2L);
        assertThat(snapshot.getBookingsByDay()).containsEntry("2026-02-01", 2L);
        assertThat(snapshot.getBookingsByDay()).containsEntry("2026-02-03", 1L);
        assertThat(snapshot.getGeneratedAt()).isNotNull();
        assertThat(snapshot.getCalendarYear()).isNotNull();
        assertThat(snapshot.getCalendarMonth()).isBetween(1, 12);
    }

    @Test
    void getBookingsSnapshotUsesStoredSnapshotWhenAvailable() {
        DashboardSnapshot stored = DashboardSnapshot.builder()
                .id("bookings")
                .generatedAt(Instant.parse("2026-02-20T20:00:00Z"))
                .calendarYear(2026)
                .calendarMonth(2)
                .counters(Map.of("today", 1L))
                .byType(Map.of("REHARSAL", 1L))
                .bookingsByDay(Map.of("2026-02-20", 1L))
                .build();
        when(dashboardSnapshotRepository.findById("bookings")).thenReturn(Optional.of(stored));

        DashboardSnapshot response = dashboardService.getBookingsSnapshot();

        assertThat(response.getGeneratedAt()).isEqualTo(stored.getGeneratedAt());
        assertThat(response.getCalendarYear()).isEqualTo(2026);
        assertThat(response.getCalendarMonth()).isEqualTo(2);
        assertThat(response.getCounters()).containsEntry("today", 1L);
        verify(dashboardSnapshotRepository, never()).save(any());
    }

    @Test
    void refreshContactsSnapshotCalculatesUnreadCount() {
        when(contactRepository.countByReadFalse()).thenReturn(7L);
        when(dashboardSnapshotRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DashboardSnapshot snapshot = dashboardService.refreshContactsSnapshot();

        assertThat(snapshot.getId()).isEqualTo("contacts");
        assertThat(snapshot.getCounters()).containsEntry("unread", 7L);
        assertThat(snapshot.getGeneratedAt()).isNotNull();
    }
}

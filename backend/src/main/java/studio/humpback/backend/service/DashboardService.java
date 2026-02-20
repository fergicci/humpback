package studio.humpback.backend.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import studio.humpback.backend.model.Booking;
import studio.humpback.backend.model.BookingType;
import studio.humpback.backend.model.DashboardSnapshot;
import studio.humpback.backend.repository.BookingRepository;
import studio.humpback.backend.repository.ContactRepository;
import studio.humpback.backend.repository.DashboardSnapshotRepository;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final String SNAPSHOT_ID_BOOKINGS = "bookings";
    private static final String SNAPSHOT_ID_CONTACTS = "contacts";
    private static final String COUNTER_TODAY = "today";
    private static final String COUNTER_MONTH = "month";
    private static final String COUNTER_YEAR = "year";
    private static final String COUNTER_TOTAL = "total";
    private static final String COUNTER_UNREAD = "unread";
    private static final ZoneId ZONE_UTC = ZoneOffset.UTC;

    private final BookingRepository bookingRepository;
    private final ContactRepository contactRepository;
    private final DashboardSnapshotRepository dashboardSnapshotRepository;

    public DashboardSnapshot getBookingsSnapshot() {
        return dashboardSnapshotRepository.findById(SNAPSHOT_ID_BOOKINGS)
                .orElseGet(this::refreshDashboardSnapshot);
    }

    public DashboardSnapshot getContactsSnapshot() {
        return dashboardSnapshotRepository.findById(SNAPSHOT_ID_CONTACTS)
                .orElseGet(this::refreshContactsSnapshot);
    }

    public DashboardSnapshot refreshDashboardSnapshot() {
        LocalDate now = LocalDate.now(ZONE_UTC);

        Instant todayStart = now.atStartOfDay(ZONE_UTC).toInstant();
        Instant tomorrowStart = now.plusDays(1).atStartOfDay(ZONE_UTC).toInstant();

        LocalDate monthStartDate = now.withDayOfMonth(1);
        Instant monthStart = monthStartDate.atStartOfDay(ZONE_UTC).toInstant();
        Instant nextMonthStart = monthStartDate.plusMonths(1).atStartOfDay(ZONE_UTC).toInstant();

        LocalDate yearStartDate = now.withDayOfYear(1);
        Instant yearStart = yearStartDate.atStartOfDay(ZONE_UTC).toInstant();
        Instant nextYearStart = yearStartDate.plusYears(1).atStartOfDay(ZONE_UTC).toInstant();

        Map<String, Long> counters = new LinkedHashMap<>();
        counters.put(COUNTER_TODAY, countBetweenInclusiveStart(todayStart, tomorrowStart));
        counters.put(COUNTER_MONTH, countBetweenInclusiveStart(monthStart, nextMonthStart));
        counters.put(COUNTER_YEAR, countBetweenInclusiveStart(yearStart, nextYearStart));
        counters.put(COUNTER_TOTAL, bookingRepository.count());

        Map<String, Long> byType = Arrays.stream(BookingType.values())
                .collect(Collectors.toMap(BookingType::name, value -> 0L, (left, right) -> left, LinkedHashMap::new));

        for (Booking booking : bookingRepository.findAll()) {
            if (booking.getBookingType() == null) {
                continue;
            }
            byType.merge(booking.getBookingType().name(), 1L, Long::sum);
        }

        List<Booking> monthBookings = findBetweenInclusiveStart(monthStart, nextMonthStart);
        Map<String, Long> bookingsByDay = new LinkedHashMap<>();
        for (Booking booking : monthBookings) {
            if (booking.getBookingAt() == null) {
                continue;
            }
            String dayKey = booking.getBookingAt().atZone(ZONE_UTC).toLocalDate().toString();
            bookingsByDay.merge(dayKey, 1L, Long::sum);
        }

        DashboardSnapshot snapshot = DashboardSnapshot.builder()
                .id(SNAPSHOT_ID_BOOKINGS)
                .generatedAt(Instant.now())
                .calendarYear(now.getYear())
                .calendarMonth(now.getMonthValue())
                .counters(counters)
                .byType(byType)
                .bookingsByDay(bookingsByDay)
                .build();

        return dashboardSnapshotRepository.save(snapshot);
    }

    public DashboardSnapshot refreshContactsSnapshot() {
        Map<String, Long> counters = new LinkedHashMap<>();
        counters.put(COUNTER_UNREAD, contactRepository.countByReadFalse());

        DashboardSnapshot snapshot = DashboardSnapshot.builder()
                .id(SNAPSHOT_ID_CONTACTS)
                .generatedAt(Instant.now())
                .counters(counters)
                .build();

        return dashboardSnapshotRepository.save(snapshot);
    }

    public void refreshAllSnapshots() {
        refreshDashboardSnapshot();
        refreshContactsSnapshot();
    }

    private long countBetweenInclusiveStart(Instant startInclusive, Instant endExclusive) {
        return bookingRepository.countByBookingAtBetween(startInclusive, toInclusiveUpperBound(endExclusive));
    }

    private List<Booking> findBetweenInclusiveStart(Instant startInclusive, Instant endExclusive) {
        return bookingRepository.findByBookingAtBetween(startInclusive, toInclusiveUpperBound(endExclusive));
    }

    private Instant toInclusiveUpperBound(Instant endExclusive) {
        return endExclusive.minusNanos(1);
    }
}

package studio.humpback.backend.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import studio.humpback.backend.model.Booking;
import studio.humpback.backend.model.BookingType;
import studio.humpback.backend.repository.BookingRepository;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final String BOOKING_NOT_FOUND = "Booking with if %s not found";
    private static final String BOOKING_OVERLAP = "Booking time overlaps with an existing booking";

    private BookingRepository bookingRepository;

    public List<Booking> getTodayBookings() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        Instant startOfDay = LocalDateTime.now().toInstant(ZoneOffset.UTC);
        Instant endOfDay = today.atTime(23, 59, 59, 999999999)
                .atZone(ZoneOffset.UTC).toInstant();
        return bookingRepository.findByBookingAtBetween(startOfDay, endOfDay);
    }

    public Page<Booking> getPage(Pageable pageable) {
        return bookingRepository.findAll(pageable);
    }

    public Booking create(String name, String email, String phone, Instant bookingAt, Integer numberOfHours,
            BookingType bookingType) {
        Booking booking = Booking.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .bookingAt(bookingAt)
                .numberOfHours(numberOfHours)
                .bookingType(bookingType)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        checkOverlap(booking);

        return bookingRepository.save(booking);
    }

    public Booking update(String id, Instant bookingAt, Integer numberOfHours,
            BookingType bookingType) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format(BOOKING_NOT_FOUND, id)));

        booking.setBookingAt(bookingAt);
        booking.setNumberOfHours(numberOfHours);
        booking.setBookingType(bookingType);

        checkOverlap(booking);

        booking.setUpdatedAt(Instant.now());

        return bookingRepository.save(booking);
    }

    public void delete(String id) {
        bookingRepository.deleteById(id);
    }

    private void checkOverlap(Booking booking) {
        List<Booking> existingBookings = bookingRepository.findByBookingAtBetween(
                booking.getBookingAt(),
                booking.getBookingAt().plus(booking.getNumberOfHours(), ChronoUnit.HOURS));

        if (existingBookings.isEmpty()) {
            return;
        }

        for (Booking existingBooking : existingBookings) {
            if (!canOverlap(booking, existingBooking)) {
                throw new IllegalArgumentException(BOOKING_OVERLAP);
            }
        }
    }

    private Boolean canOverlap(Booking booking, Booking existingBooking) {
        if (!BookingType.allowOverlap().contains(booking.getBookingType())) {
            return false;
        }

        if (!BookingType.allowOverlap().contains(existingBooking.getBookingType())) {
            return false;
        }

        if (booking.getBookingType() == existingBooking.getBookingType()) {
            return false;
        }

        return true;
    }

    public List<Booking> getBetweenBookings(String from, String to) {
        Instant startDateTime = LocalDateTime.parse(from).toInstant(ZoneOffset.UTC);
        Instant endDateTime = LocalDateTime.parse(to).toInstant(ZoneOffset.UTC);
        return bookingRepository.findByBookingAtBetween(startDateTime, endDateTime);
    }
}

package studio.humpback.backend.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import studio.humpback.backend.exception.ResourceNotFoundException;
import studio.humpback.backend.model.Booking;
import studio.humpback.backend.model.BookingType;
import studio.humpback.backend.repository.BookingDslQuerySpecification;
import studio.humpback.backend.repository.BookingRepository;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final String BOOKING_NOT_FOUND = "Booking with id %s not found";
    private static final String BOOKING_OVERLAP = "Booking time overlaps with an existing booking";

    private final BookingRepository bookingRepository;
    private final BookingDslQuerySpecification bookingDslQuerySpecification;

    public Page<Booking> getPage(Pageable pageable) {
        return bookingRepository.findAll(pageable);
    }

    public Page<Booking> getPage(Pageable pageable, List<String> dslFilters) {
        if (dslFilters == null || dslFilters.isEmpty()) {
            return bookingRepository.findAll(pageable);
        }
        Predicate predicate = bookingDslQuerySpecification.toPredicate(dslFilters);
        return bookingRepository.findAll(predicate, pageable);
    }

    public Booking getById(String id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(BOOKING_NOT_FOUND, id)));
    }

    public Booking create(String name, String email, String phone, Instant bookingAt, Integer numberOfHours,
            BookingType bookingType) {
        Booking booking = Booking.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .bookingAt(bookingAt)
                .endAt(bookingAt.plus(numberOfHours, ChronoUnit.HOURS))
                .bookingType(bookingType)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        checkOverlap(booking);

        return bookingRepository.save(booking);
    }

    public Booking update(String id, String name, String email, String phone, Instant bookingAt, Integer numberOfHours,
            BookingType bookingType) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(BOOKING_NOT_FOUND, id)));

        booking.setName(name);
        booking.setEmail(email);
        booking.setPhone(phone);
        booking.setBookingAt(bookingAt);
        booking.setEndAt(bookingAt.plus(numberOfHours, ChronoUnit.HOURS));
        booking.setBookingType(bookingType);

        checkOverlap(booking);

        booking.setUpdatedAt(Instant.now());

        return bookingRepository.save(booking);
    }

    public void delete(String id) {
        bookingRepository.deleteById(id);
    }

    public void setPayment(String id, Boolean hasBeenPayed) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(BOOKING_NOT_FOUND, id)));
        booking.setHasBeenPayed(hasBeenPayed);
        booking.setUpdatedAt(Instant.now());
        bookingRepository.save(booking);
    }

    private void checkOverlap(Booking booking) {

        List<Booking> conflicts = bookingRepository.findByBookingAtLessThanAndEndAtGreaterThan(
                booking.getEndAt(),
                booking.getBookingAt());

        for (Booking existing : conflicts) {
            if (booking.getId() != null && booking.getId().equals(existing.getId())) {
                continue;
            }

            boolean sharesRoom = !Collections.disjoint(
                    booking.getBookingType().getUsedRooms(),
                    existing.getBookingType().getUsedRooms());

            if (sharesRoom) {
                throw new IllegalArgumentException(BOOKING_OVERLAP);
            }
        }
    }

    public List<Booking> getBookingsBetween(Instant from, Instant to) {
        return bookingRepository.findByBookingAtBetween(from, to);
    }
}

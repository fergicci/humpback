package studio.humpback.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.querydsl.core.types.Predicate;

import studio.humpback.backend.exception.ResourceNotFoundException;
import studio.humpback.backend.model.Booking;
import studio.humpback.backend.model.BookingType;
import studio.humpback.backend.repository.BookingDslQuerySpecification;
import studio.humpback.backend.repository.BookingRepository;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingDslQuerySpecification bookingDslQuerySpecification;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void getPageWithoutDslUsesRepositoryDefaultFindAll() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Booking> expected = new PageImpl<>(List.of(), pageable, 0);
        when(bookingRepository.findAll(pageable)).thenReturn(expected);

        Page<Booking> result = bookingService.getPage(pageable, List.of());

        assertThat(result).isSameAs(expected);
        verify(bookingRepository).findAll(pageable);
        verify(bookingDslQuerySpecification, never()).toPredicate(anyList());
    }

    @Test
    void getPageWithDslUsesPredicateExecution() {
        PageRequest pageable = PageRequest.of(0, 10);
        List<String> dsl = List.of("hasBeenPayed:eq:false");
        Predicate predicate = org.mockito.Mockito.mock(Predicate.class);
        Page<Booking> expected = new PageImpl<>(List.of(), pageable, 0);

        when(bookingDslQuerySpecification.toPredicate(dsl)).thenReturn(predicate);
        when(bookingRepository.findAll(predicate, pageable)).thenReturn(expected);

        Page<Booking> result = bookingService.getPage(pageable, dsl);

        assertThat(result).isSameAs(expected);
        verify(bookingDslQuerySpecification).toPredicate(dsl);
        verify(bookingRepository).findAll(predicate, pageable);
    }

    @Test
    void setPaymentUpdatesBookingAndSaves() {
        Booking booking = Booking.builder()
                .id("b1")
                .name("Alice")
                .bookingType(BookingType.REHARSAL)
                .bookingAt(Instant.parse("2026-02-20T10:00:00Z"))
                .endAt(Instant.parse("2026-02-20T12:00:00Z"))
                .hasBeenPayed(false)
                .build();

        when(bookingRepository.findById("b1")).thenReturn(Optional.of(booking));

        bookingService.setPayment("b1", true);

        assertThat(booking.getHasBeenPayed()).isTrue();
        assertThat(booking.getUpdatedAt()).isNotNull();
        verify(bookingRepository).save(booking);
    }

    @Test
    void setPaymentThrowsWhenBookingDoesNotExist() {
        when(bookingRepository.findById("missing")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> bookingService.setPayment("missing", true));

        assertThat(ex.getMessage()).contains("Booking with id missing not found");
    }
}

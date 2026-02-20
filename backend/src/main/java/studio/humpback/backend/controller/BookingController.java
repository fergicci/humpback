package studio.humpback.backend.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import studio.humpback.backend.dto.ApiResponse;
import studio.humpback.backend.dto.BookingRequest;
import studio.humpback.backend.dto.BookingResponse;
import studio.humpback.backend.dto.PagedResponse;
import studio.humpback.backend.model.Booking;
import studio.humpback.backend.model.BookingType;
import studio.humpback.backend.service.BookingService;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

        private final BookingService bookingService;

        @GetMapping
        @PreAuthorize("hasAnyAuthority('ADMIN', 'READER')")
        public ApiResponse<PagedResponse<BookingResponse>> getBookings(
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(name = "dsl", required = false) List<String> dsl) {
                Pageable pageable = PageRequest.of(
                                page - 1,
                                size,
                                Sort.by(Sort.Direction.DESC, "bookingAt"));

                List<String> dslFilters = dsl == null ? Collections.emptyList() : dsl;
                Page<Booking> bookingPage = bookingService.getPage(pageable, dslFilters);
                List<BookingResponse> content = bookingPage
                                .getContent()
                                .stream()
                                .map(this::toResponse)
                                .collect(Collectors.toList());

                PagedResponse<BookingResponse> responses = PagedResponse.<BookingResponse>builder()
                                .content(content)
                                .page(bookingPage.getNumber())
                                .size(bookingPage.getSize())
                                .totalElements(bookingPage.getTotalElements())
                                .totalPages(bookingPage.getTotalPages())
                                .build();

                return ApiResponse.success(responses);
        }

        @GetMapping("/today")
        public ApiResponse<List<BookingResponse>> getTodayBookings() {
                InstantRange range = resolveRange(Optional.empty(), Optional.empty());
                List<Booking> bookings = bookingService.getBookingsBetween(range.from(), range.to());

                List<BookingResponse> responses = bookings.stream()
                                .map(this::toRestrictResponse)
                                .collect(Collectors.toList());
                return ApiResponse.success(responses);
        }

        @GetMapping("/on")
        public ApiResponse<List<BookingResponse>> getBookingsBetween(
                        @RequestParam Optional<Instant> from,
                        @RequestParam Optional<Instant> to) {

                InstantRange range = resolveRange(from, to);

                List<Booking> bookings = bookingService.getBookingsBetween(range.from(), range.to());

                List<BookingResponse> responses = bookings.stream()
                                .map(this::toRestrictResponse)
                                .collect(Collectors.toList());

                return ApiResponse.success(responses);
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasAnyAuthority('ADMIN', 'READER')")
        public ApiResponse<BookingResponse> getBookingById(@PathVariable String id) {
                Booking booking = bookingService.getById(id);
                return ApiResponse.success(toResponse(booking));
        }

        @PostMapping
        public ApiResponse<BookingResponse> createBooking(@RequestBody @Valid BookingRequest bookingRequest) {
                Booking booking = bookingService.create(
                                bookingRequest.getName(),
                                bookingRequest.getEmail(),
                                bookingRequest.getPhone(),
                                bookingRequest.getBookingAt().toInstant(),
                                bookingRequest.getNumberOfHours(),
                                BookingType.valueOf(bookingRequest.getType()));
                return ApiResponse.success(toResponse(booking));
        }

        @PutMapping("/{id}")
        @ResponseStatus(code = HttpStatus.OK)
        @PreAuthorize("hasAuthority('ADMIN')")
        public ApiResponse<BookingResponse> updateBooking(@PathVariable String id,
                        @RequestBody @Valid BookingRequest bookingRequest) {
                Booking booking = bookingService.update(
                                id,
                                bookingRequest.getName(),
                                bookingRequest.getEmail(),
                                bookingRequest.getPhone(),
                                bookingRequest.getBookingAt().toInstant(),
                                bookingRequest.getNumberOfHours(),
                                BookingType.valueOf(bookingRequest.getType()));
                return ApiResponse.success(toResponse(booking));
        }

        @PatchMapping("/{id}/payment/{hasBeenPayed}")
        @ResponseStatus(code = HttpStatus.OK)
        @PreAuthorize("hasAuthority('ADMIN')")
        public ApiResponse<Void> setBookingPayment(@PathVariable String id, @PathVariable Boolean hasBeenPayed) {
                bookingService.setPayment(id, hasBeenPayed);
                return ApiResponse.success();
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('ADMIN')")
        public ApiResponse<Void> deleteBooking(@PathVariable String id) {
                bookingService.delete(id);
                return ApiResponse.success();
        }

        private BookingResponse toResponse(Booking booking) {
                boolean isAdmin = Optional
                                .ofNullable(SecurityContextHolder.getContext().getAuthentication())
                                .filter(Authentication::isAuthenticated)
                                .map(Authentication::getAuthorities)
                                .stream()
                                .flatMap(Collection::stream)
                                .anyMatch(a -> a.getAuthority().equals("ADMIN"));

                return isAdmin
                                ? toFullResponse(booking)
                                : toRestrictResponse(booking);
        }

        private BookingResponse toRestrictResponse(Booking booking) {
                return BookingResponse.builder()
                                .bookingAt(booking.getBookingAt())
                                .endAt(booking.getEndAt())
                                .build();
        }

        private BookingResponse toFullResponse(Booking booking) {
                return BookingResponse.builder()
                                .id(booking.getId())
                                .name(booking.getName())
                                .email(booking.getEmail())
                                .phone(booking.getPhone())
                                .bookingAt(booking.getBookingAt())
                                .endAt(booking.getEndAt())
                                .bookingType(booking.getBookingType())
                                .bookingRoom(booking.getBookingRoom())
                                .hasBeenPayed(booking.getHasBeenPayed())
                                .build();
        }

        private InstantRange resolveRange(Optional<Instant> from, Optional<Instant> to) {

                ZoneId zone = ZoneOffset.UTC;

                Instant start = from.orElseGet(() -> LocalDate.now(zone)
                                .atStartOfDay(zone)
                                .toInstant());

                Instant end = to.orElseGet(() -> LocalDate.now(zone)
                                .plusDays(1)
                                .atStartOfDay(zone)
                                .toInstant());

                if (end.isBefore(start)) {
                    throw new IllegalArgumentException("'to' must be after 'from'");
                }

                return new InstantRange(start, end);
        }

        record InstantRange(Instant from, Instant to) {
        }
}

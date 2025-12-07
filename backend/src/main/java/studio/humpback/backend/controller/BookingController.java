package studio.humpback.backend.controller;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

        private BookingService bookingService;

        @GetMapping
        @PreAuthorize("hasAuthority('ADMIN')")
        public ApiResponse<PagedResponse<BookingResponse>> getBookings(
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Locale locale) {
                Pageable pageable = PageRequest.of(
                                page - 1,
                                size,
                                Sort.by(Sort.Direction.DESC, "bookingAt"));

                Page<Booking> bookingPage = bookingService.getPage(pageable);
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
                List<Booking> bookings = bookingService.getTodayBookings();
                List<BookingResponse> responses = bookings.stream()
                                .map(this::toRestrictResponse)
                                .collect(Collectors.toList());
                return ApiResponse.success(responses);
        }

        @GetMapping("/on")
        public ApiResponse<List<BookingResponse>> getTodayBookings(
                        @RequestParam("from") String from,
                        @RequestParam("to") String to
        ) {
                List<Booking> bookings = bookingService.getBetweenBookings(from, to);
                List<BookingResponse> responses = bookings.stream()
                                .map(this::toRestrictResponse)
                                .collect(Collectors.toList());
                return ApiResponse.success(responses);
        }

        @PostMapping
        public ApiResponse<BookingResponse> createBooking(@RequestBody @Valid BookingRequest bookingRequest) {
                Booking booking = bookingService.create(
                                bookingRequest.getName(),
                                bookingRequest.getEmail(),
                                bookingRequest.getPhone(),
                                bookingRequest.getBookingAt(),
                                bookingRequest.getNumberOfHours(),
                                BookingType.valueOf(bookingRequest.getType()));
                return ApiResponse.success(toResponse(booking));
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('ADMIN')")
        public ApiResponse<Void> deleteBooking(@RequestParam String id) {
                bookingService.delete(id);
                return ApiResponse.success();
        }

        private BookingResponse toResponse(Booking booking) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth.getAuthorities().stream()
                                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ADMIN"))) {
                        return toFullResponse(booking);
                } else {
                        return toRestrictResponse(booking);
                }
        }

        private BookingResponse toRestrictResponse(Booking booking) {
                return BookingResponse.builder()
                                .bookingAt(booking.getBookingAt())
                                .numberOfHours(booking.getNumberOfHours())
                                .build();
        }

        private BookingResponse toFullResponse(Booking booking) {
                return BookingResponse.builder()
                                .id(booking.getId())
                                .name(booking.getName())
                                .email(booking.getEmail())
                                .phone(booking.getPhone())
                                .bookingAt(booking.getBookingAt())
                                .numberOfHours(booking.getNumberOfHours())
                                .bookingType(booking.getBookingType())
                                .bookingRoom(booking.getBookingRoom())
                                .hasBeenPayed(booking.getHasBeenPayed())
                                .build();
        }
}

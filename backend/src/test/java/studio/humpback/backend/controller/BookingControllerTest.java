package studio.humpback.backend.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import studio.humpback.backend.config.MessageConfig;
import studio.humpback.backend.model.Booking;
import studio.humpback.backend.model.BookingType;
import studio.humpback.backend.security.JwtTokenProvider;
import studio.humpback.backend.service.BookingService;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(MessageConfig.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void getBookingTypesReturnsFriendlyLabels() throws Exception {
        mockMvc.perform(get("/api/v1/bookings/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].value").value("REHARSAL"))
                .andExpect(jsonPath("$.data[0].label").value("Rehearsal"));
    }

    @Test
    void getBookingsWithDslFiltersCallsServiceWithDslList() throws Exception {
        Booking booking = Booking.builder()
                .id("b1")
                .name("Alice")
                .email("alice@example.com")
                .phone("+5511999999999")
                .bookingAt(Instant.parse("2026-12-31T23:59:00Z"))
                .endAt(Instant.parse("2027-01-01T01:59:00Z"))
                .bookingType(BookingType.REHARSAL)
                .hasBeenPayed(false)
                .build();

        when(bookingService.getPage(
                any(),
                eq(List.of("hasBeenPayed:eq:false", "bookingType:eq:REHARSAL"))))
                .thenReturn(new PageImpl<>(List.of(booking), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/bookings?page=1&size=10&dsl=hasBeenPayed:eq:false&dsl=bookingType:eq:REHARSAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].bookingAt").exists());

        verify(bookingService).getPage(
                any(),
                eq(List.of("hasBeenPayed:eq:false", "bookingType:eq:REHARSAL")));
    }

    @Test
    void createBookingAcceptsTypeWithSpacesAndCallsService() throws Exception {
        Booking booking = Booking.builder()
                .id("b2")
                .name("Alice")
                .email("alice@example.com")
                .phone("+5511999999999")
                .bookingAt(Instant.parse("2026-12-31T23:59:00Z"))
                .endAt(Instant.parse("2027-01-01T01:59:00Z"))
                .bookingType(BookingType.REHARSAL_RECORDING)
                .build();

        when(bookingService.create(
                "Alice",
                "alice@example.com",
                "+5511999999999",
                Instant.parse("2026-12-31T23:59:00Z"),
                2,
                BookingType.REHARSAL_RECORDING)).thenReturn(booking);

        String payload = """
                {
                  "name": "Alice",
                  "email": "alice@example.com",
                  "phone": "+5511999999999",
                  "bookingAt": "2026-12-31T23:59:00.000Z",
                  "numberOfHours": 2,
                  "type": "Rehearsal Recording"
                }
                """;

        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bookingAt").exists());

        verify(bookingService).create(
                "Alice",
                "alice@example.com",
                "+5511999999999",
                Instant.parse("2026-12-31T23:59:00Z"),
                2,
                BookingType.REHARSAL_RECORDING);
    }

    @Test
    void createBookingAcceptsIsoDateAndCallsService() throws Exception {
        Booking booking = Booking.builder()
                .id("b1")
                .name("Alice")
                .email("alice@example.com")
                .phone("+5511999999999")
                .bookingAt(Instant.parse("2026-12-31T23:59:00Z"))
                .endAt(Instant.parse("2027-01-01T01:59:00Z"))
                .bookingType(BookingType.REHARSAL)
                .build();

        when(bookingService.create(
                "Alice",
                "alice@example.com",
                "+5511999999999",
                Instant.parse("2026-12-31T23:59:00Z"),
                2,
                BookingType.REHARSAL)).thenReturn(booking);

        String payload = """
                {
                  "name": "Alice",
                  "email": "alice@example.com",
                  "phone": "+5511999999999",
                  "bookingAt": "2026-12-31T23:59:00.000Z",
                  "numberOfHours": 2,
                  "type": "REHARSAL"
                }
                """;

        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bookingAt").exists())
                .andExpect(jsonPath("$.data.endAt").exists());

        verify(bookingService).create(
                "Alice",
                "alice@example.com",
                "+5511999999999",
                Instant.parse("2026-12-31T23:59:00Z"),
                2,
                BookingType.REHARSAL);
    }

    @Test
    void createBookingWithInvalidPayloadReturnsBadRequest() throws Exception {
        String payload = """
                {
                  "name": "",
                  "email": "invalid",
                  "phone": "",
                  "bookingAt": "2020-01-01T00:00:00.000Z",
                  "numberOfHours": 1,
                  "type": ""
                }
                """;

        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.details", hasItem(containsString("name"))))
                .andExpect(jsonPath("$.error.details", hasItem(containsString("email"))))
                .andExpect(jsonPath("$.error.details", hasItem(containsString("bookingAt"))));
    }
}

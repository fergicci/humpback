package studio.humpback.backend.dto;

import java.time.Instant;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardResponse {
    private Instant generatedAt;
    private Integer calendarYear;
    private Integer calendarMonth;
    private Map<String, Long> counters;
    private Long unreadContacts;
    private Map<String, Long> byType;
    private Map<String, Long> bookingsByDay;
}

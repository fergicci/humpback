package studio.humpback.backend.model;

import java.time.Instant;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "dashboard")
public class DashboardSnapshot {

    @Id
    private String id;

    private Instant generatedAt;

    private Integer calendarYear;

    /**
     * 1-based month value (January = 1, December = 12).
     */
    private Integer calendarMonth;

    /**
     * Generic KPI map, e.g. today/month/year/total.
     */
    private Map<String, Long> counters;

    private Map<String, Long> byType;

    private Map<String, Long> bookingsByDay;
}

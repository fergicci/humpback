package studio.humpback.backend.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Data;
import studio.humpback.backend.model.BookingRoom;
import studio.humpback.backend.model.BookingType;

@Data
@Builder
public class BookingResponse {
    
    private String id;
    private String name;
    private String email;
    private String phone;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Instant bookingAt;
    private Integer numberOfHours;
    private BookingType bookingType;
    private BookingRoom bookingRoom;
    private Boolean hasBeenPayed;
}

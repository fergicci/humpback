package studio.humpback.backend.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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
@Document(collection = "bookings")
public class Booking {

    @Id
    private String id;
    private String name;
    private String email;
    private String phone;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant bookingAt;
    private Integer numberOfHours;

    @Field(name = "type")
    private BookingType bookingType;

    @Field(name = "room")
    private BookingRoom bookingRoom;

    @Builder.Default
    private Boolean hasBeenPayed = false;
}
